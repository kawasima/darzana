(defproject darzana "1.0.0-SNAPSHOT"
  :description "Build BFF like Scratch"
  :url "http://github.com/kawasima/darzana"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/java.data "0.1.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.stuartsierra/component "0.3.2"]
                 [bidi "2.0.16"]
                 [duct "0.8.2"]
                 [environ "1.1.0"]
                 [liberator "0.14.1"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring-jetty-component "0.3.1"]
                 [ring-webjars "0.1.1"]

                 [javax.cache/cache-api "1.0.0"]
                 [clj-jgit "0.8.9"]

                 [io.swagger/swagger-parser "1.0.24"]
                 [com.github.jknack/handlebars "4.0.6"]
                 [com.squareup.okhttp3/okhttp "3.6.0"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [org.webjars/normalize.css "3.0.2"]
                 [org.webjars/blockly "36eb0787cc5"]]
  :plugins [[lein-environ "1.0.3"]
            [lein-cljsbuild "1.1.2"]]
  :main ^:skip-aot darzana.main
  :target-path "target/%s/"
  :resource-paths ["resources"]
  :prep-tasks [["javac"] ["compile"]]

  :aliases {"setup"  ["run" "-m" "duct.util.repl/setup"]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :repl {:resource-paths ^:replace ["resources" "dev/resources"]
          :prep-tasks     ^:replace [["javac"] ["compile"]]}
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:dependencies [[duct/generate "0.8.2"]
                                  [reloaded.repl "0.2.3"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [org.jsr107.ri/cache-ri-impl "1.0.0"]
                                  [eftest "0.1.1"]
                                  [com.gearswithingears/shrubbery "0.4.1"]
                                  [kerodon "0.8.0"]
                                  [binaryage/devtools "0.8.2"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths   ["dev/src"]
                   :java-source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :repl-options {:init-ns user}
                   :env {:port "3000"}}
   :project/test  {}})
