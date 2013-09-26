(ns darzana.sexp.test
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test runtests testing)])
  (:require 
    [cemerick.cljs.test :as t]))

(deftest test-if-success
  (is (= (Blockly.Language/if_success :help_url) "")))

(deftest test-backbone
  (println (new darzana.model.Template)))
