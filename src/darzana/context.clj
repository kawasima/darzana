(ns darzana.context)

(defn merge-scope [context]
  (apply merge (vals (context :scope))))

(defn- find-in-scopes-inner [{{{:keys [scope-priorities]} :runtime} :components scope :scope}
                             key]
  (cond
    (string? key) key
    (number? key) (str key)
    :else
    (let [keys (if (coll? key)
                 (map name key)
                 (name key))]
      (first
        (filter #(not (nil? %))
                (for [scope-name scope-priorities]
                  (get-in scope (flatten [scope-name keys]))))))))

(defn find-in-scopes
  ([context key]
    (find-in-scopes-inner context key))
  ([context key not-found]
    (let [ value (find-in-scopes-inner context key) ]
      (if (nil? value) not-found value))))
