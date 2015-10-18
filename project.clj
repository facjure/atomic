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
                 [org.clojure/core.typed "0.3.11"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.datomic/datomic-free "0.9.5327" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.2.3"]
                 [clj-time "0.9.0"]
                 [environ "1.0.1"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-marginalia "0.8.0"]
            [lein-expectations "0.0.7"]
            [lein-autoexpect "1.4.2"]]
  :profiles {:dev {:source-paths ["src" "examples"]
                   :dependencies [[expectations "2.0.9"]]}})
