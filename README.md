Atomic
=======

Atomic is a thin wrapper on Datomic, optimized with a declarative api for
working with Facts, Schemas, and Rules.

## Quickstart

Add the following to Leiningen or Boot dependency:

	[facjure/atomic "0.2.0"]

Assuming you've acquired a license for
[Datomic Starter Pro](https://my.datomic.com/downloads/pro), install datomic-pro
in your local maven:

	bin/maven-install

[Environ](https://github.com/weavejester/environ) is used to manage environment
variables for AWS, Heroku, Cassandra and other storage engines.

Add the follwing keys in `~/.lein/profiles.clj`:

    :aws-access-key-id "" 
    :aws-secret-key ""

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

## Documentation

See [doc](doc/index.md) for a developer guide.

## Credits

I learned a lot from studying the source code of the following libraries and
demos, and copied some functions:
[Day of Datomic](https://github.com/Datomic/day-of-datomic), 
[mBrainz-sample](https://github.com/Datomic/mbrainz-sample), [Lib-noir](https://github.com/noir-clojure/lib-noir),
and [Adi](https://github.com/zcaudate/adi).

## Status & Roadmap

This library is still in development. Feedback and contributions are welcome.

[![Clojars Project](http://clojars.org/facjure/atomic/latest-version.svg)](http://clojars.org/facjure/atomic)

## License

© Facjure LLC, 2015.

Distributed under the Eclipse Public License, the same as Clojure.
