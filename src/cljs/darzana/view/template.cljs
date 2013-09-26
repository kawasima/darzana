(ns darzana.view.template
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Template TemplateList)]
    [jayq.core :only ($)]))

(def TemplateListView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-template-list'/>")
      "events" (js-obj
                 "submit #form-template-new" "createTemplate"
                 "click .btn-add" "newTemplate"
                 "click a.btn-delete" "deleteTemplate")
      "initialize"
      (fn []
        (this-as me
          (set! (.-collection me)
            (TemplateList. (js-obj)
              (js-obj
                "url"
                (str "template/" (aget (.-options me) "workspace")))))
          (.. me -collection (on "reset"  (.-render me) me))
          (.. me -collection (on "add"    (.-render me) me))
          (.. me -collection (on "remove" (.-render me) me))
          (.. me -collection (fetch (js-obj "reset" true)))
          ))
      
      "render"
      (fn []
        (this-as me
          (let [template-fn (.get js/Handlebars.TemplateLoader "template/list")]
            (.. me -$el (html (template-fn
                                (js-obj
                                  "templates" (.. me -collection toJSON))))))))

      "newTemplate"
      (fn [event]
        (this-as me
          (let [ template-fn (.get js/Handlebars.TemplateLoader "template/new")
                 $template ($ (template-fn (js-obj)))]
            (.. me ($ ".list-templates") (append $template))
            (.scrollTop ($ js/window) (.. $template offset -top)))))

      "createTemplate"
      (fn [event]
        (this-as me
          (let [ template (Template. (js-obj
                                       "path"
                                       (.. me ($ "#form-template-new [name=path]") val)))]
            (try
              (.. me -collection (add template))
              (.save template)
              (.. ($ "#form-template-new") parent remove)
              (catch js/Error e (.log js/console (pr-str e))))))
        false)

      "deleteTemplate"
      (fn [event]
        (this-as me
          (.. me
            -collection
            (at (.data ($ (.-currentTarget event)) "template-id"))
            destroy))))))
  
(def TemplateEditView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-template-edit'/>")
      "events" (js-obj
                 "click .btn-save" "save"
                 "click .btn-back" "back")
      "initialize"
      (fn []
        (this-as me
          (set! (.-model me)
            (Template. (js-obj
                         "id" (aget (.-options me) "path")
                         "path" (aget (.-options me) "path")
                         "workspace" (aget (.-options me) "workspace"))))
          (.. me -model (on "change" (.-render me) me))
          (.. me -model fetch)))

      "render"
      (fn []
        (this-as me
          (let [template-fn (.get js/Handlebars.TemplateLoader "template/edit")]
            (.. me -$el (html (template-fn (.. me -model toJSON)))))
          (set! (.-codeMirror me )
            (.fromTextArea js/CodeMirror
              (first (.$ me "textarea[name=hbs]"))
              (js-obj
                "mode" "mustache"
                "lineNumbers" true)))))

      "save"
      (fn []
        (this-as me
          (.. me -model
            (save "hbs" (.. me -codeMirror getValue)
              (js-obj
                "success"
                (fn [model]
                  (.. me
                    ($ ".label-comm-status")
                    (removeClass "label-info")
                    (addClass "label-success")
                    (text "Saved!"))
                  (.setTimeout
                    (fn [] (.. me
                             ($ ".label-comm-status")
                             (removeClass "label-success")
                             (text "")))
                    1500))
                
                "error"
                (fn [model]
                  (.. me
                    ($ ".label-comm-status")
                    (removeClass "label-info")
                    (addClass "label-error")
                    (text "Save failed!"))
                  (.setTimeout
                    (fn [] (.. me
                             ($ ".label-comm-status")
                             (removeClass "label-error")
                             (text "")))
                    1500)))))
          (.. me ($ ".label-comm-status") (addClass "label-info") (text "Saving..."))))
      
      "back"
      (fn []
        (this-as me
          (.navigate app
            (str (aget (.-options me) "workspace") "/template")
            (js-obj
              "trigger" true)))))))
