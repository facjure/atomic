(defproject facjure/atomic "0.2.3"
  :description "A thin wrapper on Datomic"
  :url "https://github.com/facjure/atomic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "https://github.com/facjure/atomic"}
  :min-lein-version "2.5.0"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [environ "1.0.0"]
                 [com.datomic/datomic-free "0.9.5206" :scope  "provided"
                  :exclusions [joda-time
                               org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12]]
                 [ch.qos.logback/logback-classic "1.0.1"]
                 [clj-time "0.9.0"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-marginalia "0.8.0"]
            [lein-expectations "0.0.7"]
            [lein-autoexpect "1.4.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.10"]
                                  [expectations "2.0.9"]]}})
