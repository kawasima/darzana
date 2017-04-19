(ns darzana.http-client.okhttp
  (:require [integrant.core :as ig]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [ring.util.codec :as codec]
            [darzana.http-client :as http-client])
  (:import [okhttp3 OkHttpClient
            Callback
            MediaType
            Request$Builder
            RequestBody]))

(derive :darzana.http-client/okhttp :darzana/http-client)

(defonce default-response-parser (fn [body] (.string body)))

(defn strip-content-type [f]
  (let [parts (string/split (if (nil? f) "" f) #"\s*;\s*")]
    (when (not (empty? parts))
      (first parts))))

(defn- process-headers [builder headers]
  (->> headers
       (map (fn [[k v]]
              (if (seq? v)
                (doseq [val v]
                  (.addHeader builder k val))
                (.addHeader builder k v))))
       doall)
  builder)

(defn- process-body [builder method media-type body]
  (when-not (contains? #{:get :head} method)
    (.method builder
             (string/upper-case (name method))
             (RequestBody/create (MediaType/parse media-type) body)))
  builder)


(defrecord OkHttp [client]
  http-client/HttpClient
  (request [{:keys [client]} r on-success on-failure]
    (let [req (-> (Request$Builder.)
                  (.url (:url r))
                  (process-headers (:headers r {}))
                  (process-body (:method r :get)
                                (get-in r [:headers "Content-Type"] "text/plain")
                                (:body r))
                  (.build))]
      (-> (.newCall client req)
          (.enqueue (reify Callback
                      (onFailure [_ call ex]
                        (on-failure ex))
                      (onResponse [_ call response]
                        (try (on-success response)
                             (finally (.close response)))))))))
  (parse-response [component response]
    (let [content-type (strip-content-type (.header response "Content-Type"))
          body (.body response)]
      (cond
                                        ;(re-find #"/xml$"  content-type) (.read (XMLSerializer.) body)
        (re-find #"/json$" content-type) (json/read-str (.string body))
        (re-find #"/x-www-form-urlencoded" content-type)
        (codec/form-decode
         (cond
           (string? body) body
           (instance? java.io.InputStream body) (slurp body)))
        (re-find #"^text/plain$" content-type) body
        :else (default-response-parser body)))))

(defmethod ig/init-key :darzana.http-client/okhttp [_ spec]
  (let [client (-> (OkHttpClient.)
                     (.newBuilder)
                     (.build))]
    (map->OkHttp {:client client})))
