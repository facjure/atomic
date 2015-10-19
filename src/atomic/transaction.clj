(ns atomic.transaction
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :refer (infof)]
            [clojure.walk :as walk]
            [datomic.api :refer [q] :as d]
            [atomic.query :as query])
  (:import java.util.UUID))

(defn datom->transaction
  "Return the transaction for the current datom"
  [datom]
  (let [{:keys [a e v tx added]} datom]
    [(if added :db/add :db/retract) e a v]))

(defn revert-transaction [conn {:keys [tx-data db-after] :as transaction-report}]
  (d/transact conn (map (fn [{:keys [a e v tx added] :as datom}]
                          [(if added :db/retract :db/add) e a v])
                        (remove #(->> % :a (d/entity db-after) :db/ident (= :db/txInstant))
                                tx-data))))

(defn rollback
    "Reassert retracted datoms and retract asserted datoms in a transaction,
  effectively \"undoing\" the transaction."
    [conn tx]
    (let [tx-log (-> conn d/log (d/tx-range tx nil) first) ; find the transaction
          txid   (-> tx-log :t d/t->tx) ; get the transaction entity id
          newdata (->> (:data tx-log)   ; get the datoms from the transaction
                       (remove #(= (:e %) txid)) ; remove transaction-metadata datoms
                                        ; invert the datoms add/retract state.
                       (map #(do [(if (:added %) :db/retract :db/add) (:e %) (:a %) (:v %)]))
                       reverse)] ; reverse order of inverted datoms.
      @ (d/transact conn newdata)))  ; commit new datoms.

(defn unique-conflict? [ex]
  (loop [ex ex]
    (if (:db/error (ex-data ex))
      (= (:db/error (ex-data ex)) :db.error/unique-conflict)
      (if (.getCause ex)
        (recur (.getCause ex))
        false))))

(defonce tx-report-ch (async/chan (async/sliding-buffer 1024)))

(defn setup-tx-report-ch [conn]
  (let [queue (d/tx-report-queue conn)]
    (future (while true
              (let [transaction (.take queue)]
                (async/put! tx-report-ch transaction))))))

(defn repl-refs [db refs m]
  (walk/postwalk   (fn [arg]
     (if (and (coll? arg) (refs (first arg)))
       (update-in arg [1] (comp :db/ident (partial d/entity db) :db/id))
       arg))
   m))

(defn ds-temp-id? [db-id]
  (and (integer? db-id)
       (neg? db-id)))

(defn ref-attr? [db attr-name]
  (let [attr-value-type (:db/valueType (query/query-by db :db/ident attr-name))]
    (= :db.type/ref attr-value-type)))

(defn find-tx-instants [conn]
  (reverse (sort (d/q '[:find ?when
                        :where [_ :db/txInstant ?when]]
                      (d/db conn)))))

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

