(ns darzana.main
    (:gen-class)
    (:require [duct.core :as duct]
              [clojure.java.io :as io]))

(duct/load-hierarchy)

(defn -main [config & args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])]
    (-> (io/file config)
        duct/read-config
        (duct/prep keys)
        (duct/exec keys))))
