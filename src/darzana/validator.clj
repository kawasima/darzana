(ns darzana.validator
  (:require [integrant.core :as ig]))

(defprotocol Validator
  (validate [component bean]))

(defmethod ig/init-key :darzana/validator [_ spec]
  )
