(ns darzana.view.api
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (API APIList)]
    [darzana.debug :only (formatJSON)]
    [jayq.core :only ($)])
  (:use-macros [jayq.macros :only [let-ajax]]))

(def APIListView
  (.extend js/Backbone.View
    (clj->js
      { :el ($ "<div id='page-api-list'/>") 
        :events (js-obj)
        :initialize
        (fn []
          (this-as me
            (set! (. me -collection)
              (APIList. (js-obj)
                (js-obj
                  "url"
                  (str "api/" (.. me -options -workspace -id)))))
            (.. me -collection (on "reset"  (. me -render) me))
            (.. me -collection (on "add"    (. me -render) me))
            (.. me -collection (on "remove" (. me -render) me))
            (.. me -collection (fetch (clj->js { :reset true })))
            ))

        :render
        (fn []
          (this-as me
            (let [template-fn (. js/Handlebars.TemplateLoader get "api/list")]
              (.. me -$el (html (template-fn
                                  (clj->js
                                    { :workspace (.. me -options -workspace)
                                      :apis (.. me -collection toJSON) })))))))})))


(def APIShowView
  (.extend js/Backbone.View
    (clj->js
      { :el ($ "<div id='page-api-show'/>") 
        :events (js-obj
                  "click .btn-back" "back"
                  "click .btn-execute" "exec")
        :initialize
        (fn []
          (this-as me
            (set! (.-model me)
              (API. (js-obj
                           "id"        (.. me -options -name)
                           "workspace" (.. me -options -workspace toJSON))))
            (.. me -model (on "change" (.-render me) me))
            (.. me -model fetch)))

        :render
        (fn []
          (this-as me
            (let [template-fn (. js/Handlebars.TemplateLoader get "api/show")]
              (.. me -$el (html (template-fn
                                  (clj->js
                                    { :workspace (.. me -options -workspace)
                                      :api (.. me -model toJSON) })))))))
        :exec
        (fn []
          (this-as me
            (.. me ($ ".btn-execute") (addClass ".disabled"))
            (.. me ($ ".api-execute-result") (html ($ "<img src=\"img/loader.gif\"/>")))
            (let-ajax [data { :url (str "api/"
                                     (.. me -options -workspace -id)
                                     "/"
                                     (.. me -model -id))
                              :data (.. me ($ ".form-api") serialize)
                              :method "post"}]
              (.. me ($ ".btn-execute") (addClass ".disabled"))
              (.. me ($ ".api-execute-result") (html (formatJSON data))))))

        :back
        (fn []
          (this-as me
            (. app navigate
              (str (.. me -options -workspace -id) "/api")
              (clj->js { :trigger true}))))
        })))

