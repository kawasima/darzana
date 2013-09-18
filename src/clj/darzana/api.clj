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
    :success? (fn [response]
                (or
                  (and
                    (= (get-in response [:opts :method]) :get)
                    (= (response :status) 200))
                   (and
                     (= (get-in response [:opts :method]) :post)
                     (some #(= (response :status) %) [200 201]))
                   (and
                     (some #{:put :delete} [(get-in response [:opts :method])])
                     (some #(= (response :status) %) [200 204]))))})

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

(defn oauth-token
  [api oauth-token]
  (assoc api :oauth-token oauth-token))

(defn basic-auth
  ([api id passwd]
    (assoc api :basic-auth [id passwd]))
  ([api id-passwd]
    (assoc api :basic-auth id-passwd)))

(defmacro defapi
  [api & body]
  `(let [e# (-> (create-api ~(name api))
              ~@body)]
     (def ~api e#)))

(defroutes routes
  (compojure/context "/api" []
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] {:id _ :name _}) @apis))})))

