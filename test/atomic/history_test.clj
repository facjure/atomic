(ns atomic.history-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.history :as history]))

(def conn (db/create-anonymous!))
(schema/load-edn conn "schema/blog.edn")

;; TODO validate results
(expect
  (more Object type)
  (history/transactions conn :user/username))
