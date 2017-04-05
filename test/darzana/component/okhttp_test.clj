(ns darzana.component.okhttp-test
  (:require [com.stuartsierra.component :as component]
            [darzana.component.okhttp :as okhttp]
            [darzana.component.http-client :as http-client]
            [clojure.test :refer :all]))

(deftest test-req
  (let [system (-> (component/system-map
                    :http-client (okhttp/okhttp-component {})
)
                   (component/start))
         response (promise)]

    (http-client/request (:http-client system)
                         {:url "http://github.com"}
                         #(deliver response %)
                         #(println %))
    (println @response)))
