(ns darzana.component.runtime
  (:require [com.stuartsierra.component :as component]
            [duct.util.namespace :as ns]
            [clojure.java.io :as io]))

(def default-options
  {:application-scope {}
   :scope-priorities [:error
                      :page
                      :params
                      :session
                      :application]
   :routes-path "dev/resources/scripts"})

(defn load-routes [{:keys [commands routes-path]}]
  (let [nspace (create-ns (gensym))]
    (binding [*ns* nspace]
      (doseq [cmds commands]
        (eval `(require '~cmds)))
      (into {} (for [f (->> (file-seq (io/file routes-path))
                            (filter #(and (.isFile %)
                                          (.endsWith (.getName %) ".clj"))))]

                  (eval (read-string (slurp f))))))))

(defrecord DarzanaRuntime [commands]
  component/Lifecycle

  (start [component]
    (let [routes (load-routes component)]
      (assoc component :routes ["/" routes])))

  (stop [component]
    (dissoc component :commands)))

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

(defn runtime-component [options]
  (map->DarzanaRuntime (merge default-options options)))
