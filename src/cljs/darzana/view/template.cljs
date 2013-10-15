(ns darzana.view.template
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Template TemplateList)]
    [jayq.core :only ($)]))

(def TemplateListView
  (. Backbone.View extend
    (js-obj
      "el" ($ "<div id='page-template-list'/>")
      "events" (js-obj
                 "submit #form-template-new" "createTemplate"
                 "click .btn-open-modal-copy" "openModal"
                 "click .btn-add"     "newTemplate"
                 "click .btn-delete" "deleteTemplate"
                 "click .btn-copy"   "copyTemplate")
      "initialize"
      (fn []
        (this-as me
          (set! (.-collection me)
            (TemplateList. (js-obj)
              (js-obj
                "url"
                (str "template/" (.. me -options -workspace -id)))))
          (.. me -collection (on "reset"  (.-render me) me))
          (.. me -collection (on "add"    (.-render me) me))
          (.. me -collection (on "remove" (.-render me) me))
          (.. me -collection (fetch (clj->js { :reset true })))
          ))
      
      "render"
      (fn []
        (this-as me
          (let [template-fn (. js/Handlebars.TemplateLoader get "template/list")]
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
          (let [ template (Template.
                            (clj->js
                              { :path (.. me ($ "#form-template-new [name=path]") val)
                                :workspace (.. me -options -workspace -id)})
                            (clj->js
                              { :url (str "template/" (.. me -options -workspace -id)) }))]
            (try
              (.. me -collection (add template))
              (.  template save)
              (.. ($ "#form-template-new") parent remove)
              (catch js/Error e (.log js/console (pr-str e))))))
        false)

      "openModal"
      (fn [event]
        (this-as me
          (let [template-id (-> ($ (. event -currentTarget)) (.data "template-id") )]
            (-> me (.$ ":input[name=src_path]")   (.val template-id))
            (-> me (.$ ":input[name=dest_path]")  (.val (str template-id "~"))))))

      "copyTemplate"
      (fn [event]
        (this-as me
          (let [ dest-path (.. me ($ ":input[name=dest_path]") val)
                 src-path  (.. me ($ ":input[name=src_path]")  val)
                 src-template (Template. (clj->js { :id src-path :path src-path
                                                    :workspace (.. me -options -workspace -id)})) ]
            (. src-template fetch
              (clj->js
                { :success
                  (fn [model]
                    (.. me -collection (add model))
                    (.  model save
                      (clj->js { :path dest-path})
                      (clj->js { :success (. me -renderListItem) })))})))))

      "renderListItem"
      (fn [model]
        (this-as me
          (let [ template-fn (. js/Handlebars.TemplateLoader get "template/_list_item") ]
            (.. me ($ "#modal-template-copy") (modal "hide"))
            (.. me ($ ".list-templates")
              (append ($ (template-fn (. model toJSON))))))))

      "deleteTemplate"
      (fn [event]
        (this-as me
          (.. me
            -collection
            (get (.data ($ (. event -currentTarget)) "template-id"))
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
                         "id"        (.. me -options -path)
                         "path"      (.. me -options -path)
                         "workspace" (.. me -options -workspace -id))))
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
                  (js/setTimeout
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
                  (js/setTimeout
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
            (str (.. me -options -workspace -id) "/template")
            (js-obj
              "trigger" true)))))))
