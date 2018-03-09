(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [eftest.runner :as eftest]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset reset-all]]
            [integrant.repl.state :refer [config system]]))

(duct/load-hierarchy)

(defn read-config
  ([] (read-config "dev/resources/dev.edn"))
  ([config-file]
   (integrant.repl/set-prep!
    #(duct/prep (duct/read-config config-file)))))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))
