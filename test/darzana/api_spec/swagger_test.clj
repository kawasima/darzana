(ns darzana.api-spec.swagger-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [duct.core :as duct]
            [darzana.context :as context]
            [darzana.runtime :as runtime]
            [darzana.api-spec :as api-spec]
            [darzana.api-spec.swagger :as swagger]))

(duct/load-hierarchy)

(deftest swagger-read
  (let [config {:darzana.api-spec/swagger {:swagger-path "dev/resources/swagger"}}
        system (ig/init config)
        sut    (:darzana.api-spec/swagger system)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:petId "1"}})
        api {:id :petstore :path "/pets" :method :get}]
    (is (not (nil? sut)))
    (is (= "http://petstore.swagger.io/v1/pets"
           (:url (api-spec/build-request sut api ctx))))))

(deftest query-param
  (let [config {:darzana.api-spec/swagger {:swagger-path "dev/resources/swagger"}
                :darzana/runtime {}}
        system (ig/init config)
        sut    (:darzana.api-spec/swagger system)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:limit "10"}})
        api {:id :petstore :path "/pets" :method :get}]
    (is (not (nil? sut)))
    (is (= "http://petstore.swagger.io/v1/pets?limit=10"
           (:url (api-spec/build-request sut api ctx))))))

(deftest path-param
  (let [config {:darzana.api-spec/swagger {:swagger-path "dev/resources/swagger"}
                :darzana/runtime {}}
        system (ig/init config)
        sut    (:darzana.api-spec/swagger system)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:petId "10"}})
        api {:id :petstore :path "/pets/{petId}" :method :get}]
    (is (not (nil? sut)))
    (is (= "http://petstore.swagger.io/v1/pets/10"
           (:url (api-spec/build-request sut api ctx))))))
