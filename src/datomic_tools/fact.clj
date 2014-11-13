(ns datomic-tools.fact
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomic-tools.db :refer [conn]]
            [datomic-tools.query :as query]
            [datomic-tools.utils :refer :all])
  (:import datomic.Util))


(defn temp-id []
  (d/tempid :db.part/user))


(defn install
  "Install txdata and return the single new entity possessing attr"
  [txdata attr]
  (let [t  (d/basis-t (:db-after @(d/transact @conn txdata)))
        db (d/db @conn)]
    (query/find-entity '[:find ?e
                         :in $ ?attr ?t
                         :where [?e ?attr _ ?t]]
                       db (d/entid db attr) (d/t->tx t))))

(defn add
  "Add a fact as a set of attr/val pairs"
  ([attr-data]
   "Add a fact from attribute data"
   (let [id {:db/id (temp-id)}
         tx-data (conj id attr-data)]
     (d/transact @conn (vector tx-data))))
  ([id attr-data]
    (let [tx-data (conj id attr-data)]
      (d/transact @conn (vector tx-data)))))

(defn retract [id attr-data]
  "Add a fact from attribute data"
  (let [op :db/retract
        tx-data (conj op id attr-data)]
    (d/transact @conn (vector tx-data))))

(defn delete-tx
  "Returns transactable data for delete."
  [ids]
  (map #(vec [:db.fn/retractEntity (Long. %)]) ids))
