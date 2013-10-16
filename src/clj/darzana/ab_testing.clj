(ns darzana.ab-testing
  (:use
    [darzana.router :only (serialize-component)])
  (:require
    [org.httpkit.client :as http]
    [clojure.data.json :as json]))

(def config (ref { :url-base "http://localhost:5000"}))

(defn participate-sixpack
  "Sixpack participate API"
  [experiment client-id alternatives]
  (let [response @(http/get (str (@config :url-base) "/participate")
                       { :query-params
                         { :experiment experiment
                           :client_id  client-id
                           :alternatives alternatives }})]
    (if (= (response :status) 200)
      (get-in (json/read-str (response :body)) ["alternative" "name"])
      (first alternatives))))

(defn convert-sixpack [experiment client-id]
  "Sixpack convert API"
  (http/get (str (@config :url-base) "/convert")
              { :query-params
                { :experiment experiment
                  :client_id  client-id}}))

(defmacro ab-testing-alternative [name & clauses]
  `(fn [context# alt#] (if (= alt# ~name) (-> context# ~@clauses) context#)))

(defmacro ab-testing-participate [context test-id & alternatives]
  (let [ alt-names (vec (map second alternatives))]
    `(let [ 
            ctx# (if-let [ client-id# (get-in ~context [:scope :cookies "darzana-client-id" :value])]
                   ~context
                   (assoc-in ~context [:scope :cookies "darzana-client-id" :value]
                     (str (java.util.UUID/randomUUID))))
            client-id# (get-in ctx# [:scope :cookies "darzana-client-id" :value])
            alt-name# (participate-sixpack ~test-id client-id# ~alt-names) ]
       (reduce merge (for [alt-clause# [~@alternatives]] (alt-clause# ctx# alt-name#))))))

(defn ab-testing-convert [context test-id]
  (if-let [client-id (get-in context [:scope :cookies "darzana-client-id" :value])]
    (convert-sixpack test-id client-id))
  context)

;;;
;;; Define block UI serilize / deserialize
;;;
(ns darzana.router)

(defmethod serialize-component 'ab-testing-participate [s r]
  (let [ elm [:block {:type "ab_testing_participate"}
               [:mutation {:alternative (- (count s) 2)}]
               [:title {:name "test-id"} (name (nth s 1))]]
         alts (map-indexed
                (fn [i _]
                  [[:title {:name (str "ALT_NAME" i)} (nth _ 1)]
                   [:statement {:name (str "ALT" i)}
                    (let [alt-s (drop 2 _)]
                      (serialize-component (first alt-s) (rest alt-s)))]])
                (drop 2 s))
         elm (reduce #(apply conj %1 %2) elm alts)]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod serialize-component 'ab-testing-convert [s r]
  (let [ elm [:block {:type "ab_testing_convert"}
               [:title {:name "test-id"} (name (nth s 1))]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod deserialize-block "ab_testing_participate" [block]
  (let [ alt-count (some-> block
                     (find-child :mutation)
                     (get-in [:attrs :alternative])
                     (Integer/parseInt))
         sexp (seq
                (apply conj ['ab-testing-participate
                              (some-> block
                                (find-child :title {:name "test-id"})
                                (get :content) (first))]
                  (for [x (range 0 alt-count)]
                    (seq [ 'ab-testing-alternative
                           (some-> block
                             (find-child :title {:name (str "ALT_NAME" x)})
                             (get :content) (first))
                           (some-> block
                             (find-child :statement {:name (str "ALT" x)})
                             (find-child :block)
                             (deserialize-block)
                             (deserialize-chained-block))]))))]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

(defmethod deserialize-block "ab_testing_convert" [block]
  (let [ sexp (seq ['ab-testing-convert
                     (some-> block
                       (find-child :title {:name "test-id"})
                       (get :content) (first))])]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

