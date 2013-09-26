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
  (.extend js/Backbone.Model
    (js-obj
      "urlRoot"
      (fn []
        (this-as me
          (str
            "router/"
            (.get me "workspace")
            "/"
            (.get me "router"))))
      "validate"
      (fn [attrs options]
        (this-as me
          (let [dom ($ (.parseXML $ (.-xml attrs)))]
            (if (not= (.. dom (find "xml > block") size) 1)
              "defmarga is only one.")))))))

(def RouteList
  (.extend js/Backbone.Collection
    (js-obj
      "model" Route)))

(def API
  (.extend js/Backbone.Model
    (js-obj
      "urlRoot" "api")))

(def APIList
  (.extend js/Backbone.Collection
    (js-obj
      "model" API
      "url" "api")))


