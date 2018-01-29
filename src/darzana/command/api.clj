(ns darzana.command.api
  "The commands for calling APIs."
  (:require [clojure.pprint :refer :all]
            [clojure.core.async :as async]
            [darzana.context :as context]
            [darzana.api-spec :as api-spec]
            [darzana.http-client :as http-client]
            [darzana.module.jcache :as jcache]
            [clojure.tools.logging :as log]))

(defn- execute-api [{{:keys [http-client api-spec]} :runtime :as context} ch api]
  (http-client/request
   http-client
   (api-spec/build-request api-spec api context)
   (fn [raw-res]
     (let [res (http-client/parse-response http-client raw-res)]
       (if (< (:status res) 300)
         (async/put! ch {:page {(or (:var api) (api-spec/spec-id api-spec api))
                                (:body res)}})
         (async/put! ch {:error
                         {(:id api) {:status (:status res)
                                     :message (:body res)}}}))))
   (fn [ex]
     (async/put! ch {:error
                     {(:id api)
                      {:message (.getMessage ex)}}}))))

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
             (.printStackTrace e)
             (async/put! ch {:error {(:id api)
                               {"message" e}}}))))
    @result))

(defn call-api
  "Call the giving APIs."
  [context api]
  (let [apis (if (map? api) [api] api)]
    (update-in context [:scope] merge (call-api-internal context apis))))
