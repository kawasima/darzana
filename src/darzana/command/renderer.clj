(ns darzana.command.renderer
  (:require [darzana.template :as template]
            [darzana.context :as context]
            [cheshire.core :as json]))

(defn- render-template [context template]
  (let [template-engine (get-in context [:runtime :template])]
    (template/render-html template-engine context template)))

(defn- collect-values [context var-names]
  (let [var-names (if (seq? var-names) var-names [var-names])]
    (->> var-names
         (map (fn [k] [k (get-in context (into [:scope] (context/find-in-scopes context k)))]))
         (reduce #(assoc %1 (first %2) (second %2)) {}))))

(defn- render-json [context var-names]
  (json/generate-string (collect-values context var-names)))

(defn render [context {:keys [template format var status]
                       :or {status 200}}]
  (if template
    {:status status
     :headers {"Content-Type" "text/html"}
     :body (render-template context template)}
    (case format
      :json {:status status
             :headers {"Content-Type" "application/json"}
             :body (render-json context var)})))
