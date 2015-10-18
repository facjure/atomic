(ns atomic.entity
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :refer (infof)]
            [clojure.walk :as walk]
            [datomic.api :refer [q] :as d])
  (:import java.util.UUID))

(defn entity+
  [db eid]
  (cond
    (integer? eid) (d/entity db eid)
    (:db/id eid) (d/entity db (:db/id eid))))

(defn touch+
  "By default, touch returns a map that can't be assoc'd. Fix it"
  [entity]
  ;; (into {}) makes the map assoc'able, but lacks a :db/id, which is annoying for later lookups.
  (into (select-keys entity [:db/id]) (d/touch entity)))

(defn touch-all
  "Runs the query that returns [[eid][eid]] and returns all entity maps.
   Uses the first DB to look up all entities"
  [query & query-args]
  (let [the-db (first query-args)]
    (for [[eid & _] (apply q query query-args)]
      (touch+ (d/entity the-db eid)))))

(defn touch-one
  "Runs a query that returns [[eid][eid]], and returns the first entity, touched"
  [query & query-args]
  (first (apply touch-all query query-args)))

(defn uuid []
  (UUID/randomUUID))

;; TODO: support multiple parts
(defn generate-eids [conn tempid-count]
  (let [tempids (take tempid-count (repeatedly #(d/tempid :db.part/user)))
        transaction (d/transact conn (mapv (fn [tempid] {:db/id tempid :dummy :dummy/dummy}) tempids))]
    (mapv (fn [tempid] (d/resolve-tempid (:db-after @transaction) (:tempids @transaction) tempid)) tempids)))
