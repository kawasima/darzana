(ns darzana.api-spec
  (:require [integrant.core :as ig]))

(defprotocol ApiSpec
  (build-request [this api context])
  (spec-id [this api]))

(defmethod ig/init-key :darzana/api-spec [_ spec]
  )
