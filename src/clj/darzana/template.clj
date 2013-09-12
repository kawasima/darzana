(ns darzana.template
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.java.io :as io]
    [clojure.data.json :as json]))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))
 


(defroutes routes
  (compojure/context "/template" []
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map
                 (fn [_] (let [path (clojure.string/replace (.getPath _) #"^resources/template/" "")]
                           { :path path
                             :id   path }))
                  (walk "resources/template" #".*\.hbs")))})

    (GET "/*" { params :params }
      { :headers {"Content-Type" "application/json; charset=UTF-8"}
        :body (json/write-str
                { :path (params :*)
                  :hbs  (slurp  (str "resources/template/" (params :*)))})})

    (PUT "/*" { params :params })
    (POST "/" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             template-path (str "resources/template/" (get request-body "path") ".hbs")]
        (spit template-path ""))
      { :headers {"Content-Type" "application/json; charset=UTF-8"}})))
