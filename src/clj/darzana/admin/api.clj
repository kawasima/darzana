(ns darzana.admin.api
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)]
    [darzana.api])
  (:require
    [clojure.data.json :as json]
    [org.httpkit.client :as http]
    [darzana.context :as context]))

(defn- process-query-keys [pairs]
  (let [ctx (context/create-context {})]
    (map (fn [[k v]]
         [(if (keyword? k)
            (context/find-in-scopes ctx k) k) (str v)]) pairs)))

(defroutes routes
  (compojure/context "/api/:workspace" {{ws :workspace} :params}
    (GET "/" []
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] { :id _ :name _
                               :workspace ws}) @apis))})

    (GET "/*" {params :params}
      (let [ api (-> (var-get (resolve (symbol (params :*))))
                   (update-in [:query-keys] process-query-keys)) ]
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str api
                  :value-fn (fn [k v]
                              (cond
                                (fn? v) (str v)
                                :else v)))}))

    (POST "/*" {params :params :as req}
      (let [ ctx (context/create-context req)
             api (var-get (resolve (symbol (params :*))))
             url (build-url ctx api)
             response @(http/request
                         (build-request {:url url :method (get api :method :get)} ctx api) nil)
             response-body (parse-response response)]
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str response-body
                  :value-fn
                  (fn [k v]
                    (cond (= (type v) net.sf.json.JSONNull) nil
                      :else v)))}))))

