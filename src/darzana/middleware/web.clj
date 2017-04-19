(ns darzana.middleware.web
  (:require [duct.logger :as logger]
            [integrant.core :as ig]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.util.response :as response]))

(def ^:private request-log-keys
  [:request-method :uri :query-string])

(defn wrap-log-requests
  "Log each request using the supplied logger. The logger must implement the
  duct.core.protocols/Logger protocol."
  [handler logger]
  (fn [request]
    (logger/log logger :info ::request (select-keys request request-log-keys))
    (handler request)))

(defn wrap-log-errors
  "Log any exceptions with the supplied logger, then re-throw them."
  [handler logger]
  (fn [request]
    (try
      (handler request)
      (catch Throwable ex
        (logger/log logger :error ::handler-error ex)
        (throw ex)))))

(defn wrap-hide-errors
  "Middleware that hides any uncaught exceptions behind a generic 500 internal
  error response. Intended for use in production."
  [handler error-response]
  (fn [request]
    (try
      (handler request)
      (catch Throwable _
        (-> (response/resource-response error-response)
            (response/content-type "text/html")
            (response/status 500))))))

(defn wrap-not-found
  "Middleware that returns a 404 not found response if the handler returns nil."
  [handler error-response]
  (fn [request]
    (or (handler request)
        (-> (response/resource-response error-response)
            (response/content-type "text/html")
            (response/status 404)))))

(defn wrap-route-aliases [handler aliases]
  "Middleware that takes a map of URI aliases. If the URI of the request matches
  a URI in the map's keys, the URI is changed to the value corresponding to that
  key."
  (fn [request]
    (if-let [alias (aliases (:uri request))]
      (handler (assoc request :uri alias))
      (handler request))))

(defmethod ig/init-key ::log-requests [_ {:keys [logger]}]
  #(wrap-log-requests % logger))

(defmethod ig/init-key ::log-errors [_ {:keys [logger]}]
  #(wrap-log-errors % logger))

(defmethod ig/init-key ::hide-errors [_ {:keys [response]}]
  #(wrap-hide-errors % response))

(defmethod ig/init-key ::not-found [_ {:keys [response]}]
  #(wrap-not-found % response))

(defmethod ig/init-key ::route-aliases [_ aliases]
  #(wrap-route-aliases % aliases))

(defmethod ig/init-key ::defaults [_ defaults]
  #(wrap-defaults % defaults))

(defmethod ig/init-key ::webjars [_ {:keys [path] :or {path "/assets"}}]
  #(wrap-webjars % path))

(defmethod ig/init-key ::stacktrace [_ options]
  #(wrap-stacktrace % options))
