(ns darzana.command.renderer
  (:require [darzana.module.handlebars :as handlebars]))

(defn render [context {:keys [template]}]
  (let [handlebars (get-in context [:runtime :handlebars])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (handlebars/render-html handlebars context template)}))
