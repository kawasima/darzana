(ns darzana.main
    (:gen-class)
    (:require [duct.core :as duct]
              [clojure.java.io :as io]))

(defn -main [& args]
  (duct/exec (duct/read-config (io/resource "darzana/config.edn"))))
