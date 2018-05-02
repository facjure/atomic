(defproject facjure/atomic "0.4.0"
  :description "A tiny database for writers, built on Datomic"
  :url "https://github.com/facjure/atomic"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git" :url "https://github.com/facjure/atomic"}
  :min-lein-version "2.8.1"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.logging "0.4.0"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [com.stuartsierra/component "0.3.2"]
                 [clj-time "0.14.3"]
                 [environ "1.1.0"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:dev {:source-paths ["src" "examples"]}})
