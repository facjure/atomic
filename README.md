Atomic
=======

Atomic is a thin wrapper on Datomic, optimized with a declarative api for
working with Peers, Queries, Rules, Schemas, and Validations. Includes
helper functions for common tasks.

## Usage

Leiningen dependency:

    [facjure/atomic "0.2.0"]

[Environ](https://github.com/weavejester/environ) is used to manage environment
variables for AWS, Heroku, Cassandra and other storage engines.

Add the follwing keys in `~/.lein/profiles.clj`:

    :datomic-dbtype (:free)
    :datomic-dbname ("test")
    :datomic-jdbc-url (postgres/heroku url)

## Quickstart

For in-memory datomic, setup:

```clojure

    (db/create-anonymous)
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

Finders:

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

For more examples, see [integration_test.clj](test/atomic/integration_test.clj).

## Documentation

See [docs](doc/index.md) for a developer guide.

## Credits

A big thanks to Craig Andera's excellent tutorials,
[Day of Datomic](https://github.com/Datomic/day-of-datomic). I learned a lot
from studying the source code of the following libraries (and copied some
functions): [mBrainz-sample](https://github.com/Datomic/mbrainz-sample),
[Datomico](https://github.com/cldwalker/datomico),
[Demonic](https://github.com/zololabs/demonic),
[Lib-noir](https://github.com/noir-clojure/lib-noir), and [Adi](https://github.com/zcaudate/adi)

## Status & Roadmap

0.2.0.

Feedback and contributions are welcome.

## License

© Facjure LLC, 2015.

Distributed under the Eclipse Public License, the same as Clojure.
