(ns darzana.component.jcache
  (:require [com.stuartsierra.component :as component])
  (:import [javax.cache Caching Cache]
           [javax.cache.configuration MutableConfiguration]))

(defn caching-provider []
  (Caching/getCachingProvider))

(defrecord JCache []
  component/Lifecycle
  (start [component]
    (if-let [provider (caching-provider)]
      (let [manager (.getCacheManager provider)
            config (-> (MutableConfiguration.)
                       (.setTypes java.lang.String java.lang.Integer))
            cache (.createCache manager "api-response-cache" config)]
        (assoc component
               :manager manager
               :cache   cache))
      component))

  (stop [component]
    (when-let [cache (:cache component)]
      (.close cache))
    (when-let [manager (:manager component)]
      (.close manager))
    (dissoc component :cache :manager)))

(defn get-cache [{:keys [cache]} key]
  (.get cache key))

(defn put-cache [{:keys [cache]} key val]
  (.put cache key val))

(defn jcache-component [options]
  (map->JCache options))
