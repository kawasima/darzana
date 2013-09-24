(ns darzana.router
  (:use
    [compojure.core :as compojure :only (GET POST PUT DELETE defroutes)]
    [clojure.pprint])
  (:require
    [compojure.handler :as handler]
    [compojure.route :as route]
    [clojure.data.json :as json]
    [clojure.data.xml :as xml]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [me.raynes.fs :as fs]
    [darzana.workspace :as workspace]))

(defn make-path
  ([] (.. (io/file (workspace/current-dir) "router") getPath))
  ([ws] (.. (io/file (@workspace/config :workspace) ws "router") getPath))
  ([ws router]
    (.. (io/file (@workspace/config :workspace) ws "router" (str router ".clj")) getPath)))

(def route-namespace (ref nil))

(defn load-app-routes []
  (if (nil? @route-namespace) (dosync (ref-set route-namespace *ns*)))
  (binding [*ns* @route-namespace]
    (load-string
      (string/join " "
        (flatten 
          [ "(use '[darzana.core] '[compojure.core :as compojure :only (GET POST PUT ANY defroutes)])"
            "(defroutes app-routes"
            (map #(slurp %)
              (fs/glob (io/file (make-path)) "*.clj"))
            ")"])))))

(defmulti serialize-api (fn [x] (coll? x)))

(defmethod serialize-api true [apis]
  [:block {:type "api_list" :inline false}
    [:mutation {:items (count apis)}]
    (map-indexed (fn [idx api]
                   [:value {:name (str "API" idx)}
                     (serialize-api api)]) apis)])

(defmethod serialize-api false [api]
  [:block {:type "api"}
    [:title {:name "api"} api]])

(defmulti serialize-component (fn [s r] (first s)))

