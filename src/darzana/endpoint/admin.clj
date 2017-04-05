(ns darzana.endpoint.admin
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(defn admin-endpoint [config]
  (context "/example" []
    (GET "/" []
      (io/resource "darzana/endpoint/example/example.html"))))
