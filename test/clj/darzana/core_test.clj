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

(deftest build-request-headers-token
  (testing "build oauth token header."
    (let [ ctx (context/create-context {:session {:access_token "hoge"}})
           api {:url "http://example.com/api/", :oauth-token :access_token}]
      (is (= "Bearer hoge") (get (build-request-headers ctx api) "Autorization") ))))

(deftest build-request-headers-no-token
  (testing "build no oauth token header."
    (let [ ctx (context/create-context {:session {}})
           api {:url "http://example.com/api/", :oauth-token :access_token}]
      (is (nil? (get (build-request-headers ctx api) "Autorization"))))))


(deftest build-request-headers-content-type
  (testing "build content-type header."
    (testing "When method is post, content-type is x-www-form-urlencoded"
      (let [ ctx (context/create-context {:session {}})
           api {:url "http://example.com/api/", :method :post}]
      (is (= (get (build-request-headers ctx api) "Content-Type") "application/x-www-form-urlencoded"))))
    (testing "When method is get, there is no Content-Type."
      (let [ ctx (context/create-context {:session {}})
             api {:url "http://example.com/api/"}]
      (is (nil? (get (build-request-headers ctx api) "Content-Type")))))))

(deftest if-contains-test
  (testing "if-contains"
    (testing ""
      (let [ ctx (context/create-context {:params {:name "ABC"}})
             to-lower-case (fn [context] (update-in context [:scope :params "name"] #(.toLowerCase %1)))
             res (if-contains ctx :name (to-lower-case)) ]
        (is (= {"name" "abc"} (get-in res [:scope :params])))))))


(deftest build-request-basic-auth
  (testing "if-contains"
    (testing ""
      (set-application-scope {:consumer-key "consumer" :consumer-secret "secret"})
      (let [ ctx (context/create-context {:session {}})
             api {:url "http://example.com/api/" :basic-auth [:consumer-key :consumer-secret]}
             request (build-request {} ctx api)]
        (is (= ["consumer" "secret"] (request :basic-auth)))))))

