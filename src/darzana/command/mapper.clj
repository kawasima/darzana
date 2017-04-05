(ns darzana.command.mapper
  (:require [clojure.java.data :refer [to-java from-java]]))

(defn read-value [context {:keys [scope var]
                           :or {scope :params}} bean-class]
  (assoc-in context
            [:scope :page var]
            (to-java bean-class (get-in context [:scope scope]))))
