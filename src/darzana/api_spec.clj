(ns darzana.api-spec
  (:require [integrant.core :as ig]))

(defprotocol ApiSpec
  (build-request [component api context]))

(defmethod ig/init-key :darzana/api-spec [_ spec]
  )
