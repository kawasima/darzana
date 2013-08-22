(ns darzana.template
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]))

(defroutes routes
  (compojure/context "/template" []
    (GET "/*" { params :params }
      { :headers {"Content-Type" "text/x-handlebars"}
        :body (slurp  (str "resources/hbs/" (params :*) ".hbs"))})
    (POST "/*" { params :params }
      (spit (str "resources/hbs/" (params :*) ".hbs") (:source params))
      "successful")))


