(ns darzana.template.freemarker
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]
            [clojure.java.data :refer [to-java]]
            [cheshire.core :as json]
            [clojure.walk :refer [stringify-keys]]
            [darzana.context :as context]
            [darzana.template :as template])
  (:import [java.io StringWriter]
           [freemarker.core HTMLOutputFormat]
           [freemarker.template Configuration Version]
           [freemarker.cache FileTemplateLoader]))

(defrecord FreemarkerComponent [config encoding]
  template/TemplateEngine
  (render-html [{:keys [config encoding]} ctx template-name]
    (let [template (.getTemplate config template-name encoding)
          writer   (StringWriter.)]
      (.process template (stringify-keys (context/merge-scope ctx)) writer)
      (.toString writer))))

(def default-options
  {:template-path "dev/resources/ftl"})

(defn- create-config [options]
  (let [config (Configuration. (Version. 2 3 27))
        dir (io/file (:template-path options))]
    (when (.exists dir)
      (doto config
        (.setTemplateLoader (FileTemplateLoader. dir))
        (.setOutputFormat HTMLOutputFormat/INSTANCE)))
    config))

(defmethod ig/init-key :darzana.template/freemarker [_ options]
  (map->FreemarkerComponent {:config (create-config options)
                             :encoding (:encoding options "UTF-8")}))

(defmethod ig/halt-key! :darzana.template/freemarker [_ {:keys [config]}]
  (.clearTemplateCache config))
