(ns darzana.template
  (:require [integrant.core :as ig]))

(defprotocol TemplateEngine
  (render-html [this ctx template-name]))

(defmethod ig/init-key :darzana/template [_ spec]
  )
