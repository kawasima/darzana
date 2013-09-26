(ns darzana.view.route
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Route RouteList)]
    [jayq.core :only ($)]))

(def RouteEditView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-route-edit'/>")
      "events" (js-obj
                 "click .btn-save" "save"
                 "click .btn-back" "back")
      "initialize"
      (fn []
        (this-as me
          (set! (.-model me)
            (Route. (js-obj
                      "id" (aget (.-options me) "id")
                      "workspace" (aget (.-options me) "workspace")
                      "router" (aget (.-options me) "router"))))
          (.. me -model (on "change" (.-render me) me))
          (.. me -model (on "invalid"
                          (fn [model error]
                            (.. me ($ ".label-comm-status") (label "danger" error)))
                          me))
          (set! (.-availableTemplates me)
            (TemplateList. (js-obj)
              (js-obj "url" (str "template/" (.. me -options -workspace)))))

          (set! (.-availableAPIs me) (APIList.))
          (.. me -availableTemplates
            (on "reset"
              (fn []
                (.. me -availableAPIs (fetch (js-obj "reset" true))))
              me))
          (.. me -availableAPIs (on "reset" (.-fetchRouter me) me))
          (.. me -availableTemplates (fetch (js-obj "reset" true)))))

      )))

