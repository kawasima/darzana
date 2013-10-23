(ns darzana.boot
  (:use
    [darzana.global :only (app)]
    [darzana.router :only (Application)]
    [darzana.i18n :only [t]]))

;;;
;;; Setting for handebars helpers
;;;
(. js/Handlebars registerHelper "include"
  (fn [options]
    (this-as me
      (let [ context (js-obj)
             mergeContext (fn [obj] (doseq [k (keys (js->clj obj))]
                                      (aset context k (aget obj k))))]
        (mergeContext me)
        (mergeContext (. options -hash))
        (. options fn context)))
    ))

(. js/Handlebars registerHelper "selected"
  (fn [foo bar]
    (if (= foo bar) "selected='selected'" "")))

(. js/Handlebars registerHelper "if-eq"
  (fn [a b block]
    (if (= a b) (. block fn) (. block inverse) )))

(. js/Handlebars registerHelper "if-neq"
  (fn [a b block]
    (if-not (= a b) (. block fn) (. block inverse) )))

(. js/Handlebars registerHelper "t"
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
    "api/list" "api/show"
    )
  (js-obj
    "complete"
    (fn []
      (set! app (Application.))
      (. Backbone.history start (clj->js { :pushState false })))))

