(ns atomic.ring
  (:refer-clojure :exclude [name])
  (:require [environ.core :refer [env]]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.db :refer [conn snapshot]]
            [atomic.utils :refer :all])
  (:import datomic.Util))

;; FIXME - Experimental
(defn wrap-datomic
  "A Ring middleware that provides a request-aware database as a value for
   the life of a request."
  ([handler & [uri]]
     (fn [request]
       (handler (assoc request :datomic (snapshot)))
       (handler request))))
