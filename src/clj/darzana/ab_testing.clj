(ns darzana.ab-testing
  (:use
    [darzana.router :only (serialize-component)])
  (:require
    [org.httpkit.client :as http]
    [clojure.data.json :as json]
    ))

(def config (ref { :url-base "http://localhost:5000"}))

(defn participate-sixpack [experiment client-id alternatives]
  (let [response @(http/get (str (@config :url-base) "/participate")
                       { :query-params
                         { :experiment experiment
                           :client_id  client-id
                           :alternatives alternatives }})]
    (get-in (json/read-str (response :body)) ["alternative" "name"])))

(defn convert-sixpack [experiment client-id]
  (http/get (str (@config :url-base) "/convert")
              { :query-params
                { :experiment experiment
                  :client_id  client-id}}))

(defmacro ab-testing-alternative [name & clauses]
  `(fn [context# alt#] (if (= alt# ~name) (-> context# ~@clauses) context#)))

(defmacro ab-testing-participate [context test-id & alternatives]
  (let [ client-id (get-in context [:scope :cookie "darzana-client-id"]
                      (str (java.util.UUID/randomUUID)))
         alt-names (vec (map second alternatives))]
    `(let [alt-name# (participate-sixpack ~test-id ~client-id ~alt-names)]
       (println alt-name#)
       (reduce merge (for [alt-clause# [~@alternatives]] (alt-clause# ~context alt-name#))))))

(defn ab-testing-convert [context test-id]
  (if-let [client-id (get-in context [:scope :cookie "darzana-client-id"])]
    (convert-sixpack test-id client-id))
  context)

(ns darzana.router)

(defmethod serialize-component 'ab-testing-participate [s r]
  (let [elm [:block {:type "ab_testing_participate"}
              [:title {:name "test-id"} (name (nth s 1))]
              [:statement {:name "contains"} (serialize-component (nth s 2) nil)]
              [:statement {:name "not-contains"} (serialize-component (nth s 3) nil)]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

