(ns atomic.schema-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.fact :as fact]))

(def conn (db/create-anonymous!))

(schema/load-edn conn "schema/blog.edn")

(expect
  true
  (schema/has-attribute? conn :user/username))

(expect
  (more-> Object type)
  (schema/create-attribute
   conn
   [:story/title "full title" :string :one :fulltext :index]))

(expect
  true
  (schema/has-attribute? conn :story/title))

;; TODO: Validate results
(expect
  (more-> Object type)
  (schema/create-attributes
   conn
   [[:story/title "full title" :string :one :fulltext :index]
    [:story/url "story's permamnent url" :string :one :unique-identity :index]
    [:story/slug "a short title" :string :one]
    [:comments "a collection of comments" :ref :many :component]
    [:comment/body "comment 140 chars or less" :string :one]
    [:comment/author "comment author" :ref :one]]))

(expect
  true
  (schema/has-attribute? conn :comment/author))

#_(expect
  true
  (schema/find-attribute conn :story/title))

(expect
  :db.cardinality/one
  (schema/find-cardinality conn :story/title))

(expect
  false
  (schema/has-schema? conn :story/title :story/title))

(expect
  not-empty
  (schema/properties conn))