(defmethod serialize-component 'call-api [s r]
  (let [elm [:block {:type "call_api" :inline true}
              [:value {:name "API"}
                (serialize-api (second s))]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod serialize-component 'render [s r]
  [:block {:type "render"} [:title {:name "template"} (second s)]])

(defmethod serialize-component 'redirect [s r]
  [:block {:type "redirect"} [:title {:name "url"} (second s)]])

(defmethod serialize-component 'if-success [s r]
  (let [elm [:block {:type "if_success"}
                [:statement {:name "success"} (serialize-component (nth s 1) nil)]
                [:statement {:name "error"}   (serialize-component (nth s 2) nil)]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod serialize-component 'if-contains [s r]
  (let [elm [:block {:type "if_contains"}
              [:title {:name "key"} (name (nth s 1))]
              [:statement {:name "contains"} (serialize-component (nth s 2) nil)]
              [:statement {:name "not-contains"} (serialize-component (nth s 3) nil)]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod serialize-component 'store-session [s r]
  (let [elm [:block {:type "store_session"}
              [:title {:name "session-key"} (name (nth s 1))]
              [:title {:name "context-key"} (clojure.string/join " " (map name (nth s 2)))]]]
    (if (empty? r) elm
      (conj elm [:next (serialize-component (first r) (rest r))]))))

(defmethod serialize-component '-> [s r]
  (let [chain-block (rest s)]
    (when (not-empty chain-block)
      (serialize-component (first chain-block) (rest chain-block)))))

(defmethod serialize-component :default [s r] (throw (Exception. (str "Unknown component:" s))))

(defn serialize-statement [sexp]
  (if (not-empty sexp)
  [:statement {:name "component"} 
    (serialize-component (first sexp) (rest sexp))]))

(defn serialize [sexp]
  (xml/emit-str
    (xml/sexp-as-element 
      [:xml {}
        [:block {:type "marga" :x 180 :y 20}
          [:title {:name "method"} (nth sexp 1)]
          [:title {:name "path"} (nth sexp 2)]
          (serialize-statement (drop 3 sexp))]])))

(defn filter-children
  ([node tag-name] (filter #(= (get % :tag) tag-name) (get node :content)))
  ([node tag-name attrs]
    (filter
      #(every? (fn [x] (= (get-in % [:attrs (first x)]) (second x))) attrs)
      (filter-children node tag-name))))

(defn find-child
  ([node tag-name] (first (filter-children node tag-name)))
  ([node tag-name attrs] (first (filter-children node tag-name attrs))))

(def deserialize-block)

(defn deserialize-statement [stmt]
  (seq (reduce #(apply conj %1 %2) []
       (map deserialize-block (get stmt :content)))))

(defn deserialize-next [next]
  (first (map deserialize-block (filter-children next :block))))

(defn get-text [node]
  (let [children (get node :content)]
    (reduce str (filter string? children))))

(defn deserialize-api-value [value]
  (deserialize-block (find-child value :block)))

(defn deserialize-chained-block [chained-block]
  (cond
    (coll? chained-block) (if (> (count chained-block) 1)
                            (seq (reduce conj ['->] chained-block))
                            (first chained-block))
    (nil? chained-block) (seq ['->])
    :else chained-block))

(defmulti deserialize-block (fn [block] (get-in block [:attrs :type])))

(defmethod deserialize-block "marga" [block]
  (let [ props (reduce (fn [memo item] (assoc memo (get-in item [:attrs :name]) (first (get item :content)))) {}
                 (filter-children block :title))
         sexp ['defmarga (symbol (get props "method" 'GET)) (get props "path")]]
    (seq (reduce conj sexp
           (deserialize-statement (find-child block :statement))))))

(defmethod deserialize-block "api_list" [block]
  (apply conj [] (map #(deserialize-block (first (get % :content))) (filter-children block :value))))

(defmethod deserialize-block "api" [block]
  (symbol (get-text (find-child block :title))))

(defmethod deserialize-block "call_api" [block]
  (let [sexp (seq ['call-api (deserialize-api-value (first (filter-children block :value)))])]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

(defmethod deserialize-block "render" [block]
  [(seq ['render (get-text (find-child block :title))])])

(defmethod deserialize-block "redirect" [block]
  [(seq ['redirect (get-text (find-child block :title))])])

(defmethod deserialize-block "if_success" [block]
  (let [sexp (seq ['if-success
                    (deserialize-block (find-child (find-child block :statement {:name "success"}) :block))
                    (deserialize-block (find-child (find-child block :statement {:name "error"}) :block))])]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

(defmethod deserialize-block "if_contains" [block]
  (let [sexp (seq ['if-contains
                    (-> block (find-child :title {:name "key"}) (get-text) (keyword))
                    (-> block (find-child :statement {:name "contains"})
                      (find-child :block) (deserialize-block)
                      (deserialize-chained-block))
                    (-> block (find-child :statement {:name "not-contains"})
                      (find-child :block) (deserialize-block)
                      (deserialize-chained-block))])]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

(defmethod deserialize-block "store_session" [block]
  (let [sexp (seq ['store-session
                    (keyword (get-text (find-child block :title {:name "session-key"})))
                    (vec (map keyword (clojure.string/split (get-text (find-child block :title {:name "context-key"})) #"\s+")))])]
    (reduce #(apply conj %1 %2) [sexp]
      (map deserialize-next (filter-children block :next)))))

(defmethod deserialize-block :default [block] nil)

(defmulti deserialize (fn [el] (get el :tag)))

(defmethod deserialize :xml [el] (deserialize (first (get el :content))))

(defmethod deserialize :block [el] (deserialize-block el))

(dosync (alter workspace/config update-in [:hook :change] conj
          load-app-routes))

(defroutes routes
  (compojure/context "/router/:workspace" {{ws :workspace} :params}
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] (.getName _))
                  (filter #(.endsWith (.getName %) ".clj") (file-seq (io/file (make-path ws))))))})

    (GET "/:router" [router]
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map-indexed (fn [idx route]
                               { :id idx :router router
                                 :workspace ws
                                 :method (nth route 1) :path (nth route 2)})
                  (read-string
                    (str "[" (slurp  (make-path ws router)) "]"))))})

    (GET "/:router/:id" [router id]
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                { :id id
                  :router router
                  :workspace ws
                  :xml (serialize
                         (nth (read-string
                                (str "[" (slurp  (make-path ws router)) "]")) (Integer. id)))})})

    (POST "/:router" [router :as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             router-path (make-path ws router)
             routes (read-string
                     (str "[" (slurp router-path) "]"))]
        (with-open [wrtr (io/writer router-path)]
          (doseq [route routes] (pprint route wrtr))
          (pprint (seq ['defmarga (symbol (get request-body "method")) (get request-body "path")]) wrtr))
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str (assoc request-body :id (count routes)) request-body)}))

    (DELETE "/:router/:id" [router id :as r]
      (let [ router-path (make-path ws router)
             routes (read-string
                     (str "[" (slurp router-path) "]"))]
        (with-open [wrtr (io/writer router-path)]
          (doall
            (map-indexed (fn [idx route]
                           (if (not= idx (Integer. id)) (pprint route wrtr))) routes)))
          { :headers {"Content-Type" "application/json"}
            :body (json/write-str {:status "successful"})}))

    (PUT "/:router/:id" [router id :as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             router-path (make-path ws router)
             routes (read-string
                     (str "[" (slurp router-path) "]"))
             updated-route (deserialize (xml/parse-str (get request-body "xml")))]
        (with-open [wrtr (io/writer router-path)]
          (doall
            (map-indexed (fn [idx route]
                           (pprint (if (= idx (Integer. id)) updated-route route) wrtr)) routes)))
        (workspace/commit-workspace (request-body "workspace") "Modify router.")
        { :headers {"Content-Type" "application/json"}
          :body (json/write-str request-body)}))))

