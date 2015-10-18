(defproject facjure/atomic "0.3.0"
  :description "A thin wrapper on Datomic"
  :url "https://github.com/facjure/atomic"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git" :url "https://github.com/facjure/atomic"}
  :min-lein-version "2.5.0"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.8.0-beta1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.typed "0.3.11"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.datomic/datomic-free "0.9.5327" :exclusions [joda-time]]
                 [com.stuartsierra/component "0.3.0"]
                 [clj-time "0.11.0"]
                 [environ "1.0.1"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-marginalia "0.8.0"]
            [lein-expectations "0.0.8"]
            [lein-autoexpect "1.7.0"]]
  :profiles {:dev {:source-paths ["src" "examples"]
                   :dependencies [[expectations "2.1.1"]]}})
