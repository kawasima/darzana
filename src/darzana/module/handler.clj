(ns darzana.module.handler
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [duct.core :as core]
            [duct.core.env :as env]
            [duct.core.merge :as merge]
            [duct.server.http.jetty :as jetty]
            [darzana.middleware.web :as mw]
            [bidi.ring :refer [make-handler]]
            [integrant.core :as ig]
            [ring.middleware.defaults :as defaults]))

(def ^:private server-port
  (env/env '["PORT" Int :or 3000]))

(defn- get-environment [config options]
  (:environment options (:duct.core/environment config :production)))

(defn- derived-key [m k default]
  (if-let [kv (ig/find-derived-1 m k)] (key kv) default))

(defn- http-server-key [config]
  (derived-key config :duct.server/http :duct.server.http/jetty))

(defn- server-config [config]
  {(http-server-key config) {:port (merge/displace server-port)}})

(def ^:private logging-config
  {::handler     {:middleware ^:distinct [(ig/ref ::mw/log-requests)
                                          (ig/ref ::mw/log-errors)]}
   ::mw/log-requests {:logger (ig/ref :duct/logger)}
   ::mw/log-errors   {:logger (ig/ref :duct/logger)}})

(def ^:private error-configs
  {:production
   {::handler {:middleware ^:distinct [(ig/ref ::mw/hide-errors)]}}
   :development
   {::handler {:middleware ^:distinct [(ig/ref ::mw/stacktrace)]}}})

(def ^:private api-config
  {:duct.server/http {:handler (ig/ref ::handler)
                      :logger  (ig/ref :duct/logger)}
   ::handler     {:endpoints  []
                  :middleware ^:distinct [(ig/ref ::mw/not-found)
                                          (ig/ref ::mw/defaults)]
                  :runtime (ig/ref :darzana/runtime)}
   ::mw/not-found    {:response (merge/displace "Resource Not Found")}
   ::mw/hide-errors  {:response (merge/displace "Internal Server Error")}
   ::mw/stacktrace   {}
   ::mw/defaults     (merge/displace defaults/api-defaults)})

(defmethod ig/init-key ::handler [_ {:keys [endpoints middleware runtime]}]
  ((apply comp (reverse middleware))
   (make-handler (:routes runtime)
                 (fn [h]
                   (partial h runtime)))))

(defmethod ig/init-key :darzana.module/handler [_ options]
  (fn [config]
    (core/merge-configs config
                        (server-config config)
                        api-config
                        logging-config
                        (error-configs (get-environment config options)))))
