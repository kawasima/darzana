(ns darzana.command.api-test
  (:require [com.stuartsierra.component :as component]
            [darzana.component.swagger :as swagger]
            [darzana.component.okhttp :as okhttp]
            [darzana.component.runtime :as runtime]
            [darzana.command.api :as sut]
            [clojure.test :refer :all]))

(deftest call-api
  (let [system (-> (component/system-map
                    :http-client (okhttp/okhttp-component {})
                    :api-spec (swagger/swagger-component {:swagger-path "dev/resources/swagger"})
                    :runtime (runtime/runtime-component {}))
                   (component/system-using
                    {:runtime [:http-client :api-spec]})
                   (component/start))
        runtime (:runtime system)
        ctx (runtime/create-context runtime {:params {:petId "1"}})
        api {:id "petstore" :path "/pet" :method :post}]
    (println (sut/call-api ctx api))))
