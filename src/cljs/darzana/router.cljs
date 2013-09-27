(ns darzana.router
  (:use
    [darzana.view.menu :only (MenuView)]
    [darzana.view.template :only (TemplateListView TemplateEditView)]
    [darzana.view.route :only (RouteView RouteEditView)]
    [jayq.core :only ($)]))

(def Application
  (.extend Backbone.Router
    (js-obj
      "routes"
      (js-obj
        "" "menu"
        ":workspace" "menu"
        ":workspace/route" "routeIndex"
        ":workspace/route/:router" "routeIndex"
        ":workspace/route/:router/:id/edit" "routeEdit"
        ":workspace/template" "templateList"
        ":workspace/template/*path/edit" "templateEdit")

      "initialize"
      (fn []
        (this-as me
          (set! (.-currentView me) nil)))

      "menu"
      (fn [workspace]
        (this-as me
          (.switchView me
            (MenuView.
              (js-obj "workspace"
                (if (empty? workspace)
                  "master"
                  workspace))))))

      "routeIndex"
      (fn [workspace router]
        (this-as me
          (.switchView me
            (RouteView.
              (js-obj
                "workspace" workspace
                "router" router)))))

      "routeEdit"
      (fn [workspace router id]
        (this-as me
          (.switchView me
            (RouteEditView.
              (js-obj
                "workspace" workspace
                "router" router
                "id" id)))))

      "templateList"
      (fn [workspace]
        (this-as me
          (.switchView me (TemplateListView. (js-obj "workspace" workspace)))))

      "templateEdit"
      (fn [workspace path]
        (this-as me
          (.switchView me (TemplateEditView. (js-obj "workspace" workspace "path" path)))))

      "switchView"
      (fn [newView]
        (this-as me
          (if-not (nil? (.-currentView me)) (.. me -currentView remove))
          (set! (.-currentView me) newView)
          (if-not (.. me -currentView -$el parent (is "*"))
            (-> ($ "#content") (.append (.. me -currentView -$el)))))))))

