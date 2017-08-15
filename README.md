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
```

## Developing

### Setup

When you first clone this repository, run:

```sh
lein setup
```

This will create files for local configuration, and prep your system
for the project.

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to initiate and start the system.

```clojure
dev=> (go)
:started
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server. Changes to CSS or ClojureScript
files will be hot-loaded into the browser.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

If you want to access a ClojureScript REPL, make sure that the site is loaded
in a browser and run:

```clojure
dev=> (cljs-repl)
Waiting for browser connection... Connected.
To quit, type: :cljs/quit
nil
cljs.user=>
```

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
dev=> (test)
...
```

But you can also run tests through Leiningen.

```sh
lein test
```


## Legal

Copyright © 2017 kawasima
