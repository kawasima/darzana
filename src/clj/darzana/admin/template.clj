(ns darzana.admin.template
  (:use
    [darzana.template]
    [compojure.core :as compojure :only (GET POST PUT DELETE ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [darzana.workspace :as workspace]))

(defroutes routes
  (compojure/context "/template/:workspace" {{ws :workspace} :params}
    (GET "/" []
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map
                  (fn [file]
                    (let [ path (.getPath file)
                           name (clojure.string/replace path
                                  (re-pattern (str "^" (make-path ws) "/(.*?)\\.hbs$")) "$1")]
                      { :path name
                        :id   name
                        :workspace ws
                        :lastModified (.lastModified file)
                        :size (.length file)}))
                  (walk (io/file (make-path ws)) #".*\.hbs")))})

    (GET "/*" {{ws :workspace template-name :*} :params}
      (let [ path (make-path ws (str template-name ".hbs"))]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str
                  { :id template-name
                    :path template-name
                    :workspace ws
                    :hbs  (slurp path)})}))

    (PUT "/*" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             path (make-path ws (str (request-body "path") ".hbs"))]
        (spit path (request-body "hbs"))
        (darzana.workspace/commit-workspace (request-body "workspace") "Modify template.")
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str {:status "successful"})}))

    (DELETE "/*" {params :params}
      (let [ path (str "template/" (params :*) ".hbs")]
        (darzana.workspace/delete-file ws path)
        (darzana.workspace/commit-workspace ws "Delete template.")
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str {:status "successful"})}))

    (POST "/" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             path (make-path ws (str (request-body "path") ".hbs"))]
        (io/make-parents path)
        (spit path (get request-body "hbs" ""))
        (darzana.workspace/commit-workspace (request-body "workspace") "New template.")
        { :headers {"Content-Type" "application/json; charset=UTF-8"}}))))

