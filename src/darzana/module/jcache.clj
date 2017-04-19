(ns darzana.module.jcache
  (:require [integrant.core :as ig])
  (:import [javax.cache Caching Cache]
           [javax.cache.configuration MutableConfiguration]))

(defn caching-provider []
  (Caching/getCachingProvider))

(defrecord JCache [manager cache])

(defn get-cache [{:keys [cache]} key]
  (.get cache key))

(defn put-cache [{:keys [cache]} key val]
  (.put cache key val))

(defmethod ig/init-key :darzana.cache/jcache [_ spec]
  (if-let [provider (caching-provider)]
    (let [manager (.getCacheManager provider)
          config (-> (MutableConfiguration.)
                     (.setTypes java.lang.String java.lang.Integer))
          cache (.createCache manager "api-response-cache" config)]
      (map->JCache {:manager manager
                    :cache   cache}))))

(defmethod ig/halt-key! :darzana.cache/jcache [_ jcache]
  (when-let [cache (:cache jcache)]
    (.close cache))
  (when-let [manager (:manager jcache)]
    (.close manager)))
