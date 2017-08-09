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
    (http-client/request (:darzana.http-client/okhttp system)
                         {:url "http://github.com"}
                         #(deliver response %)
                         #(println %))
    (println @response)))

(deftest test-spec
  (def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
  (s/def ::email-type (s/with-gen
                        (s/and string? #(re-matches email-regex %))
                        #(gen/fmap (fn [[a b]] (str a "@" b ".jp"))
                                   (gen/tuple (gen/string-alphanumeric)
                                              (gen/string-alphanumeric)))))
  (s/def ::email ::email-type)
  (s/def ::name string?)
  (s/def ::age int?)
  (s/def ::person (s/keys :req [::name ::age ::email]))
  (pprint (gen/sample (s/gen ::person) 100))


  )
