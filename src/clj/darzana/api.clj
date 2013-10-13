(ns darzana.api
  (require
    [darzana.context :as context]
    [oauth.client :as oauth]
    [oauth.signature :as sig]))

(def apis (ref []))

(def => 'assignment)

(defn assign
  ([from to]
    [from to])
  ([from arrow to]
    (if (= arrow 'assignment)
      (assign from to))))

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
  (assoc api :query-keys
    (map #(cond
            (keyword? %) (assign % %)
            (vector?  %) %) fields)))

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

(defn oauth-1-authorization
  [api & oauth-params]
  (println oauth-params)
  (assoc api :oauth-1-authorization
    (reduce conj {} (map #(cond
                            (vector?  %) [(second %) (first %)]
                            (keyword? %) [% %]) oauth-params))))

(defmacro defapi
  [api & body]
  `(let [e# (-> (create-api ~(name api))
              ~@body)]
     (def ~api e#)))


(defn build-request-headers [context api]
  (merge {}
    (when-let [content-type (api :content-type)]
      {"Content-Type" content-type})
    (when (not (or (= (get api :method :get) :get)
                 (contains? api :content-type)))
      {"Content-Type" "application/x-www-form-urlencoded"})
    (when-let [token-name (api :oauth-token)]
      (when-let [token (context/find-in-scopes context token-name)]
        {"Authorization" (str "Bearer " token)}))
    (when-let [oauth1 (api :oauth-1-authorization)]
      (let [ consumer-key (context/find-in-scopes context (oauth1 :oauth_consumer_key))
             consumer-secret (context/find-in-scopes context (oauth1 :oauth_consumer_secret))
             consumer (oauth/make-consumer consumer-key consumer-secret nil nil nil :hmac-sha1)
             unsigned-params (sig/oauth-params consumer
                               (sig/rand-str 30)
                               (sig/msecs->secs (System/currentTimeMillis)))
             unsigned-params (if-let [oauth-token (oauth1 :oauth_token)]
                               (assoc unsigned-params :oauth_token
                                 (context/find-in-scopes context oauth-token))
                               unsigned-params)
             unsigned-params (if-let [oauth-verifier (oauth1 :oauth_verifier)]
                               (assoc unsigned-params :oauth_verifier
                                 (context/find-in-scopes context oauth-verifier))
                               unsigned-params)
             unsigned-params (if-let [callback-uri (oauth1 :oauth_callback)]
                               (assoc unsigned-params :oauth_callback
                                 (context/find-in-scopes context callback-uri))
                               unsigned-params)
             signature (sig/sign
                         consumer
                         (sig/base-string
                           (-> (get api :method :get) sig/as-str clojure.string/upper-case)
                           (api :url)
                           (reduce conj unsigned-params
                               (map (fn [_] [(-> _ second keyword) (context/find-in-scopes context (first _))]) (api :query-keys))))
                         (if-let [token-secret (oauth1 :oauth_token_secret)]
                           (context/find-in-scopes context token-secret)
                           nil)) ]
        {"Authorization" (oauth/authorization-header
                           (merge unsigned-params {:oauth_signature signature}) "")}))))


