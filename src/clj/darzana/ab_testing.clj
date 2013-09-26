(ns darzana.ab-testing
  (:use
    [darzana.router :only (serialize-component)])
  (:require
    [org.httpkit.client :as http]
    [clojure.data.json :as json]
    ))

(def config (ref { :url-base "http://localhost:5000"}))

(defn participate-sixpack [experiment client-id alternatives]
  (let [response @(http/get (str (@config :url-base) "/participate"
                       { :query-params
                         { :experiment experiment
                           :client-id  client-id
                           :alternatives alternatives }}))]
    (get-in ["alternative" "name"] (json/read-str (response :body)))))

(defn convert-sixpack [experiment client-id]
  (http/get (str (@config :url-base) "/convert"
              { :query-params
                { :experiment experiment
                  :client-id  client-id}})))

(defmacro ab-testing-alternative [name & clauses]
  `(fn [context# alt#] (if (= alt# ~name) (-> context# ~@clauses) context#)))

(defmacro ab-testing-participate [context test-id & alternatives]
  (let [ client-id (get-in context [:scope :cookie "darzana-client-id"]
                      (str (java.util.UUID/randomUUID)))
         alt-names (vec (map second alternatives))]
    `(let [alt-name# (participate-sixpack ~test-id ~client-id ~alt-names)]
       (reduce merge (for [alt-clause# [~@alternatives]] (alt-clause# ~context alt-name#))))))

(defn ab-testing-convert [context test-id]
  (if-let [client-id (get-in context [:scope :cookie "darzana-client-id"])]
    (convert-sixpack test-id client-id))
  context)

;; (defmarga GET "/experiment"
;;   (ab-testing-participate "test-id"
;;     (ab-testing-alternative "" )
;;
(ns darzana.router)

(defmethod serialize-component 'ab-testing-participate [s r])
