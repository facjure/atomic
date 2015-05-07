(ns atomic.schema-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.fact :as fact]))

(def conn (db/create-anonymous))

(expect nil? (schema/load-edn conn "schema/blog.edn"))
