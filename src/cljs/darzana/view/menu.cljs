(ns darzana.view.menu
  (:use
    [darzana.model :only (Workspace WorkspaceList)]
    [darzana.global :only (app)]
    [jayq.core :only [$]]))

(def MenuView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-menu'/>")
      
      "events"
      (js-obj
        "change select[name=workspace]" "changeWorkspace"
        "click a.btn-add" "newWorkspace")

      "initialize"
      (fn []
        (this-as me
          (set! (.-workspace me) (aget (.-options me) "workspace"))
          (let [workspaceList (WorkspaceList.)]
            (.on workspaceList "reset"  (.-render me) me)
            (.on workspaceList "add"    (.-render me) me)
            (.on workspaceList "delete" (.-render me) me)
            (set! (.-workspaceList me) workspaceList)
            (.fetch workspaceList (js-obj :reset true)))
          ))

      "render"
      (fn []
        (this-as me
          (let [template-fn (.get Handlebars.TemplateLoader "menu")]
            (.html (.-$el me) (template-fn
                                (js-obj
                                  "current" (some #(when (aget % "current") %)
                                              (.. me -workspaceList toJSON))
                                  "workspace"  (.-workspace me)
                                  "workspaces" (.. me -workspaceList toJSON)))))))

      "newWorkspace"
      (fn [event]
        (this-as me
          (set! (.-btnAdd me) (.. me ($ ".container-btn > a.btn-add") (remove)))
          (-> me (.$ ".select-container") (.animate (js-obj "width" "50%") 1000))
          (let [ input ($ "<input type='text' name='name' class='form-control'/>")
                 form ($ "<form class='form-workspace-new'/>")]
            (.on form "submit" (.proxy $ (.-createWorkspace me) me))
            (-> me
              (.$ "container-btn")
              (.append (.append form input))
              (.css (js-obj "width" "40px"))
              (.animate (js-obj "width" "50%") 1000 (fn [] (.focus input)))))))

      "createWorkspace"
      (fn [event]
        (this-as me
          (let [workspace (Workspace.
                            (js-obj "name" (-> me (.$ ".form-workspace-new [name=name]") (.val))))]
            (try
              (do
                (-> me (.-workspaceList) (.add workspace))
                (.save workspace)
                (-> me (.$ ".container-btn-add") (.empty) (.append (.-btnAdd me))))
              (catch js/Error e (.log js/console (pr-str e)))))))

      "changeWorkspace"
      (fn [event]
        (.navigate app (.val ($ (.-currentTarget event))) (js-obj "trigger" true)))
      )))
