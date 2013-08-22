(ns darzana.sexp.test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test runtests testing)])
  (:require 
    [cemerick.cljs.test :as t]
    [darzana.sexp :as sexp]))

(deftest test-parse-routes
  (is (= (sexp/parse-routers '("L" ("L" "'GET" "/hoge" {"'params" "params"} ("L" "'let" ("V" "'context" ("L" "'create-context" "'params")))))) {})))
