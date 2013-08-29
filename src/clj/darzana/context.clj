(ns darzana.context)

(def application-scope (ref {}))

(def scope-priorities
  [ :error :page :params :session :application])

(defn keyword-to-str [v]
  (cond
    (keyword? v) (name v)
    (map? v)     (if (empty? v)
                   v
                   (apply assoc {} (interleave (map name (keys v)) (keyword-to-str (vals v))))) 
    (coll?  v)   (map keyword-to-str v)
    :else v))

(defn create-context [request]
  { :scope { :application (keyword-to-str @application-scope)
             :session     (get request :session {})
             :params      (keyword-to-str (get request :params {}))
             :page        {}
             :error       {}}
    :session-add-keys    {}
    :session-delete-keys []})

(defn merge-scope [context]
  (apply merge (vals (context :scope))))

(defn find-in-scopes [context key]
  (first
    (filter #(not (nil? %))
      (for [name scope-priorities]
          (get-in (context :scope) (flatten [name key]))))))

