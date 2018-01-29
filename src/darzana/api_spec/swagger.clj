(ns darzana.api-spec.swagger
  (:require [integrant.core :as ig]
            [clojure.string :as string]
            [cheshire.core :as json]
            [darzana.api-spec :as api-spec]
            [darzana.context :as context]
            [clojure.java.io :as io])
  (:import [java.io File FileFilter]
           [io.swagger.parser OpenAPIParser]
           [io.swagger.parser.models ParseOptions]
           [io.swagger.oas.models PathItem$HttpMethod]))

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
  (let [server (first (.getServers swagger))
        query-string (build-query-string operation context)]
    (str (.getUrl server)
         (replace-url-variables path context)
         (when query-string (str "?" query-string)))))

(defmulti build-model (fn [model swagger context ks] (class model)))

(defmethod build-model io.swagger.oas.models.media.StringSchema
  [model swagger context ks]
  )
(defmethod build-model io.swagger.oas.models.media.ObjectSchema
  [model swagger context ks]
  (->> (.getProperties model)
       (map (fn [[k v]]
              (when-let [vkey (context/find-in-scopes context k)]
                [k (get-in context (into [:scope] vkey))])))
       (into {})))

(defmethod build-model io.swagger.oas.models.media.ArraySchema
  [schema swagger context ks]
  (->> (.getItems schema)
       (map #(build-model % context ks))))

(defn ref-schema [swagger ref]
  (when-let [ref-name (last (re-find #"/([^/]+)$" ref))]
    (-> (.getComponents swagger)
        (.getSchemas)
        (.get ref-name))))

(defmethod build-model :default
  [schema swagger context ks]
  (if-let [schema (ref-schema swagger (.get$ref schema))]
    (build-model schema swagger context ks)
    (throw (UnsupportedOperationException. "Not implemented"))))

(defn build-request-body [swagger operation context]
  (let [content-type (or (some-> (.getRequestBody operation)
                                 (.getContent)
                                 keys
                                 first)
                         "")
        model (some-> (.getRequestBody operation)
                      (.getContent)
                      (.get content-type)
                      (.getSchema)
                      (build-model swagger context nil))]
    (cond
      (re-find #"/json$" content-type)
      (json/generate-string model)
      :default nil)))

(defn ref-parameter [swagger ref]
  (when-let [ref-name (last (re-find #"/([^/]+)$" ref))]
    (-> (.getComponents swagger)
        (.getParameters)
        (.get ref-name))))

(defn build-request-headers [swagger operation method context]
  (let [header-params (->> (.getParameters operation)
                           (map #(->> (if-let [ref (.get$ref %)]
                                        (when-let [parameter (ref-parameter swagger ref)]
                                          (when (= (.getIn parameter) "header")
                                            [(.getName parameter)
                                             (get-in context (into [:scope] (context/find-in-scopes context (.getName parameter))))])))))
                           (into {}))]
    (apply merge header-params
         (when-let [content-type (some->> (.getRequestBody operation)
                                          (.getContent)
                                          keys
                                          first)]
           {"Content-Type" content-type})
         (when-let [accept (some-> (.getResponses operation)
                                   (.get "200")
                                   (.getContent)
                                   keys
                                   first)]
           {"Accept" accept}))))

(defrecord SwaggerModel [apis]
  api-spec/ApiSpec
  (build-request [{:keys [apis]} {:keys [id path method]} context]
    (if-let [operation (get-operation apis id path method)]
      (merge {:url (build-url (get apis id) operation path context)
              :method method
              :headers (build-request-headers (get apis id) operation method context)
              :body (build-request-body (get apis id) operation context)})))
  (spec-id [{:keys [apis]} {:keys [id path method]}]
    (let [operation (get-operation apis id path method)]
      (.getOperationId operation))))

(defmethod ig/init-key :darzana.api-spec/swagger [_ {:keys [swagger-path]}]
  (let [parser (OpenAPIParser.)
        apis (->> (io/file swagger-path)
                  (file-seq)
                  (filter #(and (or (.endsWith (.getName %) ".json")
                                    (.endsWith (.getName %) ".yaml"))
                                (.isFile %)))
                  (map (fn [f]
                         [(keyword (string/replace (.getName f) #"\.\w+$" ""))
                          (->> (.readLocation parser (.getAbsolutePath f) nil (ParseOptions.))
                               (.getOpenAPI))]))
                  (into {}))]
    (map->SwaggerModel {:apis apis})))
