(defproject darzana "1.0.0-SNAPSHOT"
  :description "Build BFF like Scratch"
  :url "http://github.com/kawasima/darzana"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/java.data "0.1.1"]
                 [cheshire "5.7.1"] ;; json
                 [bidi "2.1.2"]
                 [duct/core "0.5.2"]
                 [duct/module.logging "0.2.0"]
                 [duct/server.http.jetty "0.1.5"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-devel "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-webjars "0.2.0"]

                 [javax.cache/cache-api "1.0.0"]

                 [io.swagger/swagger-parser "1.0.32"]
                 [com.github.jknack/handlebars "4.0.6"]
                 [com.squareup.okhttp3/okhttp "3.8.1"]
                 [org.hibernate.validator/hibernate-validator "6.0.1.Final"]
                 [org.glassfish/javax.el "3.0.1-b08"]
                 [org.slf4j/slf4j-nop "1.7.25"]]
  :plugins [[lein-environ "1.0.3"]]
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
                                  [eftest "0.3.1"]
                                  [org.clojure/test.check "0.9.0"]
                                  [kerodon "0.8.0"]]
                   :source-paths   ["dev/src"]
                   :java-source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :repl-options {:init-ns user}
                   :env {:port "3000"}}
   :project/test  {}})
