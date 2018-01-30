(ns darzana.http-client.okhttp-test
  (:require [integrant.core :as ig]
            [darzana.runtime :as runtime]
            [darzana.http-client.okhttp :as okhttp]
            [darzana.http-client :as http-client]
            [clojure.test :refer :all]
            [clojure.pprint :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(deftest test-req
  (let [config   {:darzana.api-spec/swagger   {:swagger-path "dev/resources/darzana"}
                  :darzana/runtime            {:routes-path "dev/resources/scripts"
                                               :commands    [['darzana.command.api :as 'api]
                                                             ['darzana.command.control :as 'control]
                                                             ['darzana.command.mapper :as 'mapper]
                                                             ['darzana.command.renderer :as 'renderer]]}
                  :darzana.http-client/okhttp {}}
        system   (ig/init config)
        runtime  (:darzana/runtime system)
        ctx      (runtime/create-context runtime {:params {:petId "1"}})
        response (promise)]
    #_(http-client/request (:darzana.http-client/okhttp system)
                         {:url "http://github.com"}
                         #(deliver response %)
                         #(println %))))
