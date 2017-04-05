# darzana

Darzana is a DSL for backends-for-frontends (BFF).

- Call APIs Asynchronously
- Define APIs from Swagger specifications
- Render a HTML template

## Commands specification

### defroute

Define routing.

```clj
(defroute "/pet" :get ... )
```

### call-api

Call multiple APIs in parallel.

```clj
(call-api [{:id :petstore :path "/pet/{petId}" :method :get}
           {:id :petstore :path "/user/{userId}" :method :get}])
```

### render

Render a HTML template.

```clj
(render {:template "pet/show"})
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
