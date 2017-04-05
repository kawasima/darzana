(ns darzana.component.http-client)

(defprotocol HttpClient
  (request [component request on-success on-error])
  (parse-response [component response]))
