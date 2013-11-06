(ns darzana.api
  (:require
    [clojure.string :as string]
    [clojure.data.json :as json]
    [ring.util.codec :as codec]
    [darzana.context :as context]
    [oauth.client :as oauth]
    [oauth.signature :as sig])
  (:import
    [net.sf.json.xml XMLSerializer]))

(def ^:dynamic default-response-parser (fn [body] (json/read-str body)))

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
  (dosync (alter apis conj (str *ns* "/" api)))
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
    (vec (map #(cond
                 (keyword? %) (assign % %)
                 (vector?  %) %) fields))))

(defn expire
  [api expire]
  (assoc api :expire expire))

(defn headers
  [api & headers]
  (assoc api :headers
    (map #(cond
            (keyword? %) (assign % %)
            (vector?  %) %) headers)))

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
  (assoc api :oauth-1-authorization
    (reduce conj {} (map #(cond
                            (vector?  %) [(second %) (first %)]
                            (keyword? %) [% %]) oauth-params))))

(defmacro defapi
  [api & body]
  `(let [e# (-> (create-api ~(name api))
              ~@body)]
     (def ~api e#)))



(defn replace-url-variable [url context]
  (string/replace url #":([A-Za-z_]\w*)"
    #(context/find-in-scopes context (-> % second keyword) "")))

(defn build-query-string [context api]
  (string/join "&"
    (map #(str (-> % second name) "=" (context/find-in-scopes context (first %))) (:query-keys api))))

(defn build-url [context api]
  (let [base-url (replace-url-variable (api :url) context)]
    (if (= (api :method) :get)
      (str base-url
        (if-not (or (.contains base-url "?" ) (empty? (api :query-keys))) "?")
        (build-query-string context api))
      base-url)))

(defn strip-content-type [f]
  (let [parts (string/split (if (nil? f) "" f) #"\s*;\s*")]
    (when (not (empty? parts))
      (first parts))))

(defn parse-response [response]
  (let [ content-type (strip-content-type (-> response :headers :content-type))
         body (response :body) ]
    (cond
      (re-find #"/xml$"  content-type) (.read (XMLSerializer.) body)
      (re-find #"/json$" content-type) (json/read-str body)
      (re-find #"/x-www-form-urlencoded" content-type) (codec/form-decode
                                                         (cond
                                                           (string? body) body
                                                           (instance? java.io.InputStream body) (slurp body)))
      (re-find #"^text/plain$" content-type) body
      (empty? body) {}
      :else (default-response-parser body))))

(defn build-request-body [context api]
  (cond 
    (re-find #"/json$" (get api :content-type ""))
    (json/write-str
      (reduce #(assoc %1 (-> %2 second name)
                 (context/find-in-scopes context (first %2))) {} (api :query-keys)))
    (not= (api :method) :get)
    (string/join "&"
      (map #(str (-> % second name) "=" (context/find-in-scopes context (first %))) (api :query-keys)))))

(defn build-request-headers [context api]
  (apply merge {}
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
                           (merge unsigned-params {:oauth_signature signature}) "")}))
    (when-let [headers (api :headers)]
      (map (fn [_] {(-> _ second name) (context/find-in-scopes context (first _))}) headers))))

(defn build-request [request context api]
  (merge request
    (when-let [basic-auth (api :basic-auth)]
      {:basic-auth [ (context/find-in-scopes context (first  basic-auth))
                     (context/find-in-scopes context (second basic-auth))]})
    {:headers (build-request-headers context api)}
    {:body (build-request-body context api)}))


