(defproject net.unit8/darzana "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/kawasima/darzana/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.5.1"]
                  [org.clojure/tools.nrepl "0.2.2"]
                  [com.github.jknack/handlebars "1.1.2"]
		  [compojure "1.1.5"]
                  [http-kit "2.1.11"]
                  [com.taoensso/carmine "2.2.0"] ;;redis
                  [me.raynes/fs "1.4.4"]
                  [clj-oauth "1.4.1"] ;; for Oauth 1.0a support
                  [clj-jgit "0.5.0"] ;; git
                  [net.sf.json-lib/json-lib "2.4" :classifier "jdk15"] ;; XML -> JSON
                  [xom/xom "1.2.5"]
                  [org.clojure/data.xml "0.0.7"]
                  [ring.middleware.logger "0.4.0"]
                  [org.slf4j/slf4j-log4j12 "1.7.5"]
                  [com.taoensso/tower "2.0.0-beta5"]
                  ;; for clojurescript
                  [com.cemerick/clojurescript.test "0.0.4"]
                  [net.unit8/tower-cljs "0.1.0"]
                  [org.clojure/clojurescript "0.0-1909"]
                  [jayq "2.4.0"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Xverify:none"]
  :plugins [ [lein-ring "0.8.2"]
             [lein-cljsbuild "0.3.3"]]
  :source-paths ["src/clj"]
  :test-paths   ["test/clj"]
  :resource-paths ["resources"]

  :cljsbuild
  {
    :repl-listen-port 9000
    :repl-launch-commands
    { "phantom" [ "phantomjs"
                  "phantom/repl.js"
                  :stdout ".repl-phantom-out"
                  :stderr ".repl-phantom-err"]
      "phantom-naked" [ "phantomjs"
                        "runners/repl.js"
                        "resources/private/html/naked.html"
                        :stdout ".repl-phantom-out"
                        :stderr ".repl-phantom-err"]}
    :builds
    { :prod
      { :source-paths ["src/cljs"]
        :jar true
        :compiler
        { :output-to "resources/darzana/admin/public/js/main.min.js"
          :optimizations :advanced
          :externs [ "externs/darzana-externs.js"
                     "externs/jquery-1.9.js"
                     "externs/codemirror-externs.js"
                     "externs/handlebars-externs.js"
                     "externs/underscore-externs.js"
                     "externs/backbone-1.0.0-externs.js"]
          :libs ["lib/blockly"]
          :pretty-print false
          }}
      :dev
      { :source-paths ["src/cljs"]
        :jar true
        :compiler
        { :output-to "resources/darzana/admin/public/js/main.js"
          :optimizations :simple
          :libs ["lib/blockly"]
          :pretty-print true}}
      :test
      { :source-paths ["src/cljs" "test/cljs"]
        :compiler
        { :output-to "target/cljs/testable.js"
          :optimizations :simple
          :pretty-print false}}}
    :test-commands
    { "unit" ["runners/phantomjs.js" "target/cljs/testable.js"] }}
  :ring {:handler darzana.core/admin-app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})

