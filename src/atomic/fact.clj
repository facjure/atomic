(ns atomic.fact
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.database :refer [temp-eid]]
            [atomic.query :as query]
            [atomic.utils :refer :all])
  (:import datomic.Util))

(defn add
  "Add a fact as a set of attr/val pairs"
  ([conn attr-data]
   "Add a fact from attribute data"
   (let [id {:db/id (temp-eid)}
         tx-data (conj id attr-data)]
     (d/transact conn (vector tx-data))))
  ([conn id attr-data]
   (let [tx-data (conj id attr-data)]
     (d/transact conn (vector tx-data)))))

(defn retract [conn id attr-data]
  "Retract a fact from attribute data"
  (let [op :db/retract
        tx-data (concat (conj [] op id) attr-data)]
    (d/transact conn (vector tx-data))))

(defn- install
  "Install txdata and return the single new entity possessing attr"
  [conn txdata attr]
  (let [t  (d/basis-t (:db-after @(d/transact conn txdata)))
        snapshot (d/db conn)]
    (query/find-entity '[:find ?e
                         :in $ ?attr ?t
                         :where [?e ?attr _ ?t]]
                       snapshot
                       (d/entid snapshot attr) (d/t->tx t))))

(defn delete-tx
  "Returns transactable data for delete."
  [ids]
  (map #(vec [:db.fn/retractEntity (Long. %)]) ids))
