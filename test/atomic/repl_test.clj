(ns atomic.repl-test
  (:require [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [atomic.db :as db]
            [atomic.fact :as fact]
            [atomic.schema :as schema]
            [atomic.query :as query]
            [atomic.history :as history]
            [atomic.utils :refer :all]
            [atomic.validations :as v])
  (:import datomic.Util))

(def conn (db/create-anonymous!))

;; REPL TESTS
(comment

  (schema/load-edn conn "schema/blog.edn")

  (schema/has-attribute? conn :user/username)

  (schema/create-attribute conn
                           [:story/title "full title" :string :one :fulltext :index])

  (schema/has-attribute? conn :story/title)

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

  (schema/create-attributes conn authors)
  (schema/create-attributes conn social-news)

  (schema/has-attribute? conn :author/name)
  (schema/has-attribute? conn :comment/author)

  (def eid1
    (fact/add conn
              {:author/name "Stu"
               :author/email "stu@somemail.com"}))

  (fact/add conn
            {:author/name "Rich H"
             :author/email "rich@somemail.com"})

  #_(query/find-references conn)

  (def eid (query/find-entity-id conn :author/name "Stu"))

  (fact/retract conn eid
                [:author/email "stu@somemail.com"])

  (query/get-attributes conn eid)

  (history/changes conn eid)

  #_(query/find-by conn :author/name "Stu")

  #_(query/find-all-by conn :author/name)

  (fact/add conn
            {:story/title "Datomic's 1.0 is released"
             :story/url "http://datomic.com/downloads/1.0"
             :story/slug "New, improved declarative api"
             :comment/author 17592186045425
             :comment/body "This is great!"})

  (fact/add conn
            {:story/title "Clojure 2.0 announced"
             :story/url "http://clojure.org/downloads/2.0-beta"
             :story/slug "Static Typing, Erlang-style error handling, improved core-async"
             :comment/author eid
             :comment/body "This is great!"})

  (fact/retract conn eid
                {:story/title "Clojure 2.0 announced"})

  (query/find-all-by conn :story/title)

  #_(query/find-pattern conn [:story/title] :story/url "http://datomic.com/downloads/1.0")

  #_(query/find-pattern conn [:author/email] :author/name "Stu G")

  #_(v/is-email?
     (:author/email
      (query/find-pattern [:author/email] :author/name "Stu G")))

  (query/defquery conn
    '{:find [?title ?url]
      :in   [$ ?title]
      :where [[?a :story/title ?title]
              [?t :story/url ?url]]
      :values ["Clojure 2.0 announced", "http://clojure.org/downloads/2.0-beta"]}))
