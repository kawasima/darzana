(defproject darzana "1.0.0-SNAPSHOT"
  :description "Build BFF like Scratch"
  :url "http://github.com/kawasima/darzana"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/java.data "0.1.1"]
                 [bidi "2.0.16"]
                 [duct/core "0.1.1"]
                 [duct/module.logging "0.1.1"]
                 [duct/server.http.jetty "0.1.2"]
                 [ring/ring-core "1.6.0-RC3"]
                 [ring/ring-devel "1.6.0-RC3"]
                 [ring/ring-defaults "0.3.0-beta1"]
                 [ring-webjars "0.2.0-beta1"]

                 [liberator "0.14.1"]
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
   :project/dev   {:dependencies [[integrant/repl "0.2.0"]
                                  [org.jsr107.ri/cache-ri-impl "1.0.0"]
                                  [eftest "0.3.0"]
                                  [kerodon "0.8.0"]]
                   :source-paths   ["dev/src"]
                   :java-source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :repl-options {:init-ns user}
                   :env {:port "3000"}}
   :project/test  {}})
