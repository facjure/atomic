(ns atomic.query-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.query :as query]))

(def conn (db/create-anonymous!))
(schema/load-edn conn "schema/blog.edn")

;; TODO validate results
(expect
  (more Object type)
  (query/find-all-by :username/name))

;; TODO validate results
(expect
  (more Object type)
  (query/get-attributes conn 17592186045425))
