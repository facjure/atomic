(ns datomictools.history
  (:refer-clojure :exclude [name])
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [datomictools.peer :as peer]
            [datomictools.utils :refer :all])
  (:import datomic.Util))

;; FIXME - Experimental
(defn wrap-datomic
  "A Ring middleware that provides a request-aware database as a value for
   the life of a request. NOTE: Entire db is loaded as a lazy value.
   Use with Sequence abstractions only, otherwise it'll explode your memory!"
  ([handler & [uri]]
     (fn [request]
       (let [db peer/snapshot]
         (handler (assoc request :datomic db))
         (handler request)))))
