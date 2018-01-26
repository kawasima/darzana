(ns darzana.api-spec.swagger
  (:require [integrant.core :as ig]
            [clojure.string :as string]
            [cheshire.core :as json]
            [darzana.api-spec :as api-spec]
            [darzana.context :as context]
            [clojure.java.io :as io])
  (:import [java.io File FileFilter]
           [io.swagger.parser OpenAPIParser]
           [io.swagger.v3.oas.models PathItem$HttpMethod]))

(defn replace-url-variables [url context]
  (string/replace url #"\{([A-Za-z_]\w*)\}"
                  #(when-let [ks (context/find-in-scopes context (-> % second keyword))]
                     (get-in context (into [:scope] ks)))))

(defn get-operation [swagger api-name path method]
  (some-> (get swagger api-name)
          (.getPaths)
          (.get path)
          (.readOperationsMap)
          (.get (PathItem$HttpMethod/valueOf (string/upper-case (name method))))))

(defn build-query-string [operation context]
  (let [params (->> (.getParameters operation)
                    (filter #(= (.getIn %) "query")))]
    (->> params
         (map #(let [k (.getName %)]
                 (str k "=" (get-in context (context/find-in-scopes context (keyword k))))))
         (string/join "&"))))

(defn build-url [swagger operation path context]
  (let [scheme (first (.getSchemes swagger))
        host (.getHost swagger)
        base-path (.getBasePath swagger)
        query-string (build-query-string operation context)]
    (str scheme "://" host base-path
         (replace-url-variables path context)
         (when query-string (str "?" query-string)))))

(defmulti build-model (fn [model swagger context ks] (class model)))

(defmethod build-model io.swagger.models.properties.Property
  [model swagger context ks])

(defmethod build-model io.swagger.models.ModelImpl
  [model swagger context ks]
  (->> (.getProperties model)
       (map (fn [[k v]]
              [k (get-in context (-> (into [:scope] ks) (into [k])))]))
       (into {})))

(defmethod build-model io.swagger.models.RefModel
  [ref-model swagger context ks]
  (let [model (-> (.getDefinitions swagger)
                  (.get (.getSimpleRef ref-model)))]
    (build-model model swagger context ks)))

(defmethod build-model :default
  [model swagger context ks]
  (throw (UnsupportedOperationException. "Not implemented")))

(defn build-request-body [swagger operation context]
  (let [models (some->> (.getParameters operation)
                        (filter #(= (.getIn %) "body"))
                        (map #(build-model (.getSchema %) swagger context
                                           (context/find-in-scopes context (.getName %)))))
        content-type (or (-> (.getConsumes operation)
                             first)
                         "") ]
    (cond
      (re-find #"/json$" content-type)
      (json/generate-string (first models))
      :default nil)))

(defn build-request-headers [operation method context]
  (apply merge {}
         (when-let [content-type (first (.getConsumes operation))]
           {"Content-Type" content-type})
         (when-let [accept (first (.getProduces operation))]
           {"Accept" accept})))

(defrecord SwaggerModel [apis]
  api-spec/ApiSpec
  (build-request [{:keys [apis]} {:keys [id path method]} context]
    (if-let [operation (get-operation apis id path method)]
      (merge {:url (build-url (get apis id) operation path context)
              :method method
              :headers (build-request-headers operation method context)
              :body (build-request-body (get apis id) operation context)})))
  (spec-id [{:keys [apis]} {:keys [id path method]}]
    (let [operation (get-operation apis id path method)]
      (.getOperationId operation))))

(defmethod ig/init-key :darzana.api-spec/swagger [_ {:keys [swagger-path]}]
  (let [parser (OpenAPIParser.)
        apis (->> (io/file swagger-path)
                  (file-seq)
                  (filter #(and (.endsWith (.getName %) ".json")
                                (.isFile %)))
                  (map (fn [f]
                         [(keyword (string/replace (.getName f) #"\.json$" ""))
                          (.read parser (.getAbsolutePath f))]))
                  (into {}))]
    (map->SwaggerModel {:apis apis})))
