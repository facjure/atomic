(ns datomic-tools.integration-test
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic-tools.db :as db]
            [datomic-tools.fact :as fact]
            [datomic-tools.schema :as schema]
            [datomic-tools.query :as query]
            [datomic-tools.utils :refer :all])
  (:import datomic.Util))


(db/create-anonymous)

(schema/has-attribute? :story/title)

(schema/create-attribute
   [:story/title "full title" :string :one :fulltext :index])

(schema/has-attribute? :story/title)

(def authors
  [[:author/name "author's fullname" :string :one :fulltext :index]
   [:author/email "author's email" :string :one :unique-identity :index]])

(def social-news
  [[:story/title "full title" :string :one :fulltext :index]
   [:story/url "story's permamnent url" :string :one :unique-identity :index]
   [:story/slug "a short title" :string :one]
   [:comments "a collection of comments" :ref :many :component]
   [:comment/body "comment 140 chars or less" :string :one]
   [:comment/author "comment author" :ref :one]])

(schema/create authors)
(schema/create social-news)

(schema/has-attribute? :author/name)
(schema/has-attribute? :comment/author)

(fact/add
  {:author/name "Stu G"
   :author/email "stu@somemail.com"})

(fact/add
  {:author/name "Rich H"
   :author/email "rich@somemail.com"})

(query/find-references)

(query/find-entity-id :author/name "Stu G")
(query/find-entity-id :author/name "Rich H")

(query/get-attributes 17592186045425)

(query/find-changes 17592186045425)

(query/history :author/name "Stu G")

(query/find-all-by :author/name)

(query/find-by :author/name "Stu G")

(fact/add
 {:story/title "Datomic's 1.0 is released"
  :story/url "http://datomic.com/downloads/1.0"
  :story/slug "New, improved declarative api"
  :comment/author 17592186045425
  :comment/body "This is great!"})

(fact/add
 {:story/title "Clojure 2.0 announced"
  :story/url "http://clojure.org/downloads/2.0-beta"
  :story/slug "Static Typing, Erlang-style error handling, improved core-async"
  :comment/author 17592186045429
  :comment/body "This is great!"})

(query/find-all-by :story/title)

(query/defquery '{:find [?title ?url]
                  :in   [$ ?title]
                  :where [[?a :story/title ?title]
                          [?t :story/url ?url]]
                  :values ["Clojure 2.0 announced", "http://clojure.org/downloads/2.0-beta"]})

;; Load Schema ;;

(schema/load-edn "schema/news.edn")
(schema/has-attribute? :comment/author)
