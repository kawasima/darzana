(ns darzana.module.workspace
  (:require [integrant.core :as ig]
            (clj-jgit [internal  :refer :all]
                      [porcelain :refer :all]
                      [querying  :refer :all])
            [clojure.java.io :as io])
  (:import [org.eclipse.jgit.api InitCommand]))

(defn- initialize-repo [repo workspace default-branch]
  (git-clone (.. (io/file repo) toURI toString)
             (io/file workspace default-branch))
  (let [master (load-repo (io/file workspace default-branch))]
    (git-add master ".")
    (git-commit master "Initial commit")
    (-> master
        (.push)
        (.setRemote (.. (io/file repo) toURI toString))
        (.add default-branch)
        (.call))))

(defn- make-repo [repo workspace default-branch]
  (let [repo-dir (io/as-file repo)]
    (when-not (.exists repo-dir)
      (.mkdirs repo-dir)
      (-> (InitCommand.)
          (.setDirectory repo-dir)
          (.setBare true)
          (.call))
      (initialize-repo repo workspace default-branch))
    (load-repo repo-dir))
  )

(defn make-workspace [workspace name]
  (let [repo (make-repo)
        workspace-dir (io/file workspace name)]
    (if (.exists workspace-dir)
      (load-repo workspace-dir)
      (do
        (git-branch-create repo name)
        (git-clone
          (.. repo getRepository getDirectory toURI toString)
          (io/file workspace name)
          "origin" name)))))

(defn commit-workspace [component name message]
    (let [ws (make-workspace (:workspace component) name)]
      (git-add ws ".")
      (when (some not-empty (vals (git-status ws)))
        (git-commit ws message)
        (-> ws
            (.push)
            (.setRemote (.. (io/file (:repo component)) toURI toSting))
            (.add (str "refs/heads/" name))
            (.call)))))

(defn delete-workspace [{:keys [repo workspace default-branch]} name]
  (let [repo (make-repo repo workspace default-branch)]))

(defn current-dir [component]
  (io/file (:workspace component) (:current-branch component)))

(defrecord Workspace [repo workspace default-branch initial-resources
                      current-branch hook])

(defmethod ig/init-key :darzana/workspace [_ options]
  (map->Workspace {:repo "repo"
                   :workspace "workspace"
                   :default-branch "master"
                   :initial-resources "dev-resources"
                   :current-branch "master"
                   :hook {:change []}}))
