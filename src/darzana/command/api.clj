(ns darzana.command.api
  (:require [clojure.data.json :as json]
            [clojure.core.async :as async]
            [darzana.context :as context]
            [darzana.component.api-spec :as api-spec]
            [darzana.component.http-client :as http-client]
            [darzana.component.jcache :as jcache]
            [clojure.tools.logging :as log]))

(defn execute-api [{{:keys [http-client api-spec]} :runtime :as context} ch api]
  (http-client/request
   http-client
   (api-spec/build-request api-spec api context)
   (fn [res]
     (async/put! ch {:page {(:id api)
                            (http-client/parse-response http-client res)}}))
   (fn [ex]
     (async/put! ch {:error
                     {(:id api)
                      {"message" (.getMessage ex)}}}))))

(defn- call-api-internal [context apis]
  (let [ch (async/chan)
        result (promise)
        api-loop (async/go-loop [api-result {}
                                 i 1]
                   (let [res (async/<! ch)]
                     (if (< i (count apis))
                       (recur (merge api-result res) (inc i))
                       (deliver result (merge api-result res)))))]
    (doseq [api apis]
      (try (execute-api context ch api)
           (catch Exception e
             (async/put! ch {:error {(:id api)
                               {"message" e}}}))))
    @result))

(defn call-api [context api]
  (let [apis (if (map? api) [api] api)]
    (update-in context [:scope] merge (call-api-internal context apis))))
