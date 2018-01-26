(ns darzana.validator.hibernate-validator
  (:require [integrant.core :as ig]
            [darzana.validator :as v])
  (:import [javax.validation Validation]))

(defrecord HibernateValidator [validator]
  v/Validator
  (validate [{:keys [validator]} bean-obj]
    (let [err (->> (.validate validator bean-obj (make-array Class 0))
                   (map (fn [cv] [(keyword (.. cv getPropertyPath toString)) (.getMessage cv)]))
                   (reduce #(assoc %1 (first %2) (second %2)) {}))]
      (with-meta err {:scope :error}))))

(defmethod ig/init-key :darzana.validator/hibernate-validator [_ spec]
  (let [validator (.. (Validation/buildDefaultValidatorFactory)
                      getValidator)]
    (map->HibernateValidator {:validator validator})))
