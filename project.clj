(defproject facjure/datomic-tools "0.1.1"
  :description "A thin wrapper on Datomic"
  :min-lein-version "2.3.0"
  :url "https://github.com/facjure/datomic-tools"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [com.datomic/datomic-free "0.9.5067" :exclusions [joda-time]]
                 ;[com.datomic/datomic-pro "0.9.5067" :exclusions [joda-time]]
                 [clj-time "0.8.0"]
                 [slingshot "0.12.1"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-marginalia "0.7.1"]
            [lein-midje "3.0.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
