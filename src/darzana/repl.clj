(ns darzana.repl
  (:refer-clojure :exclude [test])
  (:gen-class)
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset reset-all]]
            [integrant.repl.state :refer [config system]]))

(duct/load-hierarchy)

(defn -main [config & args]
  (let [keys (or (duct/parse-keys args) [:duct/daemon])]
    (integrant.repl/set-prep!
     (-> (io/file config)
         duct/read-config
         (duct/prep keys)))))
