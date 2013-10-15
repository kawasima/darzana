(ns darzana.view.api
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (API APIList)]
    [jayq.core :only ($)]))

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
                  "click .btn-back" "back")
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
        :back
        (fn []
          (this-as me
            (. app navigate
              (str (.. me -options -workspace -id) "/api")
              (clj->js { :trigger true}))))
        })))

