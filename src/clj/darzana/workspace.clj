(ns darzana.workspace
  (:use
    [clj-jgit internal porcelain querying])
  (:require
    [clojure.java.io :as io]
    [me.raynes.fs :as fs])
  (:import
    [org.eclipse.jgit.api InitCommand]))

(def config (ref { :repo "repo"
                   :workspace "workspace"
                   :default "master"
                   :initial-resources "dev-resources"
                   :current "master"
                   :hook { :change []}}))

(defn initialize-repo []
  (git-clone (.. (io/file (@config :repo)) toURI toString) (io/file (@config :workspace) (@config :default)))
  (let [master (load-repo (io/file (@config :workspace) (@config :default)))]
    (doseq [f (fs/glob (io/file (@config :initial-resources)) "*")]
      (if (fs/directory? f)
        (fs/copy-dir f (.. master getRepository getWorkTree))
        (fs/copy f (io/file (.. master getRepository getWorkTree) (. f getName)))))
    (git-add master ".")
    (git-commit master "Initial commit")
    (-> master
      (.push)
      (.setRemote (.. (io/file (@config :repo)) toURI toString))
      (.add (@config :default))
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
  (let [ ws (make-workspace name)]
    (git-add ws ".")
    (when (some not-empty (vals (git-status ws)))
      (git-commit ws message)
      (-> ws
        (.push)
        (.setRemote (.. (io/file (@config :repo)) toURI toString))
        (.add (str "refs/heads/" name))
        (.call)))))

(defn delete-workspace [name]
  (let [repo (make-repo)]
    (git-branch-delete repo [name])
    (fs/delete-dir (io/file (@config :workspace) name))))

(defn delete-file [name path]
  (let [ws (make-workspace name)]
    (git-rm ws path)))

(defn current-dir []
  (io/file (@config :workspace) (@config :current)))

(defn change-workspace [name]
  (make-workspace name)
  (dosync
    (alter config assoc :current name))
  (doseq [hook (get-in @config [:hook :change])]
    (hook)))

(defn merge-workspace [name]
  (assert (not= name (@config :default)))
  (let [ default (make-workspace (@config :default))
         fetch-res (git-fetch default)
         commit-ref (-> fetch-res
                      (.getAdvertisedRef (str "refs/heads/" name))
                      (.getObjectId))]
    (git-merge default commit-ref)
    (-> default
      (.push)
      (.setRemote (.. (io/file (@config :repo)) toURI toString))
      (.add (str "refs/heads/" (@config :default)))
      (.call))))

