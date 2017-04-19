(ns darzana.api-spec.swagger-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [darzana.context :as context]
            [darzana.runtime :as runtime]
            [darzana.api-spec.swagger :as swagger]))

(deftest swagger-read
  (let [config {:darzana.api-spec/swagger {:swagger-path "dev/resources/swagger"}}
        system (ig/init config)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:petId "1"}})]
    #_(let [api {:id "petstore"
               :path "/pet"
               :method :get}]
      (is (= "/pet" (swagger/build-url sut "/pet" :get ctx))))
    #_(let [api {:id "petstore"
               :path "/pet/{petId}"
               :method :get}]
      (is (= "/pet/1" (swagger/build-url sut api ctx))))))
