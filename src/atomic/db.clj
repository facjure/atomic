(ns atomic.db
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [clojure.tools.logging :as log]
            [atomic.utils :refer :all])
  (:import datomic.Util))

;;FIXME
(defn- resolve-uri [dbname]
  "Resolve uri based on various storage engines. Looks up :datomic-dbtype in Env.
   Available engines :mem :free :dev :sql :ddb :ddb-local :infinispan"
  (let [dbtype (or (:datomic-dbtype env) :mem)
        uri (cond
              (= dbtype :mem) (str "datomic:mem://" dbname)
              (= dbtype :free) (str "datomic:free://localhost:4334/" dbname)
              (= dbtype :dev) (str "datomic:dev://localhost:4334//" dbname)
              (= dbtype :sql) (str "datomic:sql://" dbname "?"
                                   (:datomic-jdbc-url env))
              (= dbtype :ddb) (str "datomic:ddb://" (:aws-region env) "/"
                                   (:dynamodb-table env) "/" dbname
                                   "?aws_access_key_id=" (:aws-access-key env)
                                   "&aws_secret_key=" (::aws-secret-key env))
              (= dbtype :ddb-local) (str "datomic:ddb-local://8000/" "/"
                                         (:dynamodb-table env) "/" dbname
                                         "?aws_access_key_id="
                                         (:aws-access-key env)
                                         "&aws_secret_key=" (:aws-secret-key env))
              (= dbtype :infinispan) (str "datomic:inf://{cluster-member-host}:{port}/" dbname)
              (= dbtype :cassandra) (str "datomic:cass://{cluster-member-host}[:{port}]/{keyspace}.{table}/"
                                         dbname "[?user={user}&password={pwd}][&ssl=true]"))]
    uri))

(defn create-and-connect [uri]
  "Create a database based on URI and return a Connection."
  (d/create-database uri)
  (d/connect uri))

(defn setup [dbname]
  "Create a database: dbname based on environment config and return a Connection.
   For in-memory dbs, create a fresh copy each time. Calling setup multiple times
   is safe but unnecessary: Datomic caches the instance of Connection for a given URI."
  (let [uri (resolve-uri dbname)]
    (if (= (:datomic-dbtype env) :mem)
      (d/delete-database uri))
    (create-and-connect uri)))

(defn create-anonymous []
  "Create a connection to an anonymous, in-memory db"
  (setup (d/squuid)))

(defn delete [dbname]
  "Deletes the database"
  (log/info "deleting the database" dbname)
  (let [uri (resolve-uri dbname)]
    (d/delete-database uri)))

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
