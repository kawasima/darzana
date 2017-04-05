(ns darzana.component.handlebars
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [darzana.context :as context]
            [darzana.component.workspace :as workspace])
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

(defrecord HandlebarsComponent [workspace]
  component/Lifecycle

  (start [component]
    (let [engine (Handlebars. (FileTemplateLoader. (:template-path component)))]
      (register-helper engine)
      (assoc component :engine engine)))

  (stop [component]
    (dissoc component :engine)))

(defn render-html [{:keys [engine]} ctx template-name]
  (let [template (.compile engine template-name)]
    (.apply template (context/merge-scope ctx))))

(def default-options
  {:template-path "dev/resources/hbs"})

(defn handlebars-component [options]
  (map->HandlebarsComponent (merge default-options options)))
