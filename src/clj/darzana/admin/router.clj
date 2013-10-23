(ns darzana.admin.router
  (:use
    [darzana.router]
    [clojure.pprint]
    [compojure.core :as compojure :only (GET POST PUT DELETE defroutes)]
    [compojure.handler :as handler]
    [compojure.route :as route])
  (:require
    [clojure.data.json :as json]
    [clojure.data.xml :as xml]
    [clojure.java.io :as io]
    [darzana.workspace :as workspace]))

(defroutes routes
  (compojure/context "/router/:workspace" {{ws :workspace} :params}
    (GET "/" {}
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map (fn [_] (.getName _))
                  (filter #(.endsWith (.getName %) ".clj")
                    (file-seq (io/file (make-path ws))))))})

    (GET "/:router" [router]
      { :headers {"Content-Type" "application/json"}
        :body (json/write-str
                (map-indexed (fn [idx route]
                               { :id idx :router router
                                 :workspace ws
                                 :method (nth route 1) :path (nth route 2)})
                  (read-string
                    (str "[" (slurp (make-path ws router)) "]"))))})

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
          :body (json/write-str (assoc request-body :id (count routes)))}))

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

