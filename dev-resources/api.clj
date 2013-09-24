(ns app
  (:use [darzana.api]))

(darzana.core/set-application-scope
  {:key "fd2c0d29b6a76bb1"})

(defapi gourmet
  (url "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/")
  (query-keys :key :name :middle_area)
  (expire 300))

(defapi large-area
  (url "http://webservice.recruit.co.jp/hotpepper/large_area/v1/")
  (query-keys :key))

(defapi middle-area
  (url "http://webservice.recruit.co.jp/hotpepper/middle_area/v1/")
  (query-keys :key :large_area :middle_area))

(defapi groups
  (url "http://localhost:8082/groups")
  (query-keys :id :name))

(defapi group
  (url "http://localhost:8082/groups/:id")
  (query-keys :id))

(defapi group-post
  (url "http://localhost:8082/groups")
  (method :post)
  (query-keys :id :name))

(defapi group-put
  (url "http://localhost:8082/groups/:id")
  (method :put)
  (content-type "application/json")
  (query-keys :id :name))

(defapi group-delete
  (url "http://localhost:8082/groups/:id")
  (method :delete))
