Atomic
=======

Atomic is a wrapper on Datomic, designed with a declarative api for
working with Facts, Schemas, and Rules.

## Getting Started

Add the following to Leiningen or Boot dependency:

	[facjure/atomic "0.2.0"]

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

More coming soon.

## Credits

I borrowed ideas and functions from the following examples and libraries:
[Day of Datomic](https://github.com/Datomic/day-of-datomic),
[mBrainz-sample](https://github.com/Datomic/mbrainz-sample),
[Lib-noir](https://github.com/noir-clojure/lib-noir),
[Adi](https://github.com/zcaudate/adi), [Dato](https://github.com/sgrove/dato).

## Status & Roadmap

Atomic is currently in the design phase.

[![Clojars Project](http://clojars.org/facjure/atomic/latest-version.svg)](http://clojars.org/facjure/atomic)

[![Circle CI](https://circleci.com/gh/facjure/atomic.svg?style=svg)](https://circleci.com/gh/facjure/atomic)

## License

© 2015 Facjure, LLC.

Distributed under the Eclipse Public License, the same as Clojure.
