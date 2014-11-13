(ns datomic-tools.validations
  (:require
   [clojure.set :refer :all]
   [clj-time.core :as t])
  (:import [java.util UUID]))


(defn has-value?
  "Returns true if v is truthy and not an empty string."
  [v]
  (and v (not= v "")))

(defn has-values?
  "Returns true if all members of the collection has-value?"
  [coll]
  (let [vs (if (map? coll)
             (vals coll)
             coll)]
    (every? has-value? vs)))

(defn not-nil?
  "Returns true if v is not nil"
  [v]
  (boolean (or v (false? v))))

(defn min-length?
  "Returns true if v is greater than or equal to the given len"
  [v len]
  (>= (count v) len))

(defn max-length?
  "Returns true if v is less than or equal to the given len"
  [v len]
  (<= (count v) len))

(defn matches-regex?
  "Returns true if the string matches the given regular expr"
  [v regex]
  (boolean (re-matches regex v)))

(defn valid-number?
  "Returns true if the string can be parsed to a Long"
  [v]
  (try
    (Long/parseLong v)
    true
    (catch Exception e
      false)))

(defn greater-than?
  "Returns true if the string represents a number > given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))

(defn less-than?
  "Returns true if the string represents a number < given."
  [v n]
  (and (valid-number? v)
       (< (Long/parseLong v) n)))

(defn equal-to?
  "Returns true if the string represents a number = given."
  [v n]
  (and (valid-number? v)
       (== (Long/parseLong v) n)))

(defn is-email?
  "Returns true if v is an email address"
  [v]
  (if (nil? v)
    false
    (matches-regex? v #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")))

(defn valid-type? [attr atype]
  "Check if the attribute is of type (keyword)"
  (let [type-found (type attr)]
    (case atype
      :uuid (= type-found java.util.UUID)
      :boolean (= type-found java.lang.Boolean)
      :string (= type-found java.lang.String)
      :long (= type-found java.lang.Long)
      :double (= type-found java.lang.Double)
      :time (= type-found org.joda.time.DateTime)
      :bigint (= type-found clojure.lang.BigInt )
      :keyword (= type-found clojure.lang.Keyword)
      :vector (= type-found clojure.lang.PersistentVector)
      :set (= type-found clojure.lang.PersistentHashSet)
      :map (= type-found clojure.lang.PersistentArrayMap)
      false)))

