(ns atomic.history
  (:require [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.db :refer [temp-eid]]
            [atomic.utils :refer :all])
  (:import datomic.Util))

(defn transactions
  ([conn attr]
   (->> (d/q '[:find ?tx
               :in $ ?a
               :where [_ ?a ?v ?tx _]
               [?tx :db/txInstant ?tx-time]]
             (d/history (d/db conn))
             attr)
        (map #(first %))
        (map d/tx->t)
        (sort)))
  ([conn attr val]
   (->> (d/q '[:find ?tx
               :in $ ?a ?v
               :where [_ ?a ?v ?tx _]
               [?tx :db/txInstant ?tx-time]]
             (d/history (d/db conn))
             attr val)
        (map #(first %))
        (map d/tx->t)
        (sort))))

;; Modified from: http://dbs-are-fn.com/2013/datomic_history_of_an_entity/
(defn changes [conn entity-id]
  "Show the before/after change history (with timestamp) for an entity-id
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
