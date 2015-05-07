(ns atomic.mbrainz-test
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.db :as db]
            [atomic.fact :as fact]
            [atomic.schema :as schema]
            [atomic.query :as query]
            [atomic.utils :refer :all])
  (:import datomic.Util))

#_(db/connect "datomic:free://localhost:4334/mbrainz-1968-1973")

#_(schema/has-attribute? :release/country)

#_(def ledZeppelin 17592186050305)
#_(def mccartney 17592186046385)

#_(d/pull (snapshot) '[*] ledZeppelin)

#_(d/pull (snapshot) [:artist/name :died-in-1966?] ledZeppelin)

