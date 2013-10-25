(ns darzana.admin.workspace
  (:use
    [compojure.core :as compojure :only (GET POST PUT DELETE defroutes)]
    [darzana.workspace]
    [clj-jgit.porcelain :only [git-branch-list]])
  (:require
    [clojure.data.json :as json]))

(defroutes routes
  (compojure/context "/workspace" []
    (GET "/" {}
      (let [ repo (make-repo)
             branches (map (fn [_]
                             (let [name (clojure.string/replace (.getName _) #"^refs/heads/" "")]
                               { :id name
                                 :name name
                                 :default (= name (@config :default))
                                 :current (= name (@config :current))}))
                        (git-branch-list repo))]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str branches)}))

    (GET "/:name" [name]
      (let [ repo (make-repo)
             workspace { :id name
                         :name name
                         :current (= name (@config :current))
                         :default (= name (@config :default))}]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str workspace)}))

    (POST "/" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             name (request-body "name")]
        (make-workspace name)
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str (assoc request-body "id" name))}))

    (PUT "/:id" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             name (request-body "name")]
        (change-workspace name)
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str request-body)}))

    (PUT "/:id/merge" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             name (request-body "name")]
        (merge-workspace name)
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str request-body)}))
    
    (DELETE "/:id" [id]
      (delete-workspace id)
      { :headers {"Content-Type" "application/json; charset=UTF-8"}
        :body (json/write-str {:id id :name id})})))

