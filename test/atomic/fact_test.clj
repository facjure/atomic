(ns atomic.fact-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.fact :as fact]))

(def conn (db/create-anonymous))

#_(expect (more-> :success :status)
  (fact/add conn {:author/name "Stu G"
                  :author/email "stu@somemail.com"}))

#_(fact/add conn {:author/name "Stu G"
                :author/email "stu@somemail.com"})
