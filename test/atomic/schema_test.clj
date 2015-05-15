(ns atomic.schema-test
  (:use expectations)
  (:require [datomic.api :as d]
            [atomic.db :as db]
            [atomic.schema :as schema]
            [atomic.fact :as fact]))

(def conn (db/create-anonymous!))

;; Manual Test

(defn create-sample [conn]
  (d/transact
   conn
   [ ;; User
    {:db/id (d/tempid :db.part/db)
     :db/ident :user/username
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/value
     :db/index true
     :db/doc "This user's username"
     :db.install/_attribute :db.part/db}
    ;; Category
    {:db/id (d/tempid :db.part/db)
     :db/ident :category/name
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/doc "This category's name"
     :db.install/_attribute :db.part/db}
    ;; Article
    {:db/id (d/tempid :db.part/db)
     :db/ident :article/title
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "This article's title"
     :db.install/_attribute :db.part/db}
    {:db/id (d/tempid :db.part/db)
     :db/ident :article/author
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one
     :db/doc "This article's author"
     :db.install/_attribute :db.part/db}
    {:db/id (d/tempid :db.part/db)
     :db/ident :article/category
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/many
     :db/doc "This article's categories"
     :db.install/_attribute :db.part/db}
    {:db/id (d/tempid :db.part/db)
     :db/ident :article/body
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "This article's body"
     :db.install/_attribute :db.part/db}
    {:db/id (d/tempid :db.part/db)
     :db/ident :article/comments
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/many
     :db/isComponent true
     :db/doc "This article's comments"
     :db.install/_attribute :db.part/db}
    ;; Comment
    {:db/id (d/tempid :db.part/db)
     :db/ident :comment/author
     :db/valueType :db.type/ref
     :db/cardinality :db.cardinality/one
     :db/doc "This comment's author"
     :db.install/_attribute :db.part/db}
    {:db/id (d/tempid :db.part/db)
     :db/ident :comment/body
     :db/valueType :db.type/string
     :db/cardinality :db.cardinality/one
     :db/fulltext true
     :db/doc "This comment's body"
     :db.install/_attribute :db.part/db}])

  (d/transact
   conn
   [;; A user writing an article
    {:db/id (db/temp-eid -100)
     :user/username "john.smith"}
    {:db/id (db/temp-eid -200)
     :category/name "Functional Programming"}
    {:db/id (db/temp-eid -200)
     :article/title "Monads in Pictures"
     :article/author (db/temp-eid -100)
     :article/category (db/temp-eid -200)
     :article/body "http://bit.ly/13lW7WF"}
    ;; A user posting a comment
    {:db/id (db/temp-eid -101)
     :user/username "kate.nash"}
    {:db/id (db/temp-eid -400)
     :comment/author (db/temp-eid -101)
     :comment/body "Great article!"
     :article/_comments (db/temp-eid -300)}
    ;; Another user writing an article
    {:db/id (db/temp-eid -102)
     :user/username "alex.hill"}
    {:db/id (db/temp-eid -201)
     :category/name "Clojure News"}
    {:db/id (db/temp-eid -301)
     :article/title "Clojure Conj DC 2013"
     :article/author (db/temp-eid -102)
     :article/category (db/temp-eid -201)
     :article/body "See http://clojure-conj.org/"}
    ;; Two users posting comments
    {:db/id (db/temp-eid -103)
     :user/username "scott.carter"}
    {:db/id (db/temp-eid -401)
     :comment/author (db/temp-eid -103)
     :comment/body "Looking forward to it"
     :article/_comments (db/temp-eid -301)}
    {:db/id #db/id [:db.part/user -402]
     :comment/author (db/temp-eid -102)
     :comment/body "Me too!"
     :article/_comments (db/temp-eid -102)}]))

(create-sample conn)

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
