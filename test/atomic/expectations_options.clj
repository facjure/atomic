(ns atomic.expectations-options
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]))

#_(defn load-test-schema
  "loads test data"
  {:expectations-options :before-run}
  []
  (schema/load-edn conn "schema/blog.edn")
  (println "Loaded Test Schema"))

#_(defn in-context
  "rebind a var, expecations are run in the defined context"
  {:expectations-options :in-context}
  [conn]
  (with-redefs [some-src/a-fn-to-be-rebound (constantly :a-rebound-val)]
    (conn)))
