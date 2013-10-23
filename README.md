darzana
=======

Darzana is a mashup framework written by clojure.

## Architecture

Darzana has the features as following:

* Routing
* Call API
  * Parallel Call
  * Cache API responses
  * Auto formatting correspond to content type (XML/JSON/Form url encoding).
* Render templates using by Handlebars.java

## Get started

Write api definition.

You can use the `defapi` macro.

    (defapi gourmet
      (url "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/")
      (query-keys :key :name :middle_area)
      (expire 300))

Write routing definition.

    (defmarga GET "/groups" 
      (call-api [ app/groups ])
      (render "groups/list"))

