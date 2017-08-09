(ns darzana.command.control
  (:require [darzana.runtime :as runtime]
            [darzana.context :as context]
            [ring.util.response :as response]))

(defmacro defroute [url method & exprs]
  `{~url {~method (fn [runtime# request#]
                    (-> (runtime/create-context runtime# request#)
                        ~@exprs))}})

(defmacro if-success [context success error]
  `(if (empty? (get-in ~context [:scope :error]))
     (-> ~context ~success)
     (-> ~context ~error)))

(defmacro if-contains
  ([context key contains]
   `(if-contains ~context ~key ~contains do))
  ([context key contains not-contains]
   `(if (context/find-in-scopes ~context ~key)
        (-> ~context ~contains)
        (-> ~context ~not-contains))))

(defn redirect
  ([context url]
   (response/redirect url))
  ([context url status]
   (response/redirect url status)))
