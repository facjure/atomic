(ns datomic-tools.integration-test
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomictools.peer :as peer]
            [datomictools.fact :as fact]
            [datomictools.schema :as schema]
            [datomictools.query :as query]
            [datomictools.utils :refer :all])
  (:import datomic.Util))


;; GOALS
; setup database
; create schema
; validate schema
; create schemas
; insert data
; sql-like queries
; key-value queries
; document-oriented queries
; graph-like queries
; historic queries
; backup/load

(peer/setup "news")

(schema/create-attribute [:story/title "full title" :string :one :fulltext :index])

(schema/has-attribute? :story/title)

(def news [:story/title "full title" :string :one])

(def social-news
  [[:story/title "full title" :string :one :fulltext :index]
   [:story/url "story's permamnent url" :string :one :unique-identity :index]
   [:story/slug "a short title" :string :one]
   [:comments "a collection of comments" :ref :many :component]
   [:comment/body "comment 140 chars or less" :string :one]
   [:comment/author "comment author" :ref :one]])

(schema/create social-news)

(fact/add
 {:story/title "Datomic's 1.0 is released"
  :story/url "http://datomic.com/downloads/1.0"
  :story/slug "New, improved declarative api"
  :comment/author "Stu"})

(query/find-all-by :story/title)

(query/defquery '{:find [?title ?url]
                  :in   [$ ?title]
                  :where [[?a :story/title ?title]
                          [?t :story/url ?url]]
                  :values ["Datomic"]})

;(schema/load "schema/news.edn")

