(ns darzana.template.handlebars
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.java.data :refer [to-java]]
            [cheshire.core :as json]
            [clojure.walk :refer [stringify-keys]]
            [darzana.context :as context]
            [darzana.template :as template])
  (:import [com.github.jknack.handlebars Handlebars Handlebars$SafeString Handlebars$Utils Helper]
           [com.github.jknack.handlebars.io FileTemplateLoader]))

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
         (json/generate-string (.model (.context options))
                               :value-fn
                               (fn [k v]
                                 (cond false nil
                                       :else v)))
         ";document.write('<div class=\"darzana-debug\">' + Debug.formatJSON(DATA) + '</div>');"
         "Debug.collapsible($('.darzana-debug'), 'Debug Information');</script>"))))))

(defrecord HandlebarsComponent [engine]
  template/TemplateEngine
  (render-html [{:keys [engine]} ctx template-name]
    (let [template (.compile engine template-name)]
      (.apply template (stringify-keys (context/merge-scope ctx))))))

(def default-options
  {:template-path "dev/resources/hbs"})

(defmethod ig/init-key :darzana.template/handlebars [_ {:keys [template-path]}]
  (let [engine (Handlebars. (FileTemplateLoader. template-path))]
    (register-helper engine)
    (map->HandlebarsComponent {:engine engine})))
