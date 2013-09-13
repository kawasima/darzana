(ns darzana.template
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.java.io :as io]
    [clojure.data.json :as json]))

(def config (ref {:root "resources/template"}))

(defn make-path [template]
  (str (get @config :root) "/" template))


(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))

(defroutes routes
  (compojure/context "/template" []
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map
                 (fn [_] (let [ template-path (.getPath _)
                                name (clojure.string/replace template-path
                                       (re-pattern (str "^" (get @config :root) "/(.*?)\\.hbs$")) "$1")]
                           { :path name
                             :id   name
                             :lastModified (.lastModified (io/file template-path))
                             :size (.length (io/file template-path))}))
                  (walk (get @config :root) #".*\.hbs")))})

    (GET "/*" {params :params}
      (let [ template-path (make-path (str (params :*) ".hbs"))]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str
                  { :id (params :*)
                    :path (params :*)
                    :hbs  (slurp template-path)})}))

    (PUT "/*" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             template-path (make-path (str (get request-body "path") ".hbs"))]
        (spit template-path (get request-body "hbs"))
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str {:status "successful"})}))

    (POST "/" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             template-path (make-path (str (get request-body "path") ".hbs"))]
        (spit template-path ""))
      { :headers {"Content-Type" "application/json; charset=UTF-8"}})))
