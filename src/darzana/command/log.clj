(ns darzana.command.log
  (:require [duct.logger :as logger]
            [darzana.runtime :as runtime]
            [darzana.context :as context]
            [clojure.string :as string]
            [ring.util.response :as response]))

(defn log [context level event data]
  (when-let [logger (get-in context [:runtime :logger])]
    (logger/log logger level event data))
  context)

(defn scopes [context]
  (when-let [logger (get-in context [:runtime :logger])]
    (logger/log logger :report ::scopes (get-in context [:scope])))
  context)
