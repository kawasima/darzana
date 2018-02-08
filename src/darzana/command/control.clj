(ns darzana.command.control
  (:require [darzana.runtime :as runtime]
            [darzana.context :as context]
            [clojure.string :as string]
            [ring.util.response :as response]))

(defmacro defroute [url method & exprs]
  `{~url {~method (fn [runtime# request#]
                    (-> (runtime/create-context runtime# request#)
                        ~@exprs))}})

(defmacro if-success [context success error]
  `(let [ctx# ~context]
     (if (empty? (get-in ctx# [:scope :error]))
       (-> ctx# ~success)
       (-> ctx# ~error))))

(defmacro if-contains
  ([context key contains]
   `(if-contains ~context ~key ~contains do))
  ([context key contains not-contains]
   `(let [ctx# ~context]
      (if (context/find-in-scopes ctx# ~key)
        (-> ctx# ~contains)
        (-> ctx# ~not-contains)))))

(defn- replace-url-variables [url context]
  (string/replace url #"\{([A-Za-z_]\w*)\}"
                  #(when-let [ks (context/find-in-scopes context (-> % second keyword))]
                     (get-in context (into [:scope] ks)))))

(defn redirect
  ([context url]
   (response/redirect (replace-url-variables url context)))
  ([context url status]
   (response/redirect url status)))
