(ns darzana.component.api-spec)

(defprotocol ApiSpec
  (build-request [component api context]))
