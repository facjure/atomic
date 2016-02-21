Atomic
=======

A tiny library on Datomic, with a declarative api for working with Facts,
Schemas, and Rules.

## Getting Started

For in-memory datomic, setup:

```clojure
(db/connect-anonymously!)
```

Create a schema:

```clojure
(schema/create
  [[:story/title "full title" :string :one :fulltext :index]
  [:story/url "story's permamnent url" :string :one :unique-identity :index]
  [:story/slug "a short title" :string :one]
  [:comments "a collection of comments" :ref :many :component]
  [:comment/body "comment 140 chars or less" :string :one]
  [:comment/author "comment author" :ref :one]])
```

Validate:

```clojure
(schema/has-attribute? :story/title)
```

Create Facts:

```clojure
(fact/add
  {:story/title "Datomic's 1.0 is released"
   :story/url "http://datomic.com/downloads/1.0"
   :story/slug "New, improved declarative api"
   :comment/author 17592186045425
   :comment/body "This is great!"})
```

Find Facts:

```clojure
(query/find-all-by :story/title)
(query/find-changes 17592186045425)
>> {:author/name {:old nil, :new "Stu G"}, :author/email {:old nil, :new "stu@somemail.com"}}
(query/find-all-by :story/title)
(query/find [:story/title] :story/url "http://datomic.com/downloads/1.0")
```

Queries:

```clojure
(query/defquery
  '{:find [?title ?url]
    :in   [$ ?title]
    :where [[?a :story/title ?title]
           [?t :story/url ?url]]
    :values ["Clojure 2.0 announced", "http://clojure.org/downloads/2.0-beta"]})
```

## Status

Atomic is currently in the design phase.

[![Clojars Project](http://clojars.org/facjure/atomic/latest-version.svg)](http://clojars.org/facjure/atomic)

[![Circle CI](https://circleci.com/gh/facjure/atomic.svg?style=svg)](https://circleci.com/gh/facjure/atomic)

## License

© 2015-2016 Facjure, LLC.

Distributed under the Eclipse Public License, the same as Clojure.
