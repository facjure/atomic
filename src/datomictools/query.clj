(ns datomictools.query
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomictools.peer :refer [conn snapshot]]
            [datomictools.utils :refer :all])
  (:import datomic.Util))


(defmacro defquery
  "A simple query api. Takes care of getting the current snapshot of the db, conn etc.,

   Ex:
     (query '{:find   [?title]
              :in     [$ ?artist-name]
              :where  [[?a :artist/name ?artist-name]
                      [?t :track/artists ?a]
                      [?t :track/name ?title]]
              :values ['Joe Satriani']})"
  [q]
  `(let [v# (:values ~q)]
     (d/q ~q @snapshot (first v#))))

(defn query-single
  "Returns the single entity returned by a query."
  [que & args]
  (let [res (apply d/q query-single @snapshot args)
        only (fn [que]
               (assert (= 1 (count res)))
               (assert (= 1 (count (first res))))
               (ffirst res))]
    (d/entity @snapshot (only res))))

(defn grab-entities
  "Returns the entities returned by a query, assuming that
  all :find results are entity ids."
  [query & args]
  (->> (apply d/q query @snapshot args)
       (mapv (fn [items]
               (mapv (partial d/entity @snapshot) items)))))

(defn find-all-by
  "Returns all entities possessing attr."
  [attr]
  (grab-entities '[:find ?e
                   :in $ ?attr
                   :where [?e ?attr]]
                 @snapshot attr))

(defn find-references []
  (d/q '[:find ?ident
         :where
         [?e :db/ident ?ident]
         [_ :db.install/attribute ?e]
         [?e :db/valueType :db.type/ref]]
       @snapshot))

(defn find-entity-id [attr value]
  "Returns 'lazy' entity ids for a given attr, value"
  (let [query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res (->> (d/q query @snapshot attr value)
                 ffirst)]
    (:db/id (d/entity @snapshot res))))

(defn find-by [attr value]
  "Returns 'eager' entities"
  (let [query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res (->> (d/q query @snapshot attr value)
                 ffirst)]
    (d/touch (d/entity @snapshot res))))

;; FIXME
(defn find-by-options [attr value & options]
  "Returns an 'eager' entity with optional hints"
  (let [query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res-id (->> (d/q query @snapshot attr value)
                    ffirst)]
    (cond
     (contains? options :entity) (d/entity @snapshot res-id)
     (contains? options :eager) (d/touch (d/entity @snapshot res-id))
     (contains? options :first-only) (first (d/touch (d/entity @snapshot res-id)))
     :else res-id)))

;; CREDIT: http://dbs-are-fn.com/2013/datomic_history_of_an_entity/
(defn diff-of [entity-id]
  "Show the before/after diff history for an entity id"
  (->>
   ;; Find all tuples of the tx and the actual attribute that changed for a specific entity.
   (d/q
    '[:find ?tx ?a
      :in $ ?e
      :where
      [?e ?a _ ?tx]]
    (d/history @snapshot)
    entity-id)
   ;; Group the tuples by tx - a single tx can and will contain multiple attribute changes.
   (group-by (fn [[tx attr]] tx))
   ;; Grab the actual changes
   (vals)
   ;; Sort with oldest first
   (sort-by (fn [[tx attr]] tx))
   ;; Create a list of maps like '({:the-attribute {:old "Old value" :new "New value"}})
   (map
    (fn [changes]
      {:changes (into
                 {}
                 (map
                  (fn [[tx attr]]
                    (let [tx-before-db (d/as-of @snapshot (dec (d/tx->t tx)))
                          tx-after-db (d/as-of @snapshot tx)
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
                  changes))
       :timestamp (->> (ffirst changes)
                       (d/entity (d/as-of @snapshot (ffirst changes)))
                       :db/txInstant)}))))
