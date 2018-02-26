(ns darzana.api-spec.swagger
  (:require [duct.logger :as logger]
            [integrant.core :as ig]
            [clojure.string :as string]
            [cheshire.core :as json]
            [darzana.api-spec :as api-spec]
            [darzana.context :as context]
            [clojure.java.io :as io]
            [ring.util.codec :refer [url-encode]])
  (:import [java.io File FileFilter]
           [io.swagger.parser OpenAPIParser]
           [io.swagger.parser.models ParseOptions]
           [io.swagger.oas.models PathItem$HttpMethod]))

(defn replace-url-variables [url context]
  (string/replace url #"\{([A-Za-z_]\w*)\}"
                  #(if-let [ks (context/find-in-scopes context (-> % second keyword))]
                     (str (get-in context (into [:scope] ks)))
                     "")))

(defn get-operation [swagger api-name path method]
  (some-> (get swagger api-name)
          (.getPaths)
          (.get path)
          (.readOperationsMap)
          (.get (PathItem$HttpMethod/valueOf (string/upper-case (name method))))))

(defn ref-parameter [swagger ref]
  (when-let [ref-name (last (re-find #"/([^/]+)$" ref))]
    (-> (.getComponents swagger)
        (.getParameters)
        (.get ref-name))))

(defn ref-schema [swagger ref]
  (when-let [ref-name (last (re-find #"/([^/]+)$" ref))]
    (-> (.getComponents swagger)
        (.getSchemas)
        (.get ref-name))))

(defmulti build-model (fn [model swagger context ks] (class model)))

(defmethod build-model io.swagger.oas.models.media.StringSchema
  [model swagger context ks]
  (when ks
    (get-in context (into [:scope] ks))))

(defmethod build-model io.swagger.oas.models.media.IntegerSchema
  [schema swagger context ks]
  (some-> ks
          (#(get-in context (into [:scope] %)))
          ((fn [v]
             (if (= (.getFormat schema) "int32")
               (Integer/parseInt v)
               (Long/parseLong v))))))

(defmethod build-model io.swagger.oas.models.media.NumberSchema
  [schema swagger context ks]
  (some-> ks
          (#(get-in context (into [:scope] %)))
          ((fn [v]
             (if (= (.getFormat schema) "float")
               (Float/parseFloat v)
               (Double/parseDouble v))))))

(defmethod build-model io.swagger.oas.models.media.DateSchema
  [model swagger context ks]
  (some-> ks
          (#(get-in context (into [:scope] %)))
          ((fn [v]
             v))))

(defmethod build-model io.swagger.oas.models.media.ObjectSchema
  [model swagger context ks]
  (->> (.getProperties model)
       (map (fn [[k v]]
              (when-let [vkey (context/find-in-scopes context k)]
                [k (build-model v swagger context vkey)])))
       (into {})))

(defmethod build-model io.swagger.oas.models.media.ArraySchema
  [schema swagger context ks]
  (->> (.getItems schema)
       (map #(build-model % context ks))))

(defmethod build-model :default
  [schema swagger context ks]
  (if-let [schema (some->> (.get$ref schema) (ref-schema swagger))]
    (build-model schema swagger context ks)
    (throw (UnsupportedOperationException. (str "Not implemented" (class schema))))))

(defn build-query-string [operation swagger context]
  (let [params (->> (.getParameters operation)
                    (filter #(= (.getIn %) "query")))]
    (->> params
         (map #(let [key (.getName %)
                     ks  (context/find-in-scopes context key)]
                 (if-let [v (if (.get$ref %)
                              (build-model % swagger context ks)
                              (build-model (.getSchema %) swagger context ks))]
                   (str (url-encode key) "=" (url-encode v))) ))
         (keep identity)
         (string/join "&")
         not-empty)))

(defn replace-default-variables [url variables]
  (string/replace url #"\{([A-Za-z_]\w*)\}"
                  #(some-> (.get variables (second %))
                           (.getDefault))))

(defn build-url [swagger operation path context]
  (let [server (first (.getServers swagger))
        server-variables (.getVariables server)
        query-string (build-query-string operation swagger context)]
    (str (-> (.getUrl server)
             (replace-url-variables context)
             (replace-default-variables server-variables))
         (replace-url-variables path context)
         (when query-string (str "?" query-string)))))

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

(defrecord SwaggerModel [apis logger]
  api-spec/ApiSpec
  (build-request [{:keys [apis]} {:keys [id path method]} context]
    (if-let [operation (get-operation apis id path method)]
      (let [request {:url (build-url (get apis id) operation path context)
                     :method method
                     :headers (build-request-headers (get apis id) operation method context)
                     :body    (or (build-request-body (get apis id) operation context) "")}]
        (logger/log logger :debug ::build-request request)
        request)
      (logger/log logger :warn ::build-request (str "operation not found id=" id ",path=" path ",method=" method))))
  (spec-id [{:keys [apis]} {:keys [id path method]}]
    (if-let [operation (get-operation apis id path method)]
      (.getOperationId operation)
      (keyword (str (name id) "-" (clojure.string/replace path #"/" "-") "-" (name method))))))

(defmethod ig/init-key :darzana.api-spec/swagger [_ {:keys [swagger-path logger]}]
  (let [parser (OpenAPIParser.)
        apis (some->> (io/file swagger-path)
                      (file-seq)
                      (filter #(and (or (.endsWith (.getName %) ".json")
                                        (.endsWith (.getName %) ".yaml"))
                                    (.isFile %)))
                      (map (fn [f]
                             [(keyword (string/replace (.getName f) #"\.\w+$" ""))
                              (->> (.readLocation parser (.getAbsolutePath f) nil (ParseOptions.))
                                   (.getOpenAPI))]))
                      (into {}))]
    (map->SwaggerModel {:apis apis :logger logger})))
