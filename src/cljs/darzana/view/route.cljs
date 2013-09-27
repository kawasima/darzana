(ns darzana.view.route
  (:use
    [darzana.global :only (app)]
    [darzana.model :only (Route RouteList TemplateList APIList)]
    [darzana.block :only (apiDropdown templateDropdown)]
    [jayq.core :only ($)])
  (:use-macros
    [jayq.macros :only [let-ajax]]))

(def RouteListView)

(def RouteView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-route'/>")
      "events" (js-obj
                 "change select[name=router]" "fetchRouter")
      "initialize"
      (fn []
        (this-as me
          (let-ajax [data { :url (str "router/" (aget (.-options me) "workspace")) }]            
            (-> me
              (.$ "select[name=router]")
              (.append
                (.map js/_ data
                  (fn [routerFile]
                    (-> ($ "<option/>")
                      (.text (.replace routerFile #"\.clj$" "")))))))
            (if-let [router (aget (.-options me) "router")]
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
          (let [router (.val ($ (aget event "target")))]
            (when router
              (.navigate app (str "#" (aget (.-options me) "workspace") "/route/" router))
              (new RouteListView (js-obj
                                   "router" router
                                   "workspace" (aget (.-options me) "workspace"))))))))))

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
                        (aget (.-options me) "workspace")
                        "/"
                        (aget (.-options me) "router")))))
          (.. me -collection (on "reset"  (.-render me) me))
          (.. me -collection (on "add"    (.-render me) me))
          (.. me -collection (on "remove" (.-render me) me))
          (.. me -collection (fetch (js-obj "reset" true)))))

      "render"
      (fn []
        (this-as me
          (let [ template-fn (.get js/Handlebars.TemplateLoader "route/list")]
            (.. me -$el (html (template-fn
                                (js-obj
                                  "routes" (.. me -collection toJSON)
                                  "router" (aget (.-options me) "router"))))))))

      "newRoute"
      (fn []
        (this-as me
          (let [ template-fn (.get js/Handlebars.TemplateLoader "route/new")
                 route ($ (template-fn (js-obj)))]
            (.. me ($ ".list-routes") (append route))
            (.. ($ js/window) (scrollTop (aget (.offset me) "top"))))))

      "createRoute"
      (fn []
        (this-as me
          (let [route (Route.
                        (js-obj
                          "method" (-> me (.$ "#form-route-new [name=route-method]") (.val))
                          "path"   (-> me (.$ "#form-route-new [name=route-path]")   (.val))
                          "router" (aget (.-collection me) "router")))]
            (try
              (.. me -collection (add route))
              (.save route)
              (.. me ($ "#form-route-new") parent remove)
              (catch js/Error e (.log js/console e)))
            false)))

      "deleteRoute"
      (fn [event]
        (this-as me
          (.. me -collection
            (at (.. ($ (aget event "currentTarget")) (data "route-id")))
            destroy))))))

(def RouteEditView
  (.extend js/Backbone.View
    (js-obj
      "el" ($ "<div id='page-route-edit'/>")
      "events" (js-obj
                 "click .btn-save" "save"
                 "click .btn-back" "back")
      "initialize"
      (fn []
        (this-as me
          (set! (.-model me)
            (Route. (js-obj
                      "id" (aget (.-options me) "id")
                      "workspace" (aget (.-options me) "workspace")
                      "router" (aget (.-options me) "router"))))
          (.. me -model (on "change" (.-render me) me))
          (.. me -model (on "invalid"
                          (fn [model error]
                            (.. me ($ ".label-comm-status") (label "danger" error)))
                          me))
          (set! (.-availableTemplates me)
            (TemplateList. (js-obj)
              (js-obj "url" (str "template/" (aget (.-options me) "workspace")))))

          (set! (.-availableAPIs me) (APIList.))
          (.. me -availableTemplates
            (on "reset"
              (fn []
                (.. me -availableAPIs (fetch (js-obj "reset" true))))
              me))
          (.. me -availableAPIs (on "reset" (aget me "fetchRouter") me))
          (.. me -availableTemplates (fetch (js-obj "reset" true)))))

      "render"
      (fn []
        (this-as me
          (let [ template-fn (.get js/Handlebars.TemplateLoader "route/edit")]
            (.. me -$el (html (template-fn (js-obj)))))
          (def templateDropdown
            (fn []
              (new js/Blockly.FieldDropdown
                (.map js/_
                  (.. me -availableTemplates toJSON)
                  (fn [hbs] (array (aget hbs "id") (aget hbs "path")))))))
          (def apiDropdown
            (fn []
              (new js/Blockly.FieldDropdown
                (.map js/_
                  (.. me -availableAPIs toJSON)
                  (fn [api] (array (aget api "id") (aget api "name")))))))
          (.inject js/Blockly
            (.getElementById js/document "marga-blockly")
            (js-obj
              "path" "./"
              "toolbox" (.getElementById js/document "marga-toolbox")
              "trashcan" false
              "collapse" false))
          (.domToWorkspace js/Blockly.Xml
            (aget js/Blockly "mainWorkspace")
            (.textToDom js/Blockly.Xml (.. me -model (get "xml"))))))

      "fetchRouter"
      (fn []
        (this-as me
          (.. me -model fetch)))

      "save"
      (fn []
        (this-as me
          (let [xml (.workspaceToDom js/Blockly.Xml (aget js/Blockly "mainWorkspace"))]
            (.. me ($ ".label-comm-status") (label "info" "Saving..."))
            (.. me -model
              (save "xml" (.domToText js/Blockly.Xml)
                (js-obj
                  "success"
                  (fn [model]
                    (.. me ($ ".label-comm-status") (label "success" "Saved!"))
                    (js/setTimeout
                      (fn [] (.. me ($ ".label-comm-status") (label "default" "")))
                      1500))

                  "error"
                  (fn [model]
                    (.. me ($ ".label-comm-status") (label "error" "Save failed!"))
                    (js/setTimeout
                      (fn [] (.. me ($ ".label-comm-status") (label "default" "")))
                      1500))))))))

      "back"
      (fn []
        (this-as me
          (.navigate app
            (str
              (aget (.-options me) "workspace")
              "/route/"
              (aget (.-options me) "router"))
            (js-obj "trigger" true)))))))

