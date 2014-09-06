Datomic Tools
=============

Datomic-tools is a thin wrapper on Datomic, optimized with a declarative api for working with Peers, Queries, and Schemas.

## Usage

For in-memory datomic, setup:

```clojure
    (peer/setup "test")
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

Validations:

```clojure
    (schema/has-attribute? :story/title)
```

Queries:

```clojure
    (query/defquery '{:find [?title ?url]
                      :in   [$ ?title]
                      :where [[?a :story/title ?title]
                              [?t :story/url ?url]]
                      :values ["Datomic"]})

```

## Documentation

A tiny guide aimed at beginners to Clojure and Datomic is under [docs/datomic.md](docs/datomic.md). It's a _work in progress_, and suggestions are welcome.

## Credits

A big thanks to Craig Andera's excellent tutorials, [Day of Datomic](https://github.com/Datomic/day-of-datomic). I learned a lot from studying the source code of the following libraries (and copied some functions): [mBrainz-sample] (https://github.com/Datomic/mbrainz-sample), [Datmico](https://github.com/cldwalker/datomico). Writing this library wouldn't be possible without them.

## Status & Roadmap

**Early development**.

I still have plenty to learn about the world of Clojure, so I welcome feedback and contributions.

## License

© Facjure LLC, 2014.

Distributed under the Eclipse Public License, the same as Clojure.
