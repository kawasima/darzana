(ns darzana.admin.git
  (:use
    [clj-jgit.internal]
    [clj-jgit.porcelain]
    [clj-jgit.querying]
    [compojure.core :as compojure :only (GET POST PUT ANY defroutes)])
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [ring.util.response :as response]
    [ring.util.io :as ring-io]
    [darzana.workspace :as workspace])
  (:import
    [java.io PrintWriter]
    [org.eclipse.jgit.transport UploadPack ReceivePack PacketLineOut
      RefAdvertiser$PacketLineOutRefAdvertiser]))

(def config (atom { :upload-pack true 
                    :receive-pack true
                    :repo-name "darzana-app"}))

;; packet-line handling
(def pkt-flush "0000")

(defn hdr-nocache [resp]
  (-> resp
    (response/header "Expires" "Fri, 01 Jan 1980 00:00:00 GMT")
    (response/header "Pragma"  "no-cache")
    (response/header "Cache-Control" "no-cache, max-age=0, must-revalidate")))

(defn hdr-cache-forever [resp]
  (let [ now (.getTime (new java.util.Date)) ]
    (-> resp
      (response/header "Expires" (str now))
      (response/header "Date"    (str (+ now 31536000)))
      (response/header "Cache-Control" "public, max-age=31536000"))))

(defn has-access
  ([request rpc] (has-access request rpc false))
  ([request rpc check-content-type]
    (cond
      (and check-content-type
        (not= (request :content-type) (str "application/x-git-" rpc "-request"))) false
      (not (some #{"upload-pack" "receive-pack"} [rpc])) false
      (= rpc "receive-pack") (@config :receive-pack)
      (= rpc "upload-pack")  (@config :upload-pack)
      :else false)))

(defn create-pack [rpc repository]
  (if (= rpc "upload-pack")
    (UploadPack. repository)
    (ReceivePack. repository)))

(defn service-info-refs [service-name]
  (let [ pack (create-pack service-name (. (workspace/make-repo) getRepository))]
    (. pack setBiDirectionalPipe false)
    (-> (response/response
          (ring-io/piped-input-stream
            (fn [ostream]
              (let [ msg (str "# service=git-" service-name "\n")
                     writer (PrintWriter. ostream)]
                (. writer print (format "%04x" (+ (count msg) 4)))
                (. writer print msg)
                (. writer print pkt-flush)
                (. writer flush)
              (. pack sendAdvertisedRefs
                (RefAdvertiser$PacketLineOutRefAdvertiser. (PacketLineOut. ostream)))))))
      (response/content-type (str "application/x-git-" service-name "-advertisement"))
      (hdr-nocache))))

(defn dumb-info-refs []
  (-> (response/file-response
        (str (.. (workspace/make-repo) getRepository getDirectory getPath) "/info/refs"))
    (response/content-type "text/plain")
    (hdr-nocache)))

(defn get-service-type [request]
  (if-let [service-type (get-in request [:params :service])]
    (when (.startsWith service-type "git-")
      (.substring service-type 4))))

(defn get-info-refs [request]
  (let [service-name (get-service-type request)]
    (if (has-access request service-name)
      (service-info-refs service-name)
      (dumb-info-refs))))

(defn get-text-file [path]
  (let [ file (io/file (.. (workspace/make-repo) getRepository getDirectory getPath) path)]
    (if (.exists file)
      (-> (response/file-response (.getPath file))
        (response/content-type "text/plain")
        (hdr-nocache))
      (response/not-found "Not Found"))))

(defn get-info-packs []
  (-> (response/file-response
        (str (.. (workspace/make-repo) getRepository getDirectory getPath) "/objects/info/packs"))
    (response/content-type "text/plain; charset=utf-8")
    (hdr-nocache)))

(defn get-loose-object [path]
  (-> (response/file-response
        (str (.. (workspace/make-repo) getRepository getDirectory getPath) "/" path))
    (response/content-type "application/x-git-loose-object")
    (hdr-cache-forever)))

(defn get-pack-file [path]
  (-> (response/file-response
        (str (.. (workspace/make-repo) getRepository getDirectory getPath) "/" path))
    (response/content-type "application/x-git-packed-objects")
    (hdr-cache-forever)))

(defn get-idx-file [path]
  (-> (response/file-response
        (str (.. (workspace/make-repo) getRepository getDirectory getPath) "/" path))
    (response/content-type "application/x-git-packed-objects-toc")
    (hdr-cache-forever)))

(defn service-upload [request]
  (let [ pack (create-pack "upload-pack" (. (workspace/make-repo) getRepository))]
    (. pack setBiDirectionalPipe false)
    (-> (response/response (ring-io/piped-input-stream
                             (fn [ostream]
                               (. pack upload (request :body) ostream nil))))
      (response/content-type (str "application/x-git-upload-pack-result")))))

(defn service-receive [request]
  (let [ pack (create-pack "receive-pack" (. (workspace/make-repo) getRepository))]
    (. pack setBiDirectionalPipe false)
    (-> (response/response (ring-io/piped-input-stream
                             (fn [ostream]
                               (. pack receive (request :body) ostream nil))))
      (response/content-type (str "application/x-git-receive-pack-result")))))

(defroutes routes
  (compojure/context (str "/" (@config :repo-name) ".git")  []
    (GET "/info/refs" {:as request}
      (get-info-refs request))
    (POST "/git-upload-pack" {:as request}
      (service-upload request))
    (POST "/git-receive-pack" {:as request}
      (service-receive request))
    (GET "/HEAD" {params :params}
      (get-text-file "HEAD"))
    (GET "/objects/info/alternates" []
      (get-text-file "objects/info/alternates"))
    (GET "/objects/info/http-alternates" []
      (get-text-file "objects/info/http-alternates"))
    (GET "/objects/info/packs" []
      (get-info-packs))
    (GET "/objects/info/*" {params :params}
      (get-text-file (str "objects/info/" (params :*))))
    (GET ["/objects/:hash2/:hash38", :hash2 #"[0-9a-f]{2}" :hash38 #"[0-9a-f]{38}"] [hash2 hash38]
      (get-loose-object (str "objects/" hash2 hash38)))
    (GET ["/objects/pack/:pack-file", :pack-file #"[0-9a-f]\.(pack|idx)"] [pack-file]
      (cond
        (. pack-file endsWith ".pack") (get-pack-file (str "objects/pack/" pack-file))
        (. pack-file endsWith ".idx")  (get-idx-file  (str "objects/pack/" pack-file))))))

  
