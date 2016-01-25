(ns atomic.database
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [clojure.tools.logging :as log]
            [atomic.utils :refer :all])
  (:import datomic.Util))

(defn- resolve-uri [dbtype dbname options]
  "Resolve uri based on various storage engines. Looks up :aws-access-key
    :aws-access-secret :cassandra-user :cassandra-password from ENV.

   Pass options as needed:-
     :jdbc-url (sql)
     :aws-region :table (ddb, ddb-local)
     :host :port (infinispan)
     :host :port :keyspace :table (cassandra)

   Available engines :mem :dev :sql :ddb :ddb-local :infinispan :cassandra"
  (let [uri (cond
              (= dbtype :mem) (str "datomic:mem://" dbname)
              (= dbtype :dev) (str "datomic:dev://localhost:4334//" dbname)
              (= dbtype :sql) (str "datomic:sql://" dbname "?"
                                   (:jdbc-url options))
              (= dbtype :ddb) (str "datomic:ddb://" (:aws-region options)
                                   "/" (:table options) "/" dbname
                                   "?aws_access_key_id=" (:aws-access-key env)
                                   "&aws_secret_key=" (:aws-secret-key env))
              (= dbtype :ddb-local) (str "datomic:ddb-local://8000/" "/"
                                         (:ddb-table options) "/" dbname
                                         "?aws_access_key_id="
                                         (:aws-access-key env)
                                         "&Aws_secret_key=" (:aws-secret-key env))
              (= dbtype :infinispan) (str  "datomic:inf://"
                                           (:host options) ":"
                                           (:port options) "/"  dbname)
              (= dbtype :cassandra)  (str  "datomic:cass://" (:host options)
                                           "[:{" (:port options) "}]/"
                                           "{" (:keyspace options) "}."
                                           "{" (:table options) "}/" dbname
                                           "[?user={" (:cassandra-user env) "}"
                                           "&password={" (:cassandra-password env) "}]"
                                           "[&ssl=true]"))]
    uri))

(defn create! [dbtype dbname & options]
  "Create a database and return a Connection by resolving the URI from
   dbtype. For in-memory dbs, create a fresh db each time. Calling create!
   multiple times is safe but unnecessary: Datomic caches the instance of
   Connection for a given URI."
  (let [uri (resolve-uri (apply dbtype dbname) options)]
    (if (= dbtype :mem)
      (d/delete-database uri))
    (d/create-database uri)
    (d/connect uri)))

(defn create-anonymous! []
  "Create a connection to an anonymous, in-memory db"
  (let [db-name (str "datomic:mem://" (d/squuid))]
    (d/create-database db-name)
    (d/connect db-name)))

(defn temp-eid
  "Creats a temporary entity id, with an optional number for tracing"
  ([] (temp-eid 0))
  ([n] (d/tempid :db.part/user n)))

(defn incremental-id-gen
  "Constructs a function to generate incremental ids
  when called repeatedly
  (repeatedly 5 (incremental-id-gen 100))
  => [101 102 103 104 105]"
  ([] (incremental-id-gen 0))
  ([n]
   (let [r (atom n)]
     (fn []
       (swap! r inc)
       @r))))

(defmacro with-connection  [uri body]
  "Execute the body with the given conn uri"
  `(binding [conn# (d/connect ~uri)]
     ~@body))

