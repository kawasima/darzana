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

(deftest merge-scope-normal
  (testing "Merge scope normally."
    (let [ctx (assoc-in (create-context {:session {} :params {}}) [:scope :session :a] 1)]
      (is (= (merge-scope ctx) {:a 1})))))

(deftest merge-scope-overwrite-context
  (testing "Merge scope."
    (let [ctx (assoc-in (create-context {:session {:a 3} :params {}}) [:scope :session :a] 1)]
      (is (= (merge-scope ctx) {:a 1})))))

(deftest merge-scope-priority
  (testing "Merge scope."
    (let [ctx (reduce #(apply assoc-in %1 %2) (create-context {:session {:a 2} :params {}})
            [[[:scope :session :a] 1 ] [[:scope :params :a] 8]])]
      (is (= (merge-scope ctx) {:a 8})))))

