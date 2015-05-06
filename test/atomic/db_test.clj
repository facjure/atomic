(ns atomic.db-test
  (:use expectations)
  (:require [atomic.db :refer :all]))

(expect (more-> datomic.peer.LocalConnection type)
        (create "datomic:mem://test"))

(expect (more-> datomic.peer.LocalConnection type)
        (connect "datomic:mem://test"))

(expect (more-> datomic.peer.LocalConnection type)
        (setup "test"))

(expect true (delete "test"))

