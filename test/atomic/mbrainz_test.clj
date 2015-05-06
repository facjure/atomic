(ns atomic.mbrainz-test
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomic-tools.db :as db :refer [conn snapshot]]
            [datomic-tools.fact :as fact]
            [datomic-tools.schema :as schema]
            [datomic-tools.query :as query]
            [datomic-tools.utils :refer :all])
  (:import datomic.Util))

(db/connect "datomic:free://localhost:4334/mbrainz-1968-1973")

(schema/has-attribute? :release/country)

(def ledZeppelin 17592186050305)
(def mccartney 17592186046385)

(d/pull (snapshot) '[*] ledZeppelin)

(d/pull (snapshot) [:artist/name :died-in-1966?] ledZeppelin)

