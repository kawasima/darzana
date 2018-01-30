(ns darzana.command.mapper-test
  (:require [integrant.core :as ig]
            [duct.core :as duct]
            [darzana.command.mapper :as sut]
            [darzana.api-spec.swagger]
            [darzana.validator.hibernate-validator]
            [darzana.runtime :as runtime]
            [clojure.test :refer :all]))

(duct/load-hierarchy)

(deftest mapper
  (let [config  {:darzana.api-spec/swagger {:swagger-path "dev/resources/darzana"}
                 :darzana.validator/hibernate-validator {}
                 :darzana/runtime {:routes-path "dev/resources/scripts"
                                   :commands [['darzana.command.api :as 'api]
                                              ['darzana.command.control :as 'control]
                                              ['darzana.command.mapper :as 'mapper]
                                              ['darzana.command.renderer :as 'renderer]]
                                   :validator (ig/ref :darzana/validator)
                                   :api-spec  (ig/ref :darzana/api-spec)}}
        system  (ig/init config)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:id "1" :name "I'm cat"}})
        api {:id "petstore" :path "/pet" :method :post}]
    (let [pet (-> (sut/read-value ctx {:scope :params} {:var :pet :type io.swagger.model.Pet})
                  (get-in [:scope :page :pet]))]
      (is (= 1         (.getId pet))   "ID of the pet is 1")
      (is (= "I'm cat" (.getName pet)) ))))
