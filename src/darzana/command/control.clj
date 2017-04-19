(ns darzana.command.control
  (:require [darzana.runtime :as runtime]
            [darzana.context :as context]))

(defmacro defroute [url method & exprs]
  `{~url {~method (fn [runtime# request#]
                    (-> (runtime/create-context runtime# request#)
                        ~@exprs))}})

(defmacro if-success [context success error]
  `(if (empty? (~context :error))
     (-> ~context ~success)
     (-> ~context ~error)))

(defmacro if-contains
  ([context key contains]
   `(if-contains ~context ~key ~contains do))
  ([context key contains not-contains]
   `(if (context/find-in-scopes ~context ~key)
        (-> ~context ~contains)
        (-> ~context ~not-contains))))
