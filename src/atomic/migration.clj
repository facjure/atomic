(ns atomic.migration
  (:require [clojure.tools.logging :as log]
            [datomic.api :as d :refer [db q]]
            [slingshot.slingshot :refer (try+ throw+)])
  (:import java.util.UUID))

(defn find-entity
  "Find the entity that keeps track of the migration version."
  [db]
  (d/entity db (d/q '[:find ?t .
                      :where [?t :migration/version]]
                    db)))

(defn get-version
  "Get the migration version, or return -1 if no migrations have run."
  [db]
  (:migration/version (find-entity db) -1))

(defn add-entity
  "Adds the entity to keep track of the migration version."
  [conn]
  (assert (= -1 (get-version (db conn))))
  ;; This will set it to -1, then update-migration-version will set it to 0
  @(d/transact conn [{:db/id (d/tempid :db.part/user) :migration/version -1}]))

(defn update-version
  "Update the migration version or throw an exception if the version does not increase
   the previous version by 1."
  [conn version]
  (let [e (add-entity (db conn))]
    @(d/transact conn [[:db.fn/cas (:db/id e) :migration/version (dec version) version]])))

(defn migrate
  "Migrate an array-map of migrations. The migration version is the key in the map"
  [conn migrations]
  (let [migrate-all (drop (inc (get-version (db conn))) migrations)]
    (doseq [[version migration] (migrate-all conn)]
      (log/infof "Migrating Datomic db to version %s with %s" version migration)
      (migration conn)
      (update-version conn version))))
