(ns darzana.view.route
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Route RouteList TemplateList APIList)]
    [darzana.block :only (apiDropdown templateDropdown)]
    [darzana.i18n :only [t]]
    [jayq.core :only ($)])
  (:require
    [Blockly :as Blockly])
  (:use-macros
    [jayq.macros :only [let-ajax let-deferred]]))

(declare RouteListView)

(def RouteView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-route'/>")
      "events" (js-obj
                 "change select[name=router]" "fetchRouter")
      "initialize"
      (fn []
        (this-as me
          (let-ajax [data { :url (str "router/" (.. me -options -workspace -id)) }]
            (-> me
              (.$ "select[name=router]")
              (.append
                (.map js/_ data
                  (fn [routerFile]
                    (-> ($ "<option/>")
                      (.text (.replace routerFile #"\.clj$" "")))))))
            (if-let [one-route (.. me ($ "select[name=router] option:not(:empty)") first)] 
              (set! (.. me -options -router) (. one-route val)))
            (if-let [router (.. me -options -router)]
              (-> me
                (.$ "select[name=router]")
                (.val router)
                (.trigger "change"))))
          (.render me)))

      "render"
      (fn []
        (this-as me
          (let [template-fn (.get js/Handlebars.TemplateLoader "route/index")]
            (.. me -$el (html (template-fn (js-obj)))))))

      "fetchRouter"
      (fn [event]
        (this-as me
          (let [router (.val ($ (. event -currentTarget)))]
            (when router
              (.navigate app (str "#" (.. me -options -workspace -id) "/route/" router))
              (new RouteListView (js-obj
                                   "router" router
                                   "workspace" (.. me -options -workspace))))))))))

(def RouteListView
  (.extend js/Backbone.View
    (js-obj
      "el" "#list-route"
      "events" (js-obj
                 "submit #form-route-new" "createRoute"
                 "click .btn-add" "newRoute"
                 "click a.btn-delete" "deleteRoute")
      "initialize"
      (fn []
        (this-as me
          (set! (.-collection me)
            (RouteList. (js-obj)
              (js-obj
                "url" (str
                        "router/"
                        (.. me -options -workspace -id)
                        "/"
                        (.. me -options -router)))))
          (.. me -collection (on "reset"  (.-render me) me))
          (.. me -collection (on "add"    (.-render me) me))
          (.. me -collection (on "remove" (.-render me) me))
          (.. me -collection (fetch (js-obj "reset" true)))))

      "render"
      (fn []
        (this-as me
          (let [ template-fn (. js/Handlebars.TemplateLoader get "route/list")]
            (.. me -$el (html (template-fn
                                (clj->js
                                  { :routes    (.. me -collection toJSON)
                                    :workspace (.. me -options -workspace toJSON)
                                    :router    (.. me -options -router) })))))))

      "newRoute"
      (fn []
        (this-as me
          (let [ template-fn (. js/Handlebars.TemplateLoader get "route/new")
                 route ($ (template-fn (js-obj)))]
            (.. me ($ ".list-routes") (append route))
            (.. ($ js/window) (scrollTop (.. route offset -top))))))

      "createRoute"
      (fn []
        (this-as me
          (let [route (Route.
                        (clj->js
                          { :method (-> me (.$ "#form-route-new [name=route-method]") (.val))
                            :path   (-> me (.$ "#form-route-new [name=route-path]") (.val))
                            :router (.. me -options -router)
                            :workspace (.. me -options -workspace -id)
                            :xml "<xml><block type=\"marga\"></block></xml>"}))]
            (try
              (.. me -collection (add route))
              (.  route save)
              (.. me ($ "#form-route-new") parent remove)
              (catch js/Error e (.log js/console e)))
            false)))

      "deleteRoute"
      (fn [event]
        (this-as me
          (.. me -collection
            (at (.. ($ (. event -currentTarget)) (data "route-id")))
            destroy))))))

(def RouteEditView
  (. js/Backbone.View extend
    (clj->js
      { :el ($ "<div id='page-route-edit'/>")
        :events (js-obj
                 "click .btn-save" "save"
                 "click .btn-back" "back")
        :initialize
        (fn []
          (this-as me
            (set! (.-model me)
              (Route. (js-obj
                        "id"        (.. me -options -id)
                        "workspace" (.. me -options -workspace -id)
                        "router"    (.. me -options -router))))
            (.. me -model (on "change" (.-render me) me))
            (.. me -model (on "invalid"
                            (fn [model error]
                              (.. me ($ ".label-comm-status") (label "danger" error)))
                            me))
            (set! (. me -availableTemplates)
              (TemplateList. (js-obj)
                (clj->js
                  { :url (str "template/" (.. me -options -workspace -id))})))

            (set! (. me -availableAPIs)
              (APIList. (js-obj)
                (clj->js
                  { :url (str "api/" (.. me -options -workspace -id))})))

            (let-deferred
              [ apis      (.. me -availableAPIs      (fetch (clj->js {:reset true})))
                templates (.. me -availableTemplates (fetch (clj->js {:reset true})))]
              (.. me -model fetch))))

        :render
        (fn []
          (this-as me
            (let [ template-fn (.get js/Handlebars.TemplateLoader "route/edit")]
              (.. me -$el (html (template-fn
                                  (clj->js { :workspace (.. me -options -workspace toJSON)})))))
            (def templateDropdown
              (fn []
                (new js/Blockly.FieldDropdown
                  (.map js/_
                    (.. me -availableTemplates toJSON)
                    (fn [hbs] (clj->js [(. hbs -id) (. hbs -path)]))))))
            (def apiDropdown
              (fn []
                (new js/Blockly.FieldDropdown
                  (.map js/_
                    (.. me -availableAPIs toJSON)
                    (fn [api] (array (aget api "id") (aget api "name")))))))
            (. js/Blockly inject 
              (.getElementById js/document "marga-blockly")
              (clj->js
                { :path     "./"
                  :toolbox  (.getElementById js/document "marga-toolbox")
                  :trashcan false
                  :readOnly (.. me -options -workspace (get "default"))
                  :collapse true
                  :scrollbars false }))
            (.domToWorkspace js/Blockly.Xml
              (aget js/Blockly "mainWorkspace")
              (.textToDom js/Blockly.Xml (.. me -model (get "xml"))))))

        :save
        (fn []
          (this-as me
            (let [xml (. Blockly.Xml workspaceToDom Blockly.mainWorkspace)]
              (.. me ($ ".label-comm-status") (label "info" (t :labels/saving)))
              (.. me -model
                (save "xml" (. Blockly.Xml domToText xml)
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
                          1500))}))))))

        :back
        (fn []
          (this-as me
            (.navigate app
              (str
                (.. me -options -workspace -id)
                "/route/"
                (.. me -options -router))
              (clj->js {:trigger true}))))

        })))

