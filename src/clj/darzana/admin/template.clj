(ns darzana.admin.template
  (:use
    [darzana.template]
    [compojure.core :as compojure :only (GET POST PUT DELETE ANY defroutes)])
  (:require
    [clojure.string :as string]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [darzana.workspace :as workspace]))

(defroutes routes
  (compojure/context "/template/:workspace" {{ws :workspace} :params}

    (GET "/*.hbs" {{ws :workspace template-name :*} :params}
      (let [ path (make-path ws (str template-name ".hbs"))]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str
                  { :id template-name
                    :path template-name
                    :workspace ws
                    :hbs  (slurp path)})}))

    (GET "/*" {{path :* mode :mode} :params}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map
                  (fn [file]
                    (let [ hbs-path (.getPath file)
                           name (clojure.string/replace hbs-path
                                  (re-pattern (str "^" (make-path ws path) "/(.*?)(\\.hbs)?$")) "$1")
                           id   (if (empty? path) name (str path "/" name))]
                      { :id   id
                        :path id
                        :name name
                        :workspace ws
                        :is_dir (.isDirectory file)
                        :lastModified (.lastModified file)
                        :size (.length file)}))
                  (sort-by
                    #(.getName %)
                    (if (= mode "tree")
                      (.listFiles (io/file (make-path ws) path))
                      (walk (io/file (make-path ws) path) #".*\.hbs")))))})

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
             dir? (request-body "is_dir")
             path (make-path ws (str (request-body "path") (if-not dir? ".hbs")))]
        (io/make-parents path)
        (if dir? 
          (.mkdir (io/file path))
          (do
            (spit path (get request-body "hbs" ""))
            (darzana.workspace/commit-workspace (request-body "workspace") "New template.")))
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str (assoc request-body "id" (request-body "path")))}))))

