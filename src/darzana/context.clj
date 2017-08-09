(ns darzana.context)

(defn merge-scope [{:keys [scope] {:keys [scope-priorities]} :runtime}]
  (let [merged (->> (reverse scope-priorities)
                    (remove #(= % :error))
                    (map #(get scope %))
                    (reduce #(merge %1 %2) {}))]
    (assoc merged :error (:error scope))))

(defn- find-in-scopes-inner [{{:keys [scope-priorities]} :runtime scope :scope}
                             k]
  (let [ks (if (coll? k)
             (map name k)
             [(name k)])]
    (->> (for [scope-name scope-priorities]
           (let [ks-with-scope (into [scope-name] ks)]
             (when (get-in scope ks-with-scope)
               ks-with-scope)))
         (filter #(not (nil? %)))
         first)))

(defn find-in-scopes
  ([context key]
    (find-in-scopes-inner context key))
  ([context key not-found]
    (let [ value (find-in-scopes-inner context key) ]
      (if (nil? value) not-found value))))
