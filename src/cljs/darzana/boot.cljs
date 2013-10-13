(ns darzana.boot
  (:use
    [darzana.router :only (Application)]
    [darzana.global :only (app)]
    [darzana.i18n :only [t]]))

;;;
;;; Setting for handebars helpers
;;;
(.registerHelper js/Handlebars "selected"
  (fn [foo bar]
    (if (= foo bar) "selected='selected'" "")))

(.registerHelper js/Handlebars "t"
  (fn [key]
    (t (keyword key))))

(set! (.. js/jQuery -fn -label)
  (fn [type msg]
    (this-as me
      (.. me
        (removeClass "label-success label-info label-danger label-warning")
        (addClass    (str "label-" type))
        (text msg)))))

(. js/Handlebars.TemplateLoader config (js-obj "prefix" "./hbs/"))

(. js/Handlebars.TemplateLoader load
  (array
    "menu"
    "route/index" "route/list" "route/edit" "route/new"
    "template/list" "template/edit" "template/new" "template/_list_item"
    )
  (js-obj
    "complete"
    (fn []
      (set! app (Application.))
      (. Backbone.history start (clj->js { :pushState false })))))
