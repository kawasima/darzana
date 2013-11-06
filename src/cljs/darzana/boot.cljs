(ns darzana.boot
  (:use
    [darzana.global :only (app)]
    [darzana.router :only (Application)]
    [darzana.i18n :only [t]])
  (:require
    [clojure.string :as string]))

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
    (this-as me
      (if (= a b) (. block fn me) (. block inverse me)))))

(. js/Handlebars registerHelper "if-neq"
  (fn [a b block]
    (this-as me
      (if-not (= a b) (. block fn me) (. block inverse me)))))

(. js/Handlebars registerHelper "breadcrumb"
  (fn [workspace path]
    (new js/Handlebars.SafeString
      (let [paths (string/split path #"/")]
        (string/join
          (flatten
            [ (for [n (range 1 (count paths))]
                (apply str
                  "<li><a href=\"#" workspace "/template/"
                  (string/join "/" (take n paths))
                  "\">" (nth paths (dec n)) "</a></li>"))
              (str "<li class=\"active\">" (last paths) "</li>")]))))))

(. js/Handlebars registerHelper "keywordToName"
  (fn [key]
    (.substring key 1)))

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

