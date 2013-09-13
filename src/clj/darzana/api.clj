(ns darzana.api
  (:use
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [clojure.data.json :as json]))

(def apis (ref []))
(defn create-api
  "create an api."
  [api]
  (dosync (alter apis conj api))
  { :name (keyword api)
    :url ""
    :query-keys []
    :method :get
    :success? #(or
                 (and
                   (= (get-in % [:opts :method]) :get)
                   (= (% :status) 200))
                 (and
                   (= (get-in % [:opts :method]) :post)
                   (= (% :status) 201))
                 (and
                   (some #{:put :delete} [(get-in % [:opts :method])])
                   (= (% :status) 204)))})

(defn url
  [api url]
  (assoc api :url url))

(defn method
  [api method]
  (assoc api :method method))

(defn content-type
  [api content-type]
  (assoc api :content-type content-type))

(defn query-keys
  [api & fields]
  (assoc api :query-keys (vec fields)))

(defn expire
  [api expire]
  (assoc api :expire expire))

(defmacro defapi
  [api & body]
  `(let [e# (-> (create-api ~(name api))
              ~@body)]
     (def ~api e#)))

(defroutes routes
  (compojure/context "/api" []
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str @apis)})))
