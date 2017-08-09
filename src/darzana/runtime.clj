(ns darzana.runtime
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]))

(def default-options
  {:application-scope {}
   :scope-priorities [:error
                      :page
                      :params
                      :session
                      :application]
   :routes-path "dev/resources/scripts"})

(defn load-routes [commands routes-path]
  (let [nspace (create-ns (gensym))]
    (binding [*ns* nspace]
      (refer 'clojure.core)
      (doseq [cmds commands]
        (eval `(require '~cmds)))
      (into {} (for [f (->> (file-seq (io/file routes-path))
                            (filter #(and (.isFile %)
                                          (.endsWith (.getName %) ".clj"))))]

                  (eval (read-string (slurp f))))))))

(defn keyword-to-str [v]
  (cond
    (keyword? v) (name v)
    (map? v)     (if (empty? v)
                   v
                   (apply assoc {} (interleave (map name (keys v))
                                               (keyword-to-str (vals v)))))
    (coll?  v)   (map keyword-to-str v)
    :else v))

(defn create-context
  ([runtime request]
   {:scope {:application (keyword-to-str (:application-scope runtime))
            :session     (get request :session {})
            :params      (keyword-to-str (get request :params {}))
            :page        {}
            :error       {}
            :cookies     (get request :cookies {})}
    :session-add-keys    {}
    :session-delete-keys []
    :request request
    :runtime runtime}))

(defrecord DarzanaRuntime [routes validator template api-spec http-client scope-priorities])

(defmethod ig/init-key :darzana/runtime [_ {:keys [routes-path commands
                                                   validator
                                                   template
                                                   api-spec
                                                   http-client]}]
  (let [routes (load-routes commands routes-path)]
    (map->DarzanaRuntime (merge default-options
                                {:routes ["/" routes]
                                 :template template
                                 :api-spec api-spec
                                 :validator validator
                                 :http-client http-client}))))
