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
        "click .btn-add"    "newWorkspace"
        "click .btn-delete" "deleteWorkspace"
        "click .btn-active" "activateWorkspace"
        "click .btn-merge"  "mergeWorkspace")

      "initialize"
      (fn []
        (this-as me
          (set! (. me -workspace) (.. me -options -workspace))
          (let [workspaceList (WorkspaceList.)]
            (. workspaceList on "reset"  (. me -render) me)
            (. workspaceList on "add"    (. me -render) me)
            (. workspaceList on "delete" (. me -render) me)
            (set! (. me -workspaceList) workspaceList)
            (. workspaceList fetch (clj->js {:reset true})))))

      "render"
      (fn []
        (this-as me
          (let [template-fn (.get Handlebars.TemplateLoader "menu")]
            (.html (.-$el me) (template-fn
                                (js-obj
                                  "current" (some #(when (. % -current) %)
                                              (.. me -workspaceList toJSON))
                                  "head"    (some #(when (. % -head) %)
                                              (.. me -workspaceList toJSON))
                                  "workspace"  (. me -workspace)
                                  "workspaces" (.. me -workspaceList toJSON))))
            (.. me -$el (tooltip (clj->js { :selector "[data-toggle=tooltip]"
                                           :container "body"}))))))

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
              (. workspace save (js-obj)
                (clj->js
                  { :success
                    (fn [model]
                      (. app navigate
                        (. model get "name")
                        (clj->js { :trigger true}))
                      )}))
              (catch js/Error e (.log js/console (pr-str e))))
            false)))

      "activateWorkspace"
      (fn [event]
        (this-as me
          (if-let [ws (.. me -workspaceList (findWhere (clj->js { :name (. me -workspace)})))]
            (. ws save (clj->js {:active true}) 
              (clj->js
                {:success
                  (fn [model]
                    (.. me
                      ($ ".text-workspace")
                      (html (. model get "name"))
                      (textillate "start"))
                    (.. me -workspaceList (fetch (clj->js {:reset true}))))})))))
      
      "changeWorkspace"
      (fn [event]
        (.navigate app
          (.val ($ (.-currentTarget event)))
          (clj->js {:trigger true})))

      "mergeWorkspace"
      (fn [event]
        (this-as me))

      "deleteWorkspace"
      (fn [event]
        (this-as me
          (if-let [ws (.. me -workspaceList (findWhere (clj->js { :name (. me -workspace)})))]
            (.destroy ws
              (clj->js
                { :success
                  (fn [model]
                    (-> me
                      (.$ (str "select[name=workspace] > option[value=" (. ws get "name") "]"))
                      (.remove))
                    (. app navigate
                      (-> me (.$ "select[name=workspace]") (.val))
                      (clj->js { :trigger true}))
                    )}))))))))

