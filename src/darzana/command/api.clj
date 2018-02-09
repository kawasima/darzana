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
  (let [req (api-spec/build-request api-spec api context)]
    (http-client/request
     http-client
     req
     (fn [raw-res]
       (let [res (http-client/parse-response http-client raw-res)]
         (if (< (:status res) 300)
           (async/put! ch {:page {(or (:var api) (api-spec/spec-id api-spec api))
                                  (:body res)}})
           (async/put! ch {:error
                           {(:url req)
                            {"request" req
                             "status"  (:status res)
                             "message" (:body res)}}}))))
     (fn [ex]
       (async/put! ch {:error
                       {(:url req)
                        {"message" (.getMessage ex)}}})))))

(defn- call-api-internal [context apis]
  (let [ch (async/chan)
        result (promise)
        api-loop (async/go-loop [api-result {}
                                 i 1]
                   (let [res (async/<! ch)]
                     (if (< i (count apis))
                       (recur (merge-with merge api-result res) (inc i))
                       (deliver result (merge-with merge api-result res)))))]
    (doseq [api apis]
      (try (execute-api context ch api)
           (catch Exception e
             (.printStackTrace e)
             (async/put! ch {:error {(name (:id api))
                                     {"message" (.getMessage e)}}}))))
    @result))

(defn call-api
  "Call the giving APIs."
  [context api]
  (let [apis (if (map? api) [api] api)
        context (update-in context [:scope] merge (call-api-internal context apis))]
    context))
