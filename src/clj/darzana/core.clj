(ns darzana.core
  (:use
    [clojure.tools.nrepl.server :only (start-server stop-server)]
    [compojure.core :as compojure :only (GET)]
    [darzana.template :only (handlebars)]
    [darzana.router :only (load-app-routes)]
    [taoensso.carmine.ring :only [carmine-store]])
  (:require
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [compojure.core :as compojure]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [ring.util.response :as response]
    [org.httpkit.client :as http]
    [taoensso.carmine :as car :refer (wcar)]
    [darzana.context :as context]
    [darzana.workspace :as workspace]
    [darzana.admin router template api git workspace]
    [darzana.api :as api]))

(defmacro defblock [block-name & body]
  "Define a block component for cljs."
  `(aset js/Blockly.Language ~(name block-name) ~@body))

(def ^:dynamic redis-connection {:pool {} :spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body]
  "Redis context wrapper"
  `(car/wcar redis-connection ~@body))

(defn set-application-scope [scope]
  (dosync
    (ref-set context/application-scope scope)))

(defn execute-api [context api]
  (let [ url (api/build-url context api)
         cache (try (wcar* (car/get (str (api :name) "-" url)))
                 (catch Exception e (log/debug "Skip cache-get" e)))]
    (log/info url)
    { :api api
      :url url
      :from-cache (not (empty? cache)) 
      :response (if cache
                  cache 
                  (http/request
                    (api/build-request {:url url :method (get api :method :get)} context api)
                    nil))}))

(defn find-api [apis name]
  (first
    (filter #(= name (get % :name)) apis)))

(defn cache-response [response cache-key api]
  (let [ expire (api :expire) ]
    (if (and (= (api :method) :get) expire) 
      (try
        (wcar*
          (car/set cache-key (json/write-str response))
          (if expire (car/expire cache-key expire)))
        (catch Exception e (log/debug "Skip cache-set" e))))))

(defn- call-api-internal [context apis]
  (for [result (doall (map #(execute-api context %) apis))]
    (let [api (result :api)]
      (if (result :from-cache) 
        (do
          (log/debug "API response(from cache)" (result :response))
          {:page {(name (api :name)) (json/read-str (result :response))} }) ;; From Cache
        (let [ response (api/parse-response @(result :response))
               cache-key (str (api :name) "-" (get-in @(result :response) [:opts :url]))]
          (log/debug "API response" @(result :response))
          (if (apply (api :success?) [@(result :response)])
            (do
              (cache-response response cache-key api)
              {:page {(name (api :name)) response}})
            { :error
              {(name (api :name))
                { "status"   (-> result :response deref :status)
                  "message" response}}}))))))

(defn call-api [context api]
  (let [ apis (if (map? api) [api] api)
         api-responses (call-api-internal context apis)]
    (assoc context :scope
      (reduce #(merge-with merge %1 %2) (context :scope) api-responses))))

(defmacro if-success [context success error]
  `(if (empty? (~context :error))
     (-> ~context ~success)
     (-> ~context ~error)))

(defmacro if-contains
  ([context key contains]
    `(if-contains ~context ~key ~contains do))
  ([context key contains not-contains]
    `(if (context/find-in-scopes ~context ~key)
       (-> ~context ~contains)
       (-> ~context ~not-contains))))

(defn store-session [context & session-keys]
  (apply merge-with merge context
    (map (fn [_] {:session-add-keys
                   (if (vector? _)
                     (apply hash-map (reverse _))
                     (hash-map _ _))})
      session-keys)))

(defn- save-session [response context]
  (let [ session (->
                   (get-in context [:scope :session])
                   (#(reduce dissoc % (context :session-remove-keys)))
                   (#(reduce
                       (fn [m k] (apply assoc m k)) %
                       (for [[session-key context-keys] (context :session-add-keys)]
                         [(name session-key) (context/find-in-scopes context context-keys)]))))]
    (if (empty? session)
      response
      (assoc response :session session))))

(defn- save-cookies [response context]
  (assoc response :cookies
    (get-in context [:scope :cookies])))

(defn render [ctx template]
  (-> (ring.util.response/response (.apply (.compile @handlebars template) (context/merge-scope ctx)))
    (ring.util.response/content-type (context/find-in-scopes ctx :content-type "text/html"))
    (ring.util.response/charset (context/find-in-scopes ctx :charset "UTF-8"))
    (save-session ctx)
    (save-cookies ctx)))

(defn redirect
  ([context url]
    (redirect context url nil))
  ([context url options]
    (-> (ring.util.response/redirect
          (if options
            (api/build-url context
              { :url url
                :method :get
                :query-keys (options :query-keys)})
            url))
      (save-session context)
      (save-cookies context))))

(defmacro defmarga [method url & exprs ]
  `(~method ~url {:as request#}
     (-> (context/create-context request#) ~@exprs)))

(defn load-routes []
  (compojure/routes
    (GET "/router/reload" []
      (do (load-routes) "reloaded."))
    (load-app-routes)))

(def admin-routes
  (compojure/routes
    darzana.admin.template/routes
    darzana.admin.router/routes
    darzana.admin.api/routes
    darzana.admin.git/routes
    darzana.admin.workspace/routes
    (GET "/" [] (response/resource-response "index.html"
                  {:root "darzana/admin/public"}))
    (route/resources "/" {:root "darzana/admin/public"} )))

(def admin-app-initialized (atom nil))

(defn admin-app [args]
  (reset! darzana.router/route-namespace (create-ns 'app))
  (reset! darzana.router/plugins ['darzana.ab-testing])
  (when-not @admin-app-initialized
    (let [app-scope (io/file "dev-resources/app_scope.clj")]
      (when (. app-scope exists) (-> app-scope (.getPath) load-file)))
    (load-file "dev-resources/api.clj")
    (workspace/change-workspace "master")
    (reset! admin-app-initialized true))
  ((handler/site
     (compojure/routes (load-routes) (compojure/context "/admin" [] admin-routes))
     {:session { :store (carmine-store redis-connection) }}) args))

