(ns darzana.admin.git
  (:require
    [gring.core :as gring]
    [compojure.core :as compojure]
    [darzana.workspace :as workspace])
  (:import
    [org.eclipse.jgit.transport.resolver FileResolver]))

(def routes
  (fn [_]
    (binding [gring/repository-resolver
             (proxy [FileResolver] []
               (open [req name]
                 (.getRepository (workspace/make-repo))))]
      (apply compojure/routing _
        [gring/git-routes]))))

