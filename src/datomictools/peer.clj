(ns datomictools.peer
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [clojure.tools.logging :as log]
            [datomictools.utils :refer :all])
  (:import datomic.Util))


(defonce #^{:doc "Connection to Datomic"} conn (atom nil))

;;FIXME
(defn resolve-uri [dbname]
  "Resolve uri for various storage engines.
   TODO: Support map configs for sql and cassandra"
  (let [dbtype (or (:datomic-dbtype env) :mem)
        uri (cond
             (= dbtype :mem) (str "datomic:mem://" dbname)
             (= dbtype :free) (str "datomic:free://localhost:4334/" dbname)
             (= dbtype :dev) (str "datomic:free://localhost:4334//" dbname)
             (= dbtype :sql) (str "datomic:sql://" dbname "?" (:datomic-jdbc-url env))
             (= dbtype :dynamodb) (str "datomic:ddb://" (:aws-region env) "/" (:dynamodb-table env) "/" dbname "?aws_access_key_id=" (:aws-access-key env) "&aws_secret_key=" (::aws-secret-key env))
             (= dbtype :dynamodb-local) (str "datomic:ddb-local://8000/" "/" (:dynamodb-table env) "/" dbname "?aws_access_key_id=" (:aws-access-key env) "&aws_secret_key=" (:aws-secret-key env))
             (= dbtype :infinispan) (str "datomic:inf://{cluster-member-host}:{port}/" dbname)
             (= dbtype :cassandra) (str "datomic:cass://{cluster-member-host}[:{port}]/{keyspace}.{table}/" dbname "[?user={user}&password={pwd}][&ssl=true]"))]
             uri))

(defn setup [dbname]
  "Create a database: dbname, and return a Connection. Store the connection
   in a dynamic var, to be accessible by other fns. Finally, for in-memory dbs,
   create a fresh copy each time. Calling setup multiple times, is safe but
   unnecessary: Datomic caches the instance of Connection for a given URI."
  (let [uri (resolve-uri dbname)]
    ;; ensure fresh in-memory db on each call
    (if (= (:datomic-dbtype env) :mem) (d/delete-database uri))
    (d/create-database uri)
    (reset! conn (d/connect uri))))

(def playground
  "Create a connection to an anonymous, in-memory db"
  (setup (d/squuid)))

(def snapshot #^{:doc "Current snapshot of the database"}
  (atom (d/db @conn)))

(defmacro with-fresh-snapshot
  "Query against a fresh snapshot of db"
  [body]
  `(binding [snapshot# (swap! snapshot (d/db @conn))]
     ~@body))

(defn cleanup! [dbname]
  "WARNING: Deletes the database"
  (log/info "deleting the database" dbname)
  (let [uri (resolve-uri dbname)]
    (d/delete-database uri)))
