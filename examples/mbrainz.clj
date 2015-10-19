(ns mbrainz
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.database :as db]
            [atomic.fact :as fact]
            [atomic.schema :as schema]
            [atomic.query :as query]
            [atomic.utils :refer :all])
  (:import datomic.Util))

(comment

 (db/create! "datomic:free://localhost:4334/mbrainz-1968-1973")

 (schema/has-attribute? :release/country)

 (def ledZeppelin 17592186050305)
 (def mccartney 17592186046385)

 (d/pull (snapshot) '[*] ledZeppelin)

 (d/pull (snapshot) [:artist/name :died-in-1966?] ledZeppelin)

)

