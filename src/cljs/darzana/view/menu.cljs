(ns darzana.view.menu
  (:require [jayq.core :as jq])
  (:use
    [darzana.model :only (Workspace WorkspaceList)]
    [darzana.global :only (app)]
    [darzana.i18n :only [t]]
    [jayq.core :only [$]]))

(def MenuView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-menu'/>")
      "events"
      (js-obj
        "change select[name=workspace]" "changeWorkspace"
        "click a.btn-add" "newWorkspace"
        "click a.btn-delete" "deleteWorkspace"
        "click a.btn-active" "activateWorkspace")

      "initialize"
      (fn []
        (this-as me
          (set! (.-workspace me) (aget (.-options me) "workspace"))
          (let [workspaceList (WorkspaceList.)]
            (.on workspaceList "reset"  (.-render me) me)
            (.on workspaceList "add"    (.-render me) me)
            (.on workspaceList "delete" (.-render me) me)
            (set! (.-workspaceList me) workspaceList)
            (.fetch workspaceList (js-obj "reset" true)))))

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
          (set! (.-containerBtn me) (.. me ($ ".container-btn") clone))
          (.. me ($ ".container-btn") empty)

          (-> me (.$ ".select-container") (.animate (js-obj "width" "50%") 1000))
          (let [ input ($ "<input type='text' name='name' class='form-control'/>")
                 form ($ "<form class='form-workspace-new'/>")]
            (.on form "submit" (.proxy js/jQuery (aget me "createWorkspace") me))
            (-> me
              (.$ ".container-btn")
              (.html (.append form input))
              (.css "width" "40px")
              (.animate
                (js-obj "width" "50%")
                (js-obj "duration" 1000
                  "complete" (fn [] (jq/trigger input "focus"))))))))

      "createWorkspace"
      (fn [event]
        (this-as me
          (let [workspace (Workspace.
                            (js-obj "name" (-> me (.$ ".form-workspace-new [name=name]") (.val))))]
            (try
              (-> me (.-workspaceList) (.add workspace))
              (.save workspace)
              (-> me (.$ ".container-btn") (.empty) (.append (.-containerBtn me)))
              (catch js/Error e (.log js/console (pr-str e))))
            false)))

      "activateWorkspace"
      (fn [event]
        (this-as me
          (if-let [ws (.. me -workspaceList (findWhere (clj->js { :name (.-workspace me)})))]
            (.save ws (clj->js {:active true}) 
              (clj->js
                {:success
                  (fn [model]
                    (.. me ($ ".text-workspace") (html (. model get "name"))))})))))
      
      "changeWorkspace"
      (fn [event]
        (.navigate app (.val ($ (.-currentTarget event))) (js-obj "trigger" true)))

      "deleteWorkspace"
      (fn [event]
        (this-as me
          (if-let [ws (.. me -workspaceList (findWhere (clj->js { :name (.-workspace me)})))]
            (.destroy ws
              (clj->js
                { :success
                  (fn []
                    (.navigate app (.val ($ (.-currentTarget event))) (js-obj "trigger" true)))}))))))))

