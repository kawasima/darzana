(ns darzana.core
  (:use
    [compojure.core :as compojure :only (GET POST ANY defroutes)]
    [clojure.tools.nrepl.server :only (start-server stop-server)]
    [clojure.tools.logging]
    [darzana.api :only (defapi)])
  (:require
    [clojure.string :as string]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [org.httpkit.client :as http]
    [taoensso.carmine :as car :refer (wcar)]
    [clojure.data.json :as json]
    [darzana.template :as darzana-template]
    [darzana.router :as darzana-router]
    [darzana.context :as context])
  (:import
    [com.github.jknack.handlebars Handlebars Handlebars$SafeString Handlebars$Utils]
    [com.github.jknack.handlebars Helper]
    [com.github.jknack.handlebars.io FileTemplateLoader]
    [net.sf.json.xml XMLSerializer]))

(def darzana-routes (ref []))

(def handlebars
  (Handlebars. (FileTemplateLoader. "resources/hbs")))

(.registerHelper handlebars "debug"
  (reify Helper
    (apply [this context options]
      (Handlebars$SafeString.
        (str
          "<link rel=\"stylesheet\" href=\"/css/debug.css\"/>"
          "<script src=\"/js/debug.js\"></script>"
          "<script>var DATA="
          (json/write-str (.model (.context options)))
          ";document.write('<div class=\"darzana-debug\">' + Debug.formatJSON(DATA) + '</div>');"
          "Debug.collapsible($('.darzana-debug'), 'Debug Infomation');</script>")))))

(defmacro wcar* [& body]
  "Redis context wrapper"
  `(car/wcar {:pool {} :spec {:host "127.0.0.1" :port 6379}} ~@body))

(defn set-application-scope [scope]
  (dosync
    (ref-set context/application-scope scope)))

(defn replace-url-variable [url context]
  (string/replace url #":([A-Za-z_]\w*)" #(get context (second %) )))

(defn build-url [context api]
  (str (replace-url-variable (api :url) context) 
    "?"
    (string/join "&"
      (map #(str (name %) "=" (get context (name %))) (:query-keys api)))))

(defn strip-content-type [f]
  (let [parts (string/split (if (nil? f) "" f) #"\s*;\s*")]
    (when (not (empty? parts))
      (first parts))))

(defn parse-response [response]
  (let [ content-type (strip-content-type (-> response :headers :content-type))
         body (response :body) ]
    (cond
      (empty? body) {}
      (re-find #"/xml$"  content-type) (.read (XMLSerializer.) body) 
      (re-find #"/json$" content-type) (json/read-str body)
      (re-find #"^text/plain$" content-type) body)))

(defn build-request-body [context api]
  (cond 
    (re-find #"/json$" (get api :content-type ""))
    (json/write-str (into {} (filter #(some (set (map name (api :query-keys))) %) context)))
    (not (= (api :method) :get))
    (string/join "&"
      (map #(str (name %) "=" (get context (name %))) (api :query-keys)))))
    
(defn execute-api [context api]
  (let [ url (build-url context api)
         cache (wcar* (car/get (str (api :name) "-" url)))]
    (clojure.tools.logging/info url)
    { :api api
      :url url
      :from-cache (not (empty? cache)) 
      :response (if cache
                  cache 
                  (http/request
                       { :url url
                         :method (get api :method :get)
                         :headers {"Content-Type" (get api :content-type "application/x-www-form-urlencoded")}
                         :body (build-request-body context api)} nil))}))

(defn find-api [apis name]
  (first
    (filter #(= name (get % :name)) apis)))

(defn cache-response [response cache-key api]
  (let [ expire (api :expire) ]
    (if (and (= (api :method) :get) expire) 
      (wcar*
        (car/set cache-key (.toString response))
        (if expire (car/expire cache-key expire))))))

(defn- call-api-internal [context apis]
  (for [result (doall (map #(execute-api (context/merge-scope context) %) apis))]
    (let [api (result :api)]
      (debug "API response" @(result :response))
      (if (result :from-cache) 
        (json/read-str (result :response)) ;; From Cache
        (let [ response (parse-response @(result :response))
               cache-key (str (api :name) "-" (get-in @(result :response) [:opts :url]))]
          (if (apply (api :success?) [@(result :response)])
            (do
              (cache-response response cache-key api)
              {:page {(name (api :name)) response}})
            { :error
              {(name (api :name))
                { "status"   (-> result :response deref :status)
                  "message" response}}}))))))

(defn call-api [context apis]
  (let [api-responses (call-api-internal context apis)]
    (assoc context :scope
      (reduce #(merge-with merge %1 %2) (context :scope) api-responses))))

(defmacro if-success [context success error]
  `(if (empty? (~context :error))
     (-> ~context ~success)
     (-> ~context ~error)))

(defn store-session [context session-key context-keys]
  (assoc-in context [:session-add-keys session-key] context-keys))

(defn- save-session [response context]
  (let [ session (->
                   (get-in context [:scope :session])
                   (#(reduce dissoc % (context :session-remove-keys)))
                   (#(reduce
                       (fn [m k] (apply assoc m k)) %
                       (for [[session-key context-keys] (context :session-add-keys)]
                         [session-key (context/find-in-scopes context context-keys)]))))]
    (if (empty? session)
      response
      (assoc response :session session))))

(defn render [context template]
  (-> (ring.util.response/response (.apply (.compile handlebars template) (context/merge-scope context)))
    (save-session context)))

(defn redirect [context url]
  (-> (ring.util.response/redirect url)
    (save-session context)))

(defmacro defmarga [method url & exprs ]
  `(~method ~url {:as request#}
     (-> (context/create-context request#) ~@exprs)))

(defn load-app-routes []
  (binding [*ns* (find-ns 'darzana.core)]
    (load-string
      (string/join " "
        (flatten 
          [ "(compojure/defroutes app-routes"
            (map #(slurp %) @darzana-routes) ")"])))))

(defn add-routes [route-path]
  (dosync (alter darzana-routes conj route-path)))

(defn load-routes []
  (defroutes routes
    (GET "/router/reload" []
      (do (load-routes) "reloaded."))
    darzana-template/routes
    darzana-router/routes
    (load-app-routes)
    (route/resources "/")
    (route/not-found "Not Found")))


