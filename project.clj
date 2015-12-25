(defproject facjure/atomic "0.3.0"
  :description "A thin wrapper on Datomic"
  :url "https://github.com/facjure/atomic"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git" :url "https://github.com/facjure/atomic"}
  :min-lein-version "2.5.0"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.8.0-RC4"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/core.typed "0.3.18"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.datomic/datomic-free "0.9.5344"]
                 [com.stuartsierra/component "0.3.1"]
                 [clj-time "0.11.0"]
                 [environ "1.0.1"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-marginalia "0.8.0"]
            [lein-expectations "0.0.8"]]
  :profiles {:dev {:source-paths ["src" "examples"]
                   :dependencies [[expectations "2.1.1"]]}})
