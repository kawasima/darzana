(ns darzana.template
  (:use
    [compojure.core :as compojure :only (GET POST PUT DELETE ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [darzana.workspace :as workspace])
  (:import
    [com.github.jknack.handlebars Handlebars Handlebars$SafeString Handlebars$Utils]
    [com.github.jknack.handlebars Helper]
    [com.github.jknack.handlebars.io FileTemplateLoader]))

(defn make-path
  ([] (.. (io/file (workspace/current-dir) "template") getPath))
  ([ws] (.. (io/file (@workspace/config :workspace) ws "template")  getPath))
  ([ws template]
    (.. (io/file (@workspace/config :workspace) ws "template" template) getPath)))

(def handlebars (ref (Handlebars. (FileTemplateLoader. (make-path)))))

(defn refresh-loader []  (. @handlebars with (into-array [(FileTemplateLoader. (make-path))])))

(.registerHelper @handlebars "debug"
  (reify Helper
    (apply [this context options]
      (Handlebars$SafeString.
        (str
          "<link rel=\"stylesheet\" href=\"/css/debug.css\"/>"
          "<script src=\"/js/debug.js\"></script>"
          "<script>var DATA="
          (json/write-str (.model (.context options))
            :value-fn
            (fn [k v]
              (cond (= (type v) net.sf.json.JSONNull) nil
                :else v)))
          ";document.write('<div class=\"darzana-debug\">' + Debug.formatJSON(DATA) + '</div>');"
          "Debug.collapsible($('.darzana-debug'), 'Debug Infomation');</script>")))))

(defn walk [dir pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq dir))))

(dosync (alter workspace/config update-in [:hook :change] conj
          refresh-loader))

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
