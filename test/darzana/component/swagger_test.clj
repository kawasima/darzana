(ns darzana.component.swagger-test
  (:require [com.stuartsierra.component :as component]
            [darzana.context :as context]
            [darzana.component.runtime :as runtime]
            [darzana.component.swagger :as swagger]
            [clojure.test :refer :all]))

(deftest swagger-read
  (let [system (-> (component/system-map
                    :api-spec (swagger/swagger-component {:swagger-path "dev/resources/swagger"})
                    :runtime (runtime/runtime-component {}))
                   (component/system-using
                    {:runtime [:api-spec]})
                   (component/start))
        swagger (:api-spec system)
        runtime (:runtime system)
        ctx (runtime/create-context runtime {:params {:petId "1"}})]
    #_(let [api {:id "petstore"
               :path "/pet"
               :method :get}]
      (is (= "/pet" (swagger/build-url sut "/pet" :get ctx))))
    #_(let [api {:id "petstore"
               :path "/pet/{petId}"
               :method :get}]
      (is (= "/pet/1" (swagger/build-url sut api ctx))))))
