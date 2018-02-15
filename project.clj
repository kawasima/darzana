(defproject net.unit8.darzana/darzana "1.0.0-SNAPSHOT"
  :description "A Backends for Frontends Tool"
  :url "http://github.com/kawasima/darzana"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/java.data "0.1.1"]
                 [cheshire "5.8.0"] ;; json
                 [bidi "2.1.3"]
                 [duct/core "0.6.2"]
                 [duct/module.logging "0.3.1"]
                 [duct/logger "0.2.1"]
                 [duct/server.http.jetty "0.2.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-devel "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-webjars "0.2.0"]

                 [javax.cache/cache-api "1.0.0"]

                 [io.swagger/swagger-parser "2.0.0-rc1"]
                 [com.github.jknack/handlebars "4.0.6"]
                 [org.freemarker/freemarker "2.3.27-incubating"]
                 [com.squareup.okhttp3/okhttp "3.9.1"]
                 [org.hibernate.validator/hibernate-validator "6.0.7.Final"]
                 [org.glassfish/javax.el "3.0.1-b08"]
                 [org.slf4j/slf4j-simple "1.7.25"]
                 [integrant/repl "0.3.0"]]
  :plugins [[duct/lein-duct "0.10.6"]]
  :main ^:skip-aot darzana.main
  :target-path "target/%s/"
  :resource-paths ["resources"]
  :prep-tasks ["javac" "compile"]

  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :repl {:prep-tasks     ^:replace [["javac"] ["compile"]]
          :resource-paths ^:replace ["resources" "dev/resources"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev  {}
   :project/dev   {:dependencies [[integrant/repl "0.3.0"]
                                  [org.jsr107.ri/cache-ri-impl "1.0.0"]
                                  [eftest "0.4.3"]
                                  [org.clojure/test.check "0.9.0"]
                                  [kerodon "0.9.0"]]
                   :source-paths   ["dev/src"]
                   :java-source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :env {:port "3000"}}})
