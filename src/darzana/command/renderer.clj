(ns darzana.command.renderer
  (:require [darzana.template :as template]))

(defn render [context {:keys [template]}]
  (let [template-engine (get-in context [:runtime :template])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (template/render-html template-engine context template)}))
