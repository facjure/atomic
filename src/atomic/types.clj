(ns atomic.types
  (:use [clojure.core.typed])
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d])
  (:import [clojure.lang.Keyword]
           [clojure.lang.Symbol]
           [java.util Date]))

;; Type aliases

; Marker protocol to distinguish historical databases
(ann-protocol HistoryDB)
(defprotocol> HistoryDB)

(def-alias HDb "Historical Db, not supporting with/entity calls." HistoryDB)
(def-alias Db "Full Db, supporting all API." (I datomic.Database HDb))

; Marker protocol for the TempIDs map
(ann-protocol TempIDs)
(defprotocol> TempIDs)

; All longs, but better type documentation with aliases.
; Could differentiate in future with newtype-esque support.
(def-alias TxID "Transaction ID" Long)
(def-alias TxN "Transaction Number" Long)
(def-alias EntityID "ID for an entity in the database." Long)

(def-alias Log datomic.Log)
(def-alias Connection datomic.Connection)

(def-alias Index
  "Index Keywords"
  (U (Value :eavt)
     (Value :aevt)
     (Value :avet)
     (Value :vaet)))

(ann-record ^:no-check datomic.db.Datum [e :- EntityID
                                         a :- EntityID
                                         v :- Any
                                         t :- TxID
                                         added :- Boolean])
(def-alias Datom datomic.db.Datum)

(def-alias TDataMap (Map Any Any))
(def-alias TDataVec (Vec Any))
(def-alias SchemaEntry (U TDataMap TDataVec))
(def-alias Schema (Vec SchemaEntry))

(def-alias Transaction '{:t TxN
                         :data (Seqable Datom)})
(def-alias TransactionData '{:db-before Db
                             :db-after Db
                             :tx-data (Seqable Datom)
                             :tempids TempIDs})

; Helper for derefables
(def-alias Futur (TFn [[x :variance :covariant]] (clojure.lang.IDeref x)))

(def-alias Query
  "A datomic query.
  If a string, should read-string to the the vector or map form."
  (U (Vec Any)
     String
     (HMap :mandatory {:find (Vec Symbol)}
           :optional  {:with (Vec Symbol)
                       :in (Vec Symbol)
                       :where (Vec (Vec Any))})))

(def-alias Entity (I (clojure.lang.Associative Any Any)
                     (clojure.lang.ILookup Any Any)
                     (clojure.lang.IPersistentCollection Any)
                     (clojure.lang.Seqable Any)
                     datomic.Entity))

(def-alias FunctionDefinition
  (HMap :mandatory {:lang String
                    :params (Seqable String)
                    :code (U String (Seqable Any))}
        :optional  {:imports (Seqable Any)
                    :requires (Seqable Any)}))

;; Methods

(defmacro ann-namespace [ns-sym & annotations]
  (let [ns-str (name ns-sym)
        forms (for [[sym t] (partition 2 annotations)]
                `(ann ~(with-meta (symbol (str ns-str "/" (name sym)))
                                  (meta ns-sym))
                      ~t))]
    `(do ~@forms)))

(ann-namespace ^:no-check datomic.api
  ; add-listener Any
  as-of (Fn [HDb (U TxN TxID Date) -> HDb]
            [Db (U TxN TxID Date) -> Db])
  as-of-t [HDb -> (Option TxN)]
  basis-t [HDb -> TxN]
  connect [String -> Connection]
  create-database [String -> Boolean]
  datoms [HDb Index Any * -> (Seqable Datom)]
  db [Connection -> Db]
  delete-database [String -> Boolean]
  entid [(U EntityID Keyword) -> EntityID]
  entid-at [Db Keyword (U TxN TxID Date) -> EntityID]
  entity (Fn [Db EntityID -> Entity]
             [Db (Option EntityID) -> (Option Entity)])
  entity-db [Entity -> Db]
  filter [HDb [HDb Datom -> Boolean] -> HDb]
  function [FunctionDefinition -> (I FunctionDefinition clojure.lang.IFn)]
  gc-storage [Connection Date -> nil]
  history [Db -> HDb]
  ident [HDb EntityID -> Keyword]
  index-range [HDb Keyword Any Any -> (Seqable Datom)]
  invoke [HDb EntityID Any * -> Any]
  is-filtered [HDb -> Boolean]
  log [Connection -> (Option Log)]
  next-t [HDb -> TxN]
  part [EntityID -> Keyword]
  q (All [x] [Query Any * -> (Set x)])
  release [Connection -> nil]
  ; remove-tx-report-queue Any
  rename-database [String String -> Boolean]
  request-index [Connection -> Boolean]
  resolve-tempid [HDb TempIDs TxID -> TxID]
  seek-datoms [HDb Index Any * -> (Seqable Datom)]
  shutdown [Boolean -> nil]
  since (Fn [HDb (U TxN TxID Date) -> HDb]
            [Db (U TxN TxID Date) -> Db])
  since-t [HDb -> (U nil TxN TxID Date)]
  squuid [-> java.util.UUID]
  squuid-time-millis [java.util.UUID -> Long]
  sync (Fn [Connection -> (Futur Db)]
           [Connection TxN -> (Futur Db)])
  t->tx (I clojure.lang.IFn$LO
           clojure.lang.IFn$LL
           clojure.lang.IFn$OL
           [(U TxN TxID) -> TxID])
  tempid (Fn [clojure.lang.Keyword -> EntityID]
             [clojure.lang.Keyword Long -> EntityID])
  touch [Entity -> Entity]
  transact [Connection Schema -> (Futur TransactionData)]
  transact-async [Connection Schema -> (Future TransactionData)]
  tx->t (I clojure.lang.IFn$LO
           clojure.lang.IFn$LL
           clojure.lang.IFn$OL
           [(U TxN TxID) -> TxN])
  tx-range [Log (U nil TxN TxID Date) (U nil TxN TxID Date)
            -> (Seqable Transaction)]
  ; tx-report-queue Any
  with [Db Schema -> TransactionData])

;; Macros

(defmacro q> [tbind t & args]
  (assert (= :- tbind) "q> bind must be type annotated (q> :- Type ...)")
  `((inst datomic.api/q (~'Vector* ~@t)) ~@args))

(defn create-type
  "Extract a type from provided field idents stored in Datomic database at uri."
  [uri type-name overrides]
  (let [c (d/connect uri)
        d (d/db c)
        datomic-type-map {:db.type/string 'String
                          :db.type/ref 'Any}
        mt (q> :- [EntityID]
               '[:find ?e
                 :in $ ?t-name
                 :where [?e :type/name ?t-name]]
               d
               type-name)
        t-e (->> (ffirst mt) (d/entity d) d/touch)
        man-attrs (:type/mandatory t-e)
        opt-attrs (:type/optional t-e)
        get-type
          (fn [kw]
            (->>
             (q> :- [Keyword]
                 '[:find ?v
                   :in $ ?a
                   :where [?e :db/ident ?a]
                   [?e :db/valueType ?v]]
                 d
                 kw)
             ffirst
              (d/ident d)
              datomic-type-map))
        t-map (fn [m] (apply merge
                        (map (fn [k] { k (get-type k) }) m)))
        man-t (merge (t-map man-attrs) overrides)
        opt-t (t-map opt-attrs)]
    `(~'HMap :mandatory ~man-t :optional ~opt-t :complete? true)))

(defmacro create-typer [n uri]
  `(defmacro ~n
    ([n# type-name#]
      `(def-alias ~n# ~(create-type ~uri type-name# {})))
    ([n# type-name# overrides#]
      `(def-alias ~n# ~(create-type ~uri type-name# overrides#)))))
