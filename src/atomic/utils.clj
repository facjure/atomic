(ns atomic.utils
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.api :as d]))

(defmacro dbg [body]
  "Cheap inline debugging"
  `(let [x# ~body]
     (println x#)
     x#))

(defmacro set! [var-from var-to]
  `(alter-var-root (var ~var-from) (constantly ~var-to)))

(definline assert-nil [v ^String m]
  `(when (nil? ~v)
     (throw (IllegalArgumentException. ~m))))

(defn fetch [io-type file]
  "Fetch file from any io-type; an api neutralizer for io.
   Usage:
       fetch :resource 'data/poem.txt'"
  (cond
    (= io-type :str) (str file)
    (= io-type :resource) (slurp (io/resource file))
    (= io-type :url) (slurp (io/as-url file))
    (= io-type :file) (slurp (io/as-file file))
    :else nil))

(defn int-to-base62
  "Convert an integer to base62"
  ([n] (int-to-base62 (rem n 62) (quot n 62) ""))
  ([remainder number accum]
   (let [alphabet "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"]
     (cond
       (zero? number) (str (nth alphabet remainder) accum)
       :else (recur
              (rem number 62)
              (quot number 62)
              (str (nth alphabet remainder) accum))))))

(defn gen-short-url []
  "Generate a unique, short URL, with 8 chars"
  (subs
   (int-to-base62
    (java.math.BigInteger. (str/replace (str (java.util.UUID/randomUUID)) "-" "") 16))
   0 8))

(defn last-transaction-time [db]
  (let [t (-> db d/basis-t)]
    [t (ffirst (d/q '[:find ?t
                      :in $ ?tx
                      :where [?tx :db/txInstant ?t]]
                    db
                    (d/t->tx t)))]))

(defmethod print-method datomic.peer.LocalConnection
  [conn w]
  (.write w  (if-let [db (try (d/db conn)
                              (catch Exception t))]
               (let [[t dt] (last-transaction-time db)]
                 (format "#connection%s" {t dt}))
               "#connection{}")))

(defmethod print-method datomic.db.Db
  [db w]
  (.write w (if-let [[t dt] (last-transaction-time db)]
              (format "#db%s" {t dt})
              "#db{}")))
