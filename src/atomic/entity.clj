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

;; Modified from: http://dbs-are-fn.com/2013/datomic_history_of_an_entity/
(defn history [conn entity-id]
  "Retrieve the before/after change history (with timestamp) for an entity-id
   1. Grab the history
   2. Transform into {:attr {:old "" :new ""}}
   3. A single tx can contain multiple attr changes; grab the actual changes
   4. Sort with oldest first"
   (let [snapshot (d/db conn)
         history (d/q
                  '[:find ?tx ?a
                    :in $ ?e
                    :where
                    [?e ?a _ ?tx]]
                  (d/history snapshot)
                  entity-id)
         transform-fn (fn [[tx attr]]
                        (let [tx-before-db (d/as-of snapshot (dec (d/tx->t tx)))
                              tx-after-db (d/as-of snapshot tx)
                              tx-e (d/entity tx-after-db tx)
                              attr-e-before (d/entity tx-before-db attr)
                              attr-e-after (d/entity tx-after-db attr)]
                          [(:db/ident attr-e-after)
                           {:old (get
                                  (d/entity tx-before-db entity-id)
                                  (:db/ident attr-e-before))
                            :new (get
                                  (d/entity tx-after-db entity-id)
                                  (:db/ident attr-e-after))}]))
         query (->>
                history
                (group-by (fn [[tx attr]] tx))
                (vals)
                (sort-by (fn [[tx attr]] tx))
                (map
                 (fn [changes]
                   {:changes (into {}
                                   (map
                                    transform-fn
                                    changes))
                    :timestamp (->> (ffirst changes)
                                    (d/entity (d/as-of snapshot (ffirst changes)))
                                    :db/txInstant)})))]
     query))
