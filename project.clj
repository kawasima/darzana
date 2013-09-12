(defproject net.unit8/darzana "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.5.1"]
                  [org.clojure/tools.nrepl "0.2.2"]
                  [com.github.jknack/handlebars "1.1.3-SNAPSHOT"]
		  [compojure "1.1.5"]
                  [http-kit "2.1.10"]
                  [com.taoensso/carmine "2.2.0"] ;;redis
                  [net.sf.json-lib/json-lib "2.4" :classifier "jdk15"] ;; XML -> JSON
                  [xom/xom "1.2.5"]
                  [org.clojure/data.xml "0.0.7"]
                  [com.cemerick/clojurescript.test "0.0.4"]
                  [ring.middleware.logger "0.4.0"]
                  [org.slf4j/slf4j-log4j12 "1.7.5"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Xverify:none"]
  :plugins [ [lein-ring "0.8.2"]
             [lein-ritz "0.7.0"]
             [lein-cljsbuild "0.3.2"]]
  :source-paths ["src/clj"]
  :test-paths   ["test/clj"]
  :resource-paths ["lib/*", "resources"]

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
    { :dev
      { :source-paths ["src/cljs"]
        :jar true
        :compiler
        { :output-to "resources/public/js/main-debug.js"
          :optimizations :whitespace
          :pretty-print true}}
      :test
      { :source-paths ["src/cljs" "test/cljs"]
        :compiler
        { :output-to "target/cljs/testable.js"
          :optimizations :advanced
          :pretty-print false}}}
      :test-commands
    { "unit" ["runners/phantomjs.js" "target/cljs/testable.js"] }}
  :ring {:handler darzana.core/admin-app }
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})

