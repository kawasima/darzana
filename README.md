# darzana

Darzana is a DSL for backends-for-frontends (BFF). Darzana has some features as follows:

- Call APIs Asynchronously
- Define APIs by describing the Swagger specifications
- Validate form parameters by BeanValidation
- Render a HTML template

## Examples

```clj
(control/defroute "category/create" :post
  (mapper/read-value {:var "category"} {:type io.swagger.model.Category :var :category})
  (control/if-success
   (-> (api/call-api {:id :petstore :path "/category" :method :post})
       (control/redirect "/category"))
   (renderer/render {:template "/petstore/category/new"})))
```

## Commands specification

Darzana has some commands for describing specification of endpoints.

### defroute

Define routing.

```clj
(control/defroute "/pet" :get ... )
```

### call-api

Call multiple APIs in parallel.

```clj
(api/call-api [{:id :petstore :path "/pet/{petId}" :method :get}
               {:id :petstore :path "/user/{userId}" :method :get}])
```

### read-value

`read-value` is a command that converts to an another darzana scope variable.

```clj
(mapper/read-value {:var "pet"} {:scope :page, :var :pet, :type io.swagger.model.Pet})
```

If `type` parameter exists, validate automatically.

Add an authenticated user to session.

```clj
(mapper/read-value {:var :login-user} {:scope :session})
```

### render

Render a HTML template.

```clj
(renderer/render {:template "pet/show"})
```

### if-success

This is a command for conditional processing. If the error scope is empty,

```clj
(control/if-success
 (renderer/render {:template "pet/show"})
 (renderer/render {:template "error"}))
```

## Developing

### Setup

Create a `deps.edn`.

```
{:deps
 {net.unit8.darzana/darzana {:mvn/version "1.0.0-SNAPSHOT"}}}
```

`application.edn`

```
{:duct.core/environment :development

 :duct.core/include ["darzana/config"]
 :darzana.api-spec/swagger {:swagger-path "swagger"}
 :darzana.template/freemarker {:template-path "ftl"}
 :darzana.http-client/okhttp {}
 :darzana/runtime {:routes-path "scripts"}}
```

Run server using by `clj` command.

```sh
% clj -m darzana.main application.edn
```

## Legal

Copyright © 2017-2018 kawasima
Distributed under the Eclipse Public License, the same as Clojure.
