(ns darzana.http-client
  (:require [integrant.core :as ig]))

(defprotocol HttpClient
  (request [component request on-success on-error])
  (parse-response [component response]))

(defmethod ig/init-key :darzana/http_client [_ spec]
  )
