(ns atomic.db-test
  (:use expectations)
  (:require [atomic.db :as db]))

(expect (more-> datomic.peer.LocalConnection type)
  (db/resolve-uri :mem "test"))

(expect (more-> datomic.peer.LocalConnection type)
  (db/create! "test"))

(expect (more-> datomic.peer.LocalConnection type)
  (db/create-anonymous! "test"))

(expect (more-of res
                 datomic.db.DbId (type res)
                 [:part :db.part/user] (first res))
  (db/temp-eid))
