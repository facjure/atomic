(ns atomic.query
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.database :as db]
            [atomic.utils :refer :all])
  (:import datomic.Util))

(def included-refs
  #{[:db/lang] [:db/valueType] [:db.excise/attrs] [:db/unique] [:db.alter/attribute]
    [:db.install/function] [:db/excise] [:db/cardinality]})

(defmacro defquery
  "A simple query api. Takes care of getting the current snapshot of db, conn etc.,
  TODO: a lot more todo!
  Ex:
  (query '{:find   [?title]
           :in     [$ ?artist-name]
           :where  [[?a :artist/name ?artist-name]
                   [?t :track/artists ?a]
                    [?t :track/name ?title]]
           :values ['Joe Satriani']})"
  [conn q]
  `(let [v# (:values ~q)]
     (d/q ~q (d/db ~conn) (first v#))))

(defn get-attributes
  "Pull all the attributes of an entity into a map"
  [conn id]
  (let [db (d/db conn)
        e (d/entity db id)]
    (select-keys e (keys e))))

(defn get-attributes-from-result
  "maps through a result set where each item is a single entity and retrieves its attributes"
  [r]
  (map #(get-attributes  (first %)) r))

;;FIXME
(defn- findx [conn attr val]
  (let [eid (d/q '[:find ?e :where [?e attr val]] (d/db conn))
        ent (d/entity (d/db conn) (ffirst eid))]
    (seq ent)))

(defn find-entity
  "Returns the single entity returned by a query."
  [conn que & args]
  (let [snapshot (d/db conn)
        res (apply d/q que snapshot args)
        only (fn [que]
               (assert (= 1 (count res)))
               (assert (= 1 (count (first res))))
               (ffirst res))]
    (d/entity snapshot (only res))))

(defn find-entities
  "Returns the entities returned by a query, assuming that
  all :find results are entity ids."
  [conn query & args]
  (let [snapshot (d/db conn)]
    (->> (apply d/q query snapshot args)
         (mapv (fn [items]
                 (mapv (partial d/entity snapshot) items))))))

(defn find-entity-id [conn attr value]
  "Returns 'lazy' entity ids for a given attr, value"
  (let [snapshot (d/db conn)
        query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res (->> (d/q query snapshot attr value)
                 ffirst)]
    (:db/id (d/entity snapshot res))))

(defn find-all-by
  "Returns all entities possessing attr."
  [conn attr]
  (find-entities '[:find ?e
                   :in $ ?attr
                   :where [?e ?attr]]
                 (d/entid (d/db conn) attr)))

(defn find-references [conn]
  (let [res (d/q '[:find ?ident
                   :wher
                   e
                   [?e :db/ident ?ident]
                   [_ :db.install/attribute ?e]
                   [?e :db/valueType :db.type/ref]]
                 (d/db conn))]
    (remove included-refs res)))

(defn find-by [conn attr value]
  "Returns 'eager' entities"
  (let [snapshot (d/db conn)
        query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res (->> (d/q query snapshot attr value)
                 ffirst)]
    (d/touch (d/entity snapshot res))))

(defn find-by-options [conn attr value & options]
  "Returns an 'eager' entity with optional hints"
  (let [snapshot (d/db conn)
        query '[:find ?e
                :in $ ?attr ?value
                :where [?e ?attr ?value]]
        res-id (->> (d/q query snapshot attr value)
                    ffirst)]
    (cond
      (contains? options :entity) (d/entity snapshot res-id)
      (contains? options :eager) (d/touch (d/entity snapshot res-id))
      (contains? options :first-only) (first (d/touch (d/entity snapshot res-id)))
      :else res-id)))

(defn find-pattern [conn pattern attr valu]
  (let [snapshot (d/db conn)
        eid (find-entity-id conn attr valu)]
    (d/pull snapshot pattern eid)))

(defn -q [q & args]
  (let [key (str q)
        start (.getTime (java.util.Date.))
        res (apply d/q q args)
        end (.getTime (java.util.Date.))]
    (println "Q time: " (- end start))
    res))

(defn query
  "If queried entity id, return single entity of first result"
  [q db & sources]
  (->> (apply -q q db sources)
       ffirst
       (d/entity db)))

(defn query-by
  "Return single entity by attribute existence or specific value"
  ([db attr]
    (query '[:find ?e :in $ ?a :where [?e ?a]] db attr))
  ([db attr value]
    (query '[:find ?e :in $ ?a ?v :where [?e ?a ?v]] db attr value)))
