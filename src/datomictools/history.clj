(ns datomictools.history
  (:refer-clojure :exclude [name])
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomictools.query :refer :all]
            [datomictools.peer :refer [conn snapshot]]
            [datomictools.utils :refer :all])
  (:import datomic.Util))


(defn history-of-ids [ann-url]
  "History of any entity. Returns entity ids"
  (let [ann-id  (:db/id (find-by :annotations/url ann-url))
        history (d/history @snapshot)
        history-que '[:find ?tx ?a
                      :in $ ?e
                      :where
                      [?e ?a _ ?tx]]]
    (->> (d/q history-que history ann-id)
         (sort-by first))))


(defn history [url]
  "Show the history for an entity id"
  (let [entity-id (:db/id (find-by :annotations/url url))
        hist (d/history @snapshot)]
    (->>
     ;; Find all tuples of the tx and the actual attribute that changed for a specific entity.
     (d/q
      '[:find ?tx ?a
        :in $ ?e
        :where
        [?e ?a _ ?tx]]
      hist)
      entity-id)
     ;; Group the tuples by tx - a single tx can and will contain multiple attribute changes.
     (group-by (fn [[tx attr]] tx))
     ;; Grab the actual changes
     (vals)
     ;; Sort with oldest first
     (sort-by (fn [[tx attr]] tx))
     ;; Create a list of maps like '({:the-attribute {:old "Old value" :new "New value"}})
     (map
      (fn [changes]
        (into
         {}
         (map
          (fn [[tx attr]]
            (let [tx-before-db (d/as-of @snapshot (dec (d/tx->t tx)))
                  tx-after-db (d/as-of @snapshot tx)
                  tx-e (d/entity tx-after-db tx)
                  attr-e-before (d/entity tx-before-db attr)
                  attr-e-after (d/entity tx-after-db attr)]
              [(:db/ident attr-e-after)
               (get
                (d/entity tx-after-db entity-id)
                (:db/ident attr-e-after))]))
          changes))))))

