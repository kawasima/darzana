(ns darzana.boot
  (:use
    [darzana.router :only (Application)]
    [darzana.global :only (app)]))

(.registerHelper js/Handlebars "selected"
  (fn [foo bar]
    (if (= foo bar) "selected='selected'" "")))

(.config js/Handlebars.TemplateLoader (js-obj "prefix" "./hbs/"))

(.load js/Handlebars.TemplateLoader
  (array
    "menu"
    "route/index" "route/list" "route/edit" "route/new"
    "template/list" "template/edit" "template/new"
    )
  (js-obj
    "complete"
    (fn []
      (set! app (Application.))
      (.start Backbone.history (js-obj "pushState" false)))))

