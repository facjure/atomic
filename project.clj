(defproject facjure/datomic-tools "0.1.2"
  :description "A thin wrapper on Datomic"
  :min-lein-version "2.3.0"
  :url "https://github.com/facjure/datomic-tools"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :warn-on-reflection false
  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [environ "1.0.0"]
                 [com.datomic/datomic-free "0.9.5153" :exclusions [joda-time]]
                 ;[com.datomic/datomic-pro "0.9.5153" :exclusions [joda-time]]
                 [clj-time "0.9.0"]
                 [slingshot "0.12.2"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-marginalia "0.7.1"]]
  :profiles {:dev {:dependencies [[lein-cljfmt "0.1.7"]]}})
