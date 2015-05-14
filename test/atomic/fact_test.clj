(ns atomic.fact-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.fact :as fact]))

(def conn (db/create-anonymous!))
(schema/load-edn conn "schema/blog.edn")

;; TODO validate results
(expect
  (more Object type)
  (fact/add conn {:user/username "Stu"
                  :article/title "stu@somemail.com"}))

;; TODO validate results
(expect-let [res (fact/add conn {:article/author "Foo"})]
  (more Object type)
  (fact/retract conn (:id res) {:article/author "Foo"}))
