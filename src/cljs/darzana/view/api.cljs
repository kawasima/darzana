(ns darzana.view.template
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
            ))

        :render
        (fn []
          (this-as me
            (let [template-fn (.get js/Handlebars.TemplateLoader "api/list")]))})))

