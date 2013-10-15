(ns darzana.model
  (:use [jayq.core :only ($)]))

(def Workspace
  (.extend js/Backbone.Model
    (js-obj
      "urlRoot" "workspace")))

(def WorkspaceList
  (.extend js/Backbone.Collection
    (js-obj
      "model" Workspace
      "url" "workspace")))

(def Template
  (.extend js/Backbone.Model
    (js-obj
      "urlRoot"
      (fn [] (this-as me
               (str "template/" (.get me "workspace")))))))

(def TemplateList
  (.extend js/Backbone.Collection
    (js-obj
      "model" Template)))

(def Route
  (. js/Backbone.Model extend
    (clj->js
      { :urlRoot
        (fn []
          (this-as me
            (str
              "router/"
              (. me get "workspace")
              "/"
              (. me get "router"))))
        :validate
        (fn [attrs options]
          (this-as me
            (let [dom ($ (. js/jQuery parseXML (. attrs -xml)))]
              (if (not= (.. dom (find "xml > block") size) 1)
                "defmarga is only one."))))})))

(def RouteList
  (.extend js/Backbone.Collection
    (js-obj
      "model" Route)))

(def API
  (.extend js/Backbone.Model
    (clj->js
      { :urlRoot
        (fn [] (this-as me
                 (str "api/" (.. me (get "workspace") -id))))})))

(def APIList
  (.extend js/Backbone.Collection
    (js-obj
      "model" API
      "url" "api")))


