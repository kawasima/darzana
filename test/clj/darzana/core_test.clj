(ns darzana.core-test
  (:require 
    [clojure.test :refer :all]
    [clojure.data.json :as json]
    [darzana.core :refer :all]
    [darzana.context :as context]
    ))

(deftest build-url-test
  (testing ""
    (let [ ctx (context/create-context {:session {} :params {:id "1"}})
           api {:url "http://example.com/api/:id"}]
      (is (= (build-url ctx api) "http://example.com/api/1") ))))

(deftest build-url-other-param
  (testing ""
    (let [ ctx (context/create-context {:session {} :params {:id "1" :name "name"}})
           api {:url "http://example.com/api/:id"}]
      (is (= (build-url ctx api) "http://example.com/api/1") ))))

(deftest build-url-query-keys
  (testing ""
    (let [ ctx (context/create-context {:session {} :params {:id "1" :name "name"}})
           api {:url "http://example.com/api/:id", :method :get, :query-keys [:id :name]}]
      (is (= (build-url ctx api) "http://example.com/api/1?id=1&name=name") ))))

(deftest build-request-body-no-query-keys
  (testing "build request body."
    (let [ ctx (context/create-context {:session {} :params {:id "1" :name "name"}})
           api {:url "http://example.com/api/", :method :post}]
      (is (= (build-request-body ctx api) "")))))

(deftest build-request-body-url-encoding
  (testing "build request body."
    (let [ ctx (context/create-context {:session {} :params {:id "1" :name "name"}})
           api {:url "http://example.com/api/", :method :post, :query-keys [:id :name]}]
      (is (= (build-request-body ctx api) "id=1&name=name")))))

(deftest build-request-body-json
  (testing "build request body."
    (let [ ctx (context/create-context {:session {} :params {:id "1" :name "name"}})
           api {:url "http://example.com/api/", :method :post, :query-keys [:id :name], :content-type "application/json"}]
      (is (= (build-request-body ctx api) (json/write-str {:id "1" :name "name"}))))))


