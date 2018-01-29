(ns darzana.command.mapper-test
  (:require [integrant.core :as ig]
            [darzana.command.mapper :as sut]
            [darzana.runtime :as runtime]
            [clojure.test :refer :all]))


(deftest mapper
  (let [config  {:darzana.api-spec/swagger {:swagger-path "dev/resources/darzana"}
                 :darzana/runtime {:routes-path "dev/resources/scripts"
                                   :commands [['darzana.command.api :as 'api]
                                              ['darzana.command.control :as 'control]
                                              ['darzana.command.mapper :as 'mapper]
                                              ['darzana.command.renderer :as 'renderer]]
                                   :validator (ig/ref :darzana/validator)
                                   :api-spec (ig/ref :darzana/api-spec)}
                 :darzana.validator/hibernate-validator {}}
        system  (ig/init config)
        runtime (:darzana/runtime system)
        ctx (runtime/create-context runtime {:params {:id "1" :name "I'm cat"}})
        api {:id "petstore" :path "/pet" :method :post}]
    (sut/read-value ctx {:scope :params} {:var :pet :type io.swagger.model.Pet})))
