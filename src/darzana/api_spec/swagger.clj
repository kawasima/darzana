(ns darzana.api-spec.swagger
  (:require [integrant.core :as ig]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [darzana.api-spec :as api-spec]
            [darzana.context :as context]
            [clojure.java.io :as io])
  (:import [java.io File FileFilter]
           [io.swagger.parser SwaggerParser]
           [io.swagger.models Swagger HttpMethod]))

(derive :darzana.api-spec/swagger :darzana/api-spec)

(defn replace-url-variables [url context]
  (string/replace url #"\{([A-Za-z_]\w*)\}"
                  #(context/find-in-scopes context (-> % second keyword) "")))

(defn get-operation [swagger api-name path method]
  (some-> (get swagger api-name)
          (.getPath path)
          (.getOperationMap)
          (.get (HttpMethod/valueOf (string/upper-case (name method))))))

(defn build-query-string [operation context]
  (let [params (->> (.getParameters operation)
                    (filter #(= (.getIn %) "query")))]
    (->> params
         (map #(let [k (.getName %)]
                 (str k "=" (context/find-in-scopes context (keyword k)))))
         (string/join "&"))))

(defn build-url [swagger operation path context]
  (let [scheme (first (.getSchemes swagger))
        host (.getHost swagger)
        base-path (.getBasePath swagger)
        query-string (build-query-string operation context)]
    (str scheme "://" host base-path
         (replace-url-variables path context)
         (when query-string (str "?" query-string)))))

(defmulti build-model (fn [model swagger context] (class model)))

(defmethod build-model io.swagger.models.RefModel
  [ref-model swagger context]
  (let [model (-> (.getDefinitions swagger)
                  (.get (.getSimpleRef ref-model)))]
    (->> (.getProperties model)
         (map (fn [[k v]]
                [k (context/find-in-scopes context (keyword k))]))
         (into {}))))

(defn build-request-body [swagger operation context]
  (let [models (some->> (.getParameters operation)
                         (filter #(= (.getIn %) "body"))
                         (map #(build-model (.getSchema %) swagger context)))
        content-type (-> (.getConsumes operation)
                         first)]
    (cond
      (re-find #"/json$" content-type)
      (json/write-str (first models)))))

(defn build-request-headers [operation method context]
  (apply merge {}
    (when-let [content-type (first (.getConsumes operation))]
      {"Content-Type" content-type})))

(defrecord SwaggerModel [apis]
  api-spec/ApiSpec
  (build-request [{:keys [apis]} {:keys [id path method]} context]
    (let [operation (get-operation apis id path method)]
      (merge {:url (build-url (get apis id) operation path context)
              :method method
              :headers (build-request-headers operation method context)
              :body (build-request-body (get apis id) operation context)}))))

(defmethod ig/init-key :darzana.api-spec/swagger [_ {:keys [swagger-path]}]
  (let [parser (SwaggerParser.)
        apis (->> (io/file swagger-path)
                  (file-seq)
                  (filter #(and (.endsWith (.getName %) ".json")
                                (.isFile %)))
                  (map (fn [f]
                         [(string/replace (.getName f) #"\.json$" "")
                          (.read parser (.getAbsolutePath f))]))
                  (into {}))]
    (map->SwaggerModel apis)))
