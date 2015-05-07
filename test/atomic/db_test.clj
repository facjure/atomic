(ns atomic.db-test
  (:use expectations)
  (:require [atomic.db :as db]))

(expect (more-> datomic.peer.LocalConnection type)
  (db/create-and-connect "datomic:mem://test"))

(expect (more-> datomic.peer.LocalConnection type)
  (db/setup "test"))

(expect (more-of res
                 datomic.db.DbId (type res)
                 [:part :db.part/user] (first res))
  (db/temp-eid))
