(ns darzana.command.mapper
  (:require [clojure.java.data :refer [to-java from-java]]
            [darzana.validator :as v]))

(defmethod clojure.java.data/to-java [Long String] [clazz value]
  (Long/parseLong value))

(defmethod clojure.java.data/to-java [Integer String] [clazz value]
  (Integer/parseInt value))

(defn- error? [v]
  (= (:scope (meta v)) :error))

(defn- map-to-type [to-type from-value validator]
  (let [java-obj (to-java to-type from-value)]
    (v/validate validator java-obj)))

(defn read-value [{{validator :validator} :runtime :as context} from to]
  (let [{from-scope :scope from-var :var :or {from-scope :params}} from
        {to-scope   :scope to-var   :var to-type :type :or {to-scope :page}} to
        from-value (if from-var
                     (get-in context [:scope from-scope from-var])
                     (get-in context [:scope from-scope]))
        to-value (if to-type
                   (map-to-type to-type from-value validator)
                   from-value)]
    (if (error? to-value)
      (update-in context [:scope :error] merge to-value)
      (assoc-in  context [:scope to-scope to-var] to-value))))
