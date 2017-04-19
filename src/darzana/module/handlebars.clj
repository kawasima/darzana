(ns darzana.module.handlebars
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [darzana.context :as context]
            [darzana.module.workspace :as workspace])
  (:import [com.github.jknack.handlebars Handlebars Handlebars$SafeString Handlebars$Utils Helper]
           [com.github.jknack.handlebars.io FileTemplateLoader]))


(defn- make-path
  ([workspace]
   (.. (io/file (workspace/current-dir workspace) "template") getPath))
  ([workspace ws]
   (.. (io/file (:workspace workspace) ws "template") getPath))
  ([workspace ws template]
   (.. (io/file (:workspace workspace) ws "template" template) getPath)))

(defn- register-helper [engine]
  (.registerHelper
   engine "debug"
   (reify Helper
     (apply [this context options]
       (Handlebars$SafeString.
        (str
         "<link rel=\"stylesheet\" href=\"/admin/css/debug.css\"/>"
         "<script src=\"/admin/js/debug.js\"></script>"
         "<scriptvar DATA="
         (json/write-str (.model (.context options))
                         :value-fn
                         (fn [k v]
                           (cond false nil
                                 :else v)))
         ";document.write('<div class=\"darzana-debug\">' + Debug.formatJSON(DATA) + '</div>');"
         "Debug.collapsible($('.darzana-debug'), 'Debug Information');</script>"))))))

(defrecord HandlebarsComponent [engine])

(defn render-html [{:keys [engine]} ctx template-name]
  (let [template (.compile engine template-name)]
    (.apply template (context/merge-scope ctx))))

(def default-options
  {:template-path "dev/resources/hbs"})

(defmethod ig/init-key :darzana.template/handlebars [_ {:keys [template-path]}]
  (let [engine (Handlebars. (FileTemplateLoader. template-path))]
    (register-helper engine)
    (map->HandlebarsComponent {:engine engine})))
