(ns darzana.admin.api
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [clojure.data.json :as json]
    [darzana.api :as api]))

(defroutes routes
  (compojure/context "/api/:workspace" {{ws :workspace} :params}
    (GET "/" []
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] { :id _ :name _
                               :workspace ws}) @api/apis))})

    (GET "/*" {params :params}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (var-get (resolve (symbol (params :*))))
                :value-fn (fn [k v]
                              (if (fn? v) (str v) v)))})))

