(ns darzana.admin.api
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [clojure.data.json :as json]
    [darzana.api :as api]))

(defroutes routes
  (compojure/context "/api" []
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] {:id _ :name _}) @api/apis))})))

