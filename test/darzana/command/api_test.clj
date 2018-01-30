(ns darzana.command.api-test
  (:require [integrant.core :as ig]
            [duct.core :as duct]
            [darzana.api-spec.swagger :as swagger]
            [darzana.http-client :as http]
            [darzana.runtime :as runtime]
            [darzana.command.api :as sut]
            [cheshire.core :as json]
            [clojure.test :refer :all]))

(duct/load-hierarchy)

(deftest call-api
  (let [config  {:darzana.api-spec/swagger {:swagger-path "dev/resources/swagger"}
                 :darzana/runtime {:routes-path "dev/resources/scripts"
                                   :commands [['darzana.command.api :as 'api]
                                              ['darzana.command.control :as 'control]
                                              ['darzana.command.mapper :as 'mapper]
                                              ['darzana.command.renderer :as 'renderer]]
                                   :api-spec    (ig/ref :darzana/api-spec)
                                   :http-client (reify http/HttpClient
                                                  (request [component request on-success on-error]
                                                    (on-success {}))
                                                  (parse-response [component response]
                                                    {:status 200
                                                     :headers {"Content-Type" "application/json"}
                                                     :body {:message "OK"}}))}}
        system  (ig/init config)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:petId "1"}})
        api {:id :petstore :path "/pets" :method :post}]
    (is (= "OK" (-> (sut/call-api ctx api)
                    (get-in [:scope :page "createPets" :message]))))))
