(ns darzana.context-test
  (:require [clojure.test :refer :all]
            [darzana.context :refer :all]))

(deftest keyword-to-str-map
  (testing "convert keyword to string."
    (is (= (keyword-to-str {:a 1, :b 2}) {"a" 1, "b" 2}))))

(deftest keyword-to-str-map-complex
  (testing "convert keyword to string."
    (is (= (keyword-to-str {:a 1, :b {:b1 "2" :b2 "3"}}) {"a" 1, "b" {"b1" "2", "b2" "3"}}))))

(deftest keyword-to-str-empty
  (testing "convert keyword to string."
    (is (= (keyword-to-str {}) {}))
    (is (= (keyword-to-str nil) nil))))

(deftest keyword-to-str-vector
  (testing "convert keyword to string."
    (is (= (keyword-to-str [:a :b :c "d" :e]) ["a" "b" "c" "d" "e"]))))

(deftest keyword-to-str-seq
  (testing "convert keyword to string."
    (is (= (keyword-to-str '(:a :b :c "d" :e)) '("a" "b" "c" "d" "e")))))
