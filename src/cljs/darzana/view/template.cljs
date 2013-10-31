(ns darzana.view.template
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Template TemplateList)]
    [darzana.i18n :only [t]]
    [jayq.core :only ($)])
  (:require
    [clojure.string :as string]))

(def TemplateListView
  (. Backbone.View extend
    (clj->js
      { :el ($ "<div id='page-template-list'/>")
        :events (js-obj
                  "submit #form-template-new" "createTemplate"
                  "click .btn-open-modal-copy" "openModal"
                  "click .btn-add"     "newTemplate"
                  "click .btn-delete"  "deleteTemplate"
                  "click .btn-copy"    "copyTemplate"
                  "click .btn-disp"    "changeDispMode"
                  "click .btn-file-type" "toggleFileType")
        :initialize
        (fn []
          (this-as me
            (if-let [mode (.. me -options -mode)]
              (set! (. me -mode) mode))
            (set! (. me -collection)
              (TemplateList. (js-obj)
                (js-obj
                  "url"
                  (str "template/"
                    (.. me -options -workspace -id)
                    "/" (.. me -options -path)))))
            (.. me -collection (on "reset"  (. me -render) me))
            (.. me -collection (on "add"    (. me -render) me))
            (.. me -collection (on "remove" (. me -render) me))
            (.. me -collection (fetch (clj->js { :reset true
                                                 :data { :mode (. me -mode)}})))))
        
        :render
        (fn []
          (this-as me
            (let [template-fn (. js/Handlebars.TemplateLoader get "template/list")]
              (.. me -$el (html (template-fn
                                  (clj->js
                                    { :templates (.. me -collection toJSON)
                                      :workspace (.. me -options -workspace toJSON)
                                      :mode      (.. me -mode)
                                      :path      (.. me -options -path)})))))))

        :newTemplate
        (fn [event]
          (this-as me
            (let [ template-fn (.get js/Handlebars.TemplateLoader "template/new")
                   $template ($ (template-fn (js-obj)))]
              (.. me ($ ".list-templates") (append $template))
              (.scrollTop ($ js/window) (.. $template offset -top)))))

        :toggleFileType
        (fn [event]
          (this-as me
            (let [btn ($ (. event -currentTarget))]
              (if (= (. btn val) "file")
                (.. btn (val "folder")
                  (find "i.fa") (removeClass "fa-file-o")   (addClass "fa-folder-o"))
                (.. btn (val "file")
                  (find "i.fa") (removeClass "fa-folder-o") (addClass "fa-file-o"))))))

        :createTemplate
        (fn [event]
          (this-as me
            (let [ form     ($ (. event -currentTarget))
                   template (Template.
                              (clj->js
                                { :name (.. form (find "[name=path]") val)
                                  :path (string/join "/"
                                          [(.. me -options -path) (.. form (find "[name=path]") val)])
                                  :workspace (.. me -options -workspace -id)
                                  :is_dir (= (.. form (find "[name=type]") val) "folder")})
                              (clj->js
                                { :url (str "template/" (.. me -options -workspace -id)) }))]
              (try
                (.. me -collection (add template))
                (.  template save
                  (clj->js
                    {:success
                      (fn [model] (.. form parent remove))}))
                (catch js/Error e (.log js/console (pr-str e))))))
          false)

        :openModal
        (fn [event]
          (this-as me
            (let [template-id (-> ($ (. event -currentTarget)) (.data "template-id") )]
              (-> me (.$ ":input[name=src_path]")   (.val template-id))
              (-> me (.$ ":input[name=dest_path]")  (.val (str template-id "~"))))))

        :copyTemplate
        (fn [event]
          (this-as me
            (let [ dest-path (.. me ($ ":input[name=dest_path]") val)
                   src-path  (.. me ($ ":input[name=src_path]")  val)
                   src-template (Template. (clj->js { :id src-path :path src-path
                                                      :name (last (string/split src-path #"/"))
                                                      :workspace (.. me -options -workspace -id)})) ]
              (. src-template fetch
                (clj->js
                  { :success
                    (fn [model]
                      (.. me -collection (add model))
                      (.  model save
                        (clj->js { :path dest-path :name (last (string/split dest-path #"/"))})
                        (clj->js { :success (. me -renderListItem) })))})))))

        :renderListItem
        (fn [model]
          (this-as me
            (let [ template-fn (. js/Handlebars.TemplateLoader get "template/_list_item") ]
              (.. me ($ "#modal-template-copy") (modal "hide"))
              (.. me ($ ".list-templates")
                (append ($ (template-fn (. model toJSON))))))))

        :changeDispMode
        (fn [event]
          (this-as me
            (let [mode (if (= "tree" (.. me -mode)) "list" "tree")]
              (.. js/window -localStorage (setItem "dispMode" mode))
              (set! (. me -mode) mode)
              (if (empty? (.. me -options -path))
                (.. me -collection (fetch (clj->js { :reset true
                                                     :data {:mode mode}})))
                (. app navigate (str (.. me -options -workspace -id) "/template")
                  (clj->js {:trigger true}))))))

        :deleteTemplate
        (fn [event]
          (this-as me
            (.. me
              -collection
              (get (. ($ (. event -currentTarget)) data "template-id"))
              destroy)))})))
  
(def TemplateEditView
  (. js/Backbone.View extend
    (clj->js
      { :el ($ "<div id='page-template-edit'/>")
        :events (js-obj
                  "click .btn-save" "save"
                  "click .btn-back" "back")
        :initialize
        (fn []
          (this-as me
            (set! (. me -model)
              (Template.
                (clj->js
                  { :id        (str (.. me -options -path) ".hbs")
                    :path      (.. me -options -path)
                    :workspace (.. me -options -workspace -id)})))

            (.. me -model (on "change" (. me -render) me))
            (.. me -model fetch)))

        :render
        (fn []
          (this-as me
            (let [template-fn (.get js/Handlebars.TemplateLoader "template/edit")]
              (.. me -$el (html (template-fn
                                  (clj->js
                                    { :template  (.. me -model toJSON)
                                      :workspace (.. me -options -workspace toJSON)})))))
            (set! (. me -codeMirror)
              (.fromTextArea js/CodeMirror
                (first (.$ me "textarea[name=hbs]"))
                (js-obj
                  "mode" "mustache"
                  "lineNumbers" true
                  "readOnly" (.. me -options -workspace (get "default")))))))

        :save
        (fn []
          (this-as me
            (.. me ($ ".label-comm-status") (label "info" (t :labels/saving)))
            (.. me -model
              (save "hbs" (.. me -codeMirror getValue)
                (clj->js
                  { :success
                    (fn [model]
                      (.. me ($ ".label-comm-status") (label "success" (t :labels/saved-successfully)))
                      (js/setTimeout
                        (fn [] (.. me ($ ".label-comm-status") (label "default" "")))
                        1500))

                    :error
                    (fn [model]
                      (.. me ($ ".label-comm-status") (label "error" (t :labels/failed)))
                      (js/setTimeout
                        (fn [] (.. me ($ ".label-comm-status") (label "default" "")))
                        1500))
                    })))))
        
        :back
        (fn []
          (this-as me
            (.. js/window -history back)))})))
