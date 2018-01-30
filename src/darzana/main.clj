(ns darzana.main
    (:gen-class)
    (:require [duct.core :as duct]
              [clojure.java.io :as io]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])]
    (-> (io/resource "darzana/config.edn")
        duct/read-config
        (duct/prep keys)
        (duct/exec keys))))
