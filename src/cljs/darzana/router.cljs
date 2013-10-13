(ns darzana.router
  (:use
    [darzana.view.menu :only (MenuView)]
    [darzana.view.template :only (TemplateListView TemplateEditView)]
    [darzana.view.route :only (RouteView RouteEditView)]
    [darzana.view.api :only (APIListView)]
    [darzana.model :only (Workspace)]
    [jayq.core :only ($)]))

(def Application
  (. Backbone.Router extend
    (clj->js
      { :routes
        (clj->js
          { "" "menu"
            ":workspace" "menu"
            ":workspace/route" "routeIndex"
            ":workspace/route/:router" "routeIndex"
            ":workspace/route/:router/:id/edit" "routeEdit"
            ":workspace/template" "templateList"
            ":workspace/template/*path/edit" "templateEdit" })

        :initialize
        (fn []
          (this-as me
            (set! (.-currentView me) nil)))

        :menu
        (fn [workspace]
          (this-as me
            (.switchView me
              (MenuView.
                (js-obj "workspace"
                  (if (empty? workspace)
                    "master"
                    workspace))))))

        :routeIndex
        (fn [ws-name router]
          (this-as me
            (let [workspace (Workspace. (clj->js {:id ws-name}))]
              (. workspace fetch
                (clj->js
                  { :success
                    (fn [workspace]
                      (. me switchView
                        (RouteView.
                          (js-obj
                            "workspace" workspace
                            "router" router))))})))))

        :routeEdit
        (fn [ws-name router id]
          (this-as me
            (let [workspace (Workspace. (clj->js {:id ws-name}))]
              (. workspace fetch
                (clj->js
                  { :success
                    (fn [workspace]
                      (. me switchView
                        (RouteEditView.
                          (js-obj
                            "workspace" workspace
                            "router" router
                            "id" id))))})))))

        :templateList
        (fn [ws-name]
          (this-as me
            (let [workspace (Workspace. (clj->js {:id ws-name}))]
              (. workspace fetch
                (clj->js
                  { :success
                    (fn [workspace]
                      (. me switchView
                        (TemplateListView.
                          (js-obj "workspace" workspace))))})))))

        :templateEdit
        (fn [ws-name path]
          (this-as me
            (let [workspace (Workspace. (clj->js {:id ws-name}))]
              (. workspace fetch
                (clj->js
                  { :success
                    (fn [workspace]
                      (. me switchView
                        (TemplateEditView.
                          (js-obj "workspace" workspace "path" path))))})))))

        :switchView
        (fn [newView]
          (this-as me
            (when (. me -currentView) (.. me -currentView undelegateEvents))
            (set! (. me -currentView) newView)
            (if-not (.. me -currentView -$el parent (is "*"))
              (-> ($ "#content") (.empty) (.append (.. me -currentView -$el))))))})))

