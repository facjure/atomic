(ns datomictools.fact
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomictools.peer :refer [conn]]
            [datomictools.utils :refer :all])
  (:import datomic.Util))


(defn add [attr-data]
  "Add a fact from attribute data"
  (let [id {:db/id (d/tempid :db.part/user)}
        tx-data (conj id attr-data)]
    (d/transact @conn (vector tx-data))))

(defn retract [attr-data]
  "Add a fact from attribute data"
  (let [op {:db/retract (d/tempid :db.part/user)}
        tx-data (conj op attr-data)]
    (d/transact @conn (vector tx-data))))


