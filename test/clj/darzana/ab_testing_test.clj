(ns darzana.ab-testing-test
  (:use
    [darzana.context :only (create-context)])
  (:require
    [clojure.test :refer :all]
    [darzana.ab-testing :refer :all]))

(deftest participate
  (testing "participate macro."
    (let [ctx (assoc-in (create-context {:session {} :params {}}) [:scope :session :a] 1)]
      (is (=
            (-> ctx (ab-testing-participate "new test"
                      (ab-testing-alternative "Blue" (assoc-in [:scope :params "color"] "BLUE"))))
            {"a" 1, "b" 2})))))

(ns darzana.ab-testing)
(defn participate-sixpack [experiment client-id alternatives]
  (rand-nth alternatives))

