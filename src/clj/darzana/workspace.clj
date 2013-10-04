(ns darzana.workspace
  (:use
    [clj-jgit.internal]
    [clj-jgit.porcelain]
    [clj-jgit.querying]
    [compojure.core :as compojure :only (GET POST PUT DELETE defroutes)])
  (:require
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [me.raynes.fs :as fs])
  (:import
    [org.eclipse.jgit.api InitCommand]))

(def config (ref { :repo "repo"
                   :workspace "workspace"
                   :head "master"
                   :initial-resources "dev-resources"
                   :current "master"
                   :hook { :change []}}))

(defn initialize-repo []
  (git-clone (.. (io/file (@config :repo)) toURI toString) (io/file (@config :workspace) (@config :head)))
  (let [master (load-repo (io/file (@config :workspace) (@config :head)))]
    (doseq [f (fs/glob (io/file (@config :initial-resources)) "*")]
      (if (fs/directory? f)
        (fs/copy-dir f (.. master getRepository getWorkTree))
        (fs/copy f (io/file (.. master getRepository getWorkTree) (. f getName)))))
    (git-add master ".")
    (git-commit master "Initial commit")
    (-> master
      (.push)
      (.setRemote (.. (io/file (@config :repo)) toURI toString))
      (.add (@config :head))
      (.call))))

(defn make-repo []
  (let [repo-dir (@config :repo)]
    (when-not (fs/exists? repo-dir)
      (do
        (fs/mkdirs repo-dir)
        (-> (InitCommand.)
          (.setDirectory (io/file repo-dir))
          (.setBare true)
          (.call))
        (initialize-repo)))
    (load-repo repo-dir)))

(defn make-workspace [name]
  (let [ repo (make-repo)
         workspace-dir (io/file (@config :workspace) name)]
    (if (fs/exists? workspace-dir) 
      (load-repo workspace-dir)
      (do
        (git-branch-create repo name)
        (git-clone
          (.. repo getRepository getDirectory toURI toString)
          (io/file (@config :workspace) name)
          "origin" name)))))

(defn commit-workspace [name message]
  (let [ ws (load-repo (io/file (@config :workspace) name))]
    (git-add ws ".")
    (println (vals (git-status ws)))
    (if (some not-empty (vals (git-status ws)))
      (git-commit ws message)
      (-> ws
        (.push)
        (.setRemote (.. (io/file (@config :repo)) toURI toString))
        (.add (str "refs/heads/" name))
        (.call)))))

(defn delete-workspace [name]
  (let [repo (load-repo (@config :repo))]
    (git-branch-delete repo [name])
    (fs/delete-dir (io/file (@config :workspace) name))))

(defn current-dir []
  (io/file (@config :workspace) (@config :current)))

(defn change-workspace [name]
  (make-workspace name)
  (println "change-workspace" name)
  (dosync
    (alter config assoc :current name))
  (doseq [hook (get-in @config [:hook :change])]
    (hook)))

(defroutes routes
  (compojure/context "/workspace" []
    (GET "/" {}
      (let [ repo (make-repo)
             branches (map (fn [_]
                             (let [name (clojure.string/replace (.getName _) #"^refs/heads/" "")]
                               { :id name
                                 :name name
                                 :current (= name (@config :current))}))
                        (git-branch-list repo))]
        { :headers {"Content-Type" "application/json; charset=UTF-8"}
          :body (json/write-str branches)}))
    (POST "/" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             name (request-body "name")]
        (make-workspace name))
      { :headers {"Content-Type" "application/json; charset=UTF-8"}})
    (PUT "/:id" [:as r]
      (let [ request-body (json/read-str (slurp (r :body)))
             name (request-body "name")]
        (change-workspace name))
      { :headers {"Content-Type" "application/json; charset=UTF-8"}})
    (DELETE "/:id" [id]
      (delete-workspace id)
      { :headers {"Content-Type" "application/json; charset=UTF-8"}})))

