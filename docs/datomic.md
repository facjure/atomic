---
author: Priyatam Mudivarti
title: A rough guide to Datomic
license: [CC by NC 3.0](http://creativecommons.org/licenses/by-nc/2.0/)
tags:
  - datomic
  - distributed-databases
  - functional-databases
  - clojure
  - datalog
---

# Introduction

> Store a collection of facts
  that do not change, add
  new facts like memory
  superseding old facts.
  Build a snapshot of facts
  that collect knowledge
  over time to reveal meaning

Datomic is a database that stores a collection of Facts.

Facts are events. Each event describes what happened at what _time_, using just five attributes. Once stored in memory, like we do in life, facts don't change.

Naturally, Datomic supports only two operations: _add_ and _retract_.

Old facts can be superseded by new facts over time. The state of the database is a value defined by the set of facts in effect at a given moment in time. Hence, Datomic’s data model—based on immutable facts stored over time, enables a physical design that is different from most databases. Instead of processing all requests in a single server component, Datomic distributes _read_, _write_ and _query processing_ across different components, and even swaps storage engines.

Datomic achieves this separation by creating distinct apis for read, write, and data storage, with the notion of local and remote snapshots of data. Since data is always a snapshot in time (you always get the same value), you can ask questions like _what is the current policy?_ or _what happened to the user as of last year_ from a local repo. By appending facts over time, you cut down network calls since you only need a recent snapshot of data.

Imagine 'pulling' from the head in a git repo, and working from local?

I know.

Once you access the local data you can use a richer api for reads, directly from clojure code, its data structures and functions, all that a language can do, without ever learning new abstractions like sql, map-reduce, or even pig. What’s more, _edn_ format, a data-definition and data-modification language is simply a subset of Clojure!

Delegating the storage engine to any blob on engines like Couchbase, Dynamodb, Riak or Postgres, Datomic further enables a virtualization of cloud data providers at scale.

Essentially, Datomic makes NoSql solutions redundant from a developer's point of view.

For the [CAP](http://en.wikipedia.org/wiki/CAP_theorem) folks, Datomic is a CP system.

# Getting Started

## Installation

First, download the latest version of [Datomic Free](http://www.datomic.com/get-datomic.html):

    wget https://my.datomic.com/downloads/free/0.9.4880.2
    unzip datomic-free-0.9.4880.2.zip

You can use [Datomic Pro Starter](http://downloads.datomic.com/pro.html) with no additional cost by signing up and getting a license key. Once you get a key, make sure you swap the dependencies in [project.clj](project.clj).

## Configuration

[Environ](https://github.com/weavejester/environ) is used to manage environment variables for AWS, Heroku, Cassandra and other storage engines.

Let's begin by adding the follwing keys in `~/.lein/profiles.clj`

Setup the type in env:

    :datomic-dbtype = :mem :free :sql :dynamodb :dynamodb-local :riak :couchbase :infinispan

Postgres (Heroku):

    :datomic-jdbc-url

AWS:

- :aws-access-key
- :aws-secret-key
- :datomic-name ("test")

Let's see if our setup works.

    lein deps (first time)
    lein repl

If all goes well, you should see a repl with no errors.

## Quickstart

Start the transactor (it runs datomic):

    cd datomic-free-0.9.4880.2
    bin/transactor config/samples/free-transactor-template.properties

If you already purchased a Pro-Starter license, copy the relevant config properties from `config/samples/<type>.propertoes` to `config/dev.properties`.

Edit config/dev.properties and paste your license key.

Start the transactor:

    cd datomic-pro-starter-0.9.4880.2
    bin/transactor config/samples/free-transactor-template.properties

A basic setup:

    (ns myapp
      (:require [datomic.api :as d]))

    (defonce uri "datomic:free://localhost:4334/helloapp”)
    (def conn (d/connect uri))
    (def db (d/db conn))

A basic query (don’t worry about what this means yet):

    (d/q {:find [?title]
          :in [$ ?artist-name]
          :where [[?a :artist/name ?artist-name]
                 [?t :track/artists ?a]
                 [?t :track/name ?title]]})

If you’re using 'Day of datomic' tutorials, change the settings in leiningen to `pro` and remove your local maven repo.

That’s it for now — let’s explore the core concepts of Datomic.

# PART I - Concepts

Datomic is designed to be directly programmable using data that represents the domain model.

How do you model this Data? With Entities, Attributes, and Values.

An Entity, referred by a generated id or keyword, possess Attributes and Values. A value can be scalar (String, Integer, etc.,), or a reference to another Entity. This reference establishes a relationship between two entities. How do you represent this reference? Like a _foreign-key_, except in Datomic a reference is just the value of the referenced attribute.

Therefore, Datomic entities are definied recursively — like lists in lisp!

## Facts

The data model in Datomic is based around atomic facts called datoms. A datom is a 4-tuple consisting of:

    [entity attribute value transaction]

Any model in real or manufactured world can be represented in this 4-taple.

For ex, a Person can be modeled as:

    [100 :person/firstName "Rich" 1000]

Note that entity and transaction here are arbitrary numbers (can be generated by Datomic).

A blog can be modeled as two facts:

    [200 :blog/title "on datomic" 1000]
    [200 :blog/entry "datomic will change the way you think of databases" 1000]

A registration page can save a web form as a set of facts:

     [100 :user.registration/name "clojure-addict" 1000]
     [200 :user.registration/email "hello@example.com" 1000]
     [300 :user.registration/address "1 mission street san francisco ca 94103" 1000]

And so on ...

## Entities

A Datomic entity is simply a _Fact_ providing a lazy, associative view of all the information that can be reached from its id.

Let' revisit our 4-tuple Fact:

    [entity attribute value transaction]

- entity: entity id (typically, autogenerated)
- attribute: Clojure keyword representing both the model (namespace :person) and attribute name (firstName)
- value: any value
- transaction: tx id, generated by datomic; used internall for time-based queries

Note that entities are not a mapping layer between databases and application code: they're a direct translation from information stored in the database to application as raw data structures.

Entity References are bi-directional by default:

    [{:db/id #db/id[:db.part/user -1]
      :person/name "Bob"
      :person/spouse #db/id[:db.part/user -2]}
     {:db/id #db/id[:db.part/user -2]
      :person/name "Alice"
      :person/spouse #db/id[:db.part/user -1]}]

Entity attributes are accessed lazily as you request them. Queries and query results are all represented as simple collection data structures.

Entities aren't typed.

Entity = partition + time.

## Identity

Datomic auto-generates entity ids and stores them as part of every datom.

To simplify application access, `idents` provide a keyword-based lookup to entities that can be used whereever an api call expects entity 'id'.

For ex the following record,

    {:db/id #db/id[:db.part/db]
     :db/ident :person/name}

can be accessed via `:person/name` lookup, instead of the generated id.

`ident` is designed to be fast. It can also be used to implement enumerated tags.

    {:db/id #db/id[:db.part/db]
     :db/ident :label/type
     :db/doc "Enum, one of :label.type/distributor, :label.type/holding,
        :label.type/production, :label.type/originalProduction,
        :label.type/bootlegProduction, :label.type/reissueProduction, or
        :label.type/publisher."}

`Unique identities` allow attributes to have unique values.

    {:db/id #db/id[:db.part/db]
     :db/ident :person/email
     :db/unique :db.unique/identity}

A unique identity attribute is always indexed by value.

`Lookup Refs' are _Business Keys_ on steroids: a list containing an attribute and value

    [:person/email "joe@example.com"]

To refer to existing entities in a transaction, avoiding extra lookup code, use:

    {:db/id [:person/email "rich@example.com"]
     :person/name :rich}

Note that Lookup refs _cannot_ be used in queries.

`Squuids` provide efficient, globally unique identifiers. They are semi-sequential uuids. As long you don't generate thousands of squuids every millisecond, indexes based on squuids will not fragment: the first part of a squuid is based on the system time.

Squuids are _not_ real UUIDs, but they come close.

In general, queries against a single database can lookup entity ids via other kinds of identifiers, but for efficiency should join by entity id.

## Schemas

Let's look at an example:

    {:db/id #db/id[:db.part/db]
     :db/ident :artist/gid
     :db/valueType :db.type/uuid
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity
     :db/index true
     :db/doc "The globally unique MusicBrainz ID for an artist"
     :db.install/_attribute :db.part/db}

Three core concepts:  `db/ident`, `:db/valueType`, `:db/cardinality`

Schema is represented as a map.

Notes:

- schema is immutable; history is immutable
- maps describing entities (or tuples)
- `#db/id` is a temp-id
- db.unique is enforced by the transactor
- entity ids will change—don't rely on them; use `db.unique/identity`
- valueType is a generic

## Transactions

Datomic transactions are ACID: Atomic, Consistent, Isolated, and Durable.

If you're viewing from CRUD operations, there is no `U` & `D` in Dataomic, only Create and Read. Update is considered as 'Retract'ing of an existing attribute.

Transaction requests are data structures. Each operation to the db is represented as a tuple of four things:

    [op entity-id attribute value]
    ;; oper is one of :db/add and :db/retract.

    [data-fn args*]

Sample:

    {:db/id entity-id
     attribute value
     attribute value
     ... }

Create - `db:add`

To add data to an existing entity, build a transaction using :db/add implicitly with the map structure (or explicitly with the list structure),

    (d/transact conn
        [;; A user posting an article
          {:db/id #db/id [:db.part/user -100]
           :user/username "rich.hickey"}
          {:db/id #db/id [:db.part/user -200]
           :category/name "Functional Programming"}
          {:db/id #db/id [:db.part/user -300]
           :article/title "Scala, the bad parts"
           :article/author #db/id [:db.part/user -100]
           :article/category #db/id [:db.part/user -200]
           :article/body "Scala is ..."}])

This example shows creating data within a transaction:

- transact takes a vector of vectors: [Op, Ent, Attr, Val]
- transact can be sync or async (look for the right api param)
- sync transactions have an implicit timeout
- Either way you get a promise
- Each vector is a m which gets converted to E A V
- Single value vs multi-valued attribute => db cardinality
> joe likes tea vs joe likes xv
- [Op, Ent, Attr, Val] - tx api
- [Op, Ent, Attr, Val, Tx] - dataomic's api
- Within a tx tempid -100 denotes the same "new" entity
- Scope of a tempid is a single tx
- Every tx is yet another datum
- Consider a tx as a vector of assertions
- tx itself is an entity!
- Tuples within a tx is not ordered
- A single tx can and will contain multiple attribute changes.

## Queries

Queries are data strucuture that accept a find clause, where conditions, and optoinal attribute values.

The syntax looks like this: find, in, and where 'data patterns':

    [:find ?title
     :where
    [?e :movie/year 1987]
    [?e :movie/title ?title]]

The above query finds all movie titles in the year 1987.

Query is data. Variables are symbols: strings, numbers keywords, literals, and `#tags`

The order of the data patterns does not matter.

Datomic has implicit joins. This means you don't have to apply markers for join attributes; simply mention them in the next tuple.

    [:find ?attr
     :where
     [?p :person/name]
     [?p ?a]
     [?a :db/ident ?attr]]

In the above query, checking two entity ids (`?p`) in consecutive constraints fires off an implicit join. Sam goes with `?a`.

Datomic doesn't optimize query for size of datasets. This strategy can be changed in code.

Data is stored in chunks in a tree in storage. Datalog query retrieves chunks from the storage.

# Part II - Deep dive

## Parameterized Queries

With parameterized queries, you can pass scalars (basic types like String, Integer, etc.,), Tuples, Collections, and Relations as 'input' to queries.

**Scalars**

    [:find ?title
     :in $ ?name
     :where
       [$ ?p :person/name ?name]
       [$ ?m :movie/cast ?p]
       [$ ?m :movie/title ?title]]

**Tuples**

    [:find ?title
     :in $ [?director ?actor]
     :where
       [?d :person/name ?director]
       [?a :person/name ?actor]
       [?m :movie/director ?d]
       [?m :movie/cast ?a]
       [?m :movie/title ?title]]

**Collections**

    [:find ?title
     :in $ [?director ...]
     :where
       [?p :person/name ?director]
       [?m :movie/director ?p]
       [?m :movie/title ?title]]

**Relations**

Consider a relation `[title box-office-earnings]`:

    [["Die Hard" 140700000]
    ["Alien" 104931801]
    ["Lethal Weapon" 120207127]]

A query could be:

    [:find ?title ?box-office
     :in $ ?director [[?title ?box-office]]
     :where
       [?p :person/name ?director]
       [?m :movie/director ?p]
       [?m :movie/title ?title]]

## Expression Clauses

**Predicates**

    ;;[(predicate ...)]

    [:find ?e :where [?e :age ?a] [(< ?a 30)]]

**Functions**

    ;;[(function ...) bindings]

    (d/q '[:find ?prefix
       :in [?word ...]
       :where [(subs ?word 0 5) ?prefix]]
        ["hello" "clojurians"])

## Indexes

Indexes is the magic behind Datomic's performance.

They're a sorted set of datums and every datom is stored in two or more indexes:

EAVT, AEVT, AVET, VAET

- EAVT = row oriented, indexed by default; contains all datoms
- AEVT = column oriented; contains all datoms
- AVET = key-value; contains attributes that are db:indexed, :db/unique
- VAET = reverse index, traverse refs (db.type/ref)

In other words, Datomic allows several 'views' of the data:

- Rows      = datoms sharing a common E
- Columns   = datoms sharing a common A
- Document  = traversal of attributes
- Graph     = traversal of reference attributes

Indexes are stored in chunks, organized into trees. The leaf where the chunk is stored is a fressian encoded data. Every segment has a guuid whose value is a zipped data (chunk value).

## Caching

Datomic caches only immutable data, so all caches are valid forever.

## Constraints

- speculative, compare-set, transactor fn (macros!)->userful for atomic operations
- transactor fns are like macros for transactors.
- you can send arbitrary fn to transactors and let it run on peers.

## Database Functions

TODO

## Dataflow

We have now been introduced to the five types of functions: transform, emit, derive, continue and effect.

Each is a pure function.

Transform and derive functions produce values which change the state of part of the information model.

Derive and emit functions are called when parts of the information model, which are inputs to these functions, are changed.

All of the dependencies between functions and the information model are described in a data structure.

## Exceptions and Error codes

TODO

## Rules

Rules are analagous to functions. Instead of having forms, rules simply enclose a bunch of constraints (datalog).

Abstracting reusable parts of queries into rules, enable query composition at will.

Seriously.

Consider this:

    [?p :person/name ?name]
    [?a :article/body ?p]
    [?a :article/title ?title]

Changed into a rule:

    [(blog-post ?name ?title)
     [?p :person/name ?name]
     [?a :article/body ?p]
     [?a :article/title ?title]]

Of course this is a simple example. Rule composition is amazing.

Datalog rules can even implement graph traversal algorithms.

# Architecture

## Peers

Applications talk to Peers. They take care of queries, caching, and hide the details of syncing updates from other peers.

Peers:

- Query and access data locally using a database value.
- Cache extensively (LRU), representing a partial copy of all the facts.
- Write new facts by asking the Transactor to add them to the Storage Service.
- Get notified by the Transactor about new facts so they can add them to their caches.
- Are independent and don’t affect each other.
- Can partition the load into different peers by "type" of data/work
- Construct a value for a database at a particular moment in time

Typically, a production setup involves dynamically sharding data across peers.

## Transactors

Handle writes and retracts. They take care of syncing data to Peers. All that ACID stuff, right here.

- Standalone process, "the database"
- Handle tx and indexing, not queries and storage
- During hot failover, 2nd tx takes over (in a few seconds for pro); however queries continue to work
- isolation level: serialized
- Transactors are typically 1-2, run in serial sync, not parallel
- Basically a coordinator, all writes must go
- as if a "single thread of execution", full serializabilty, writes to a log (write-ahead log, durability)
- Transactor handles indexing; Indexes are generated so peers can use it
- Transactor owns the root index; indexes are global
- Careful with error vs timeout
- Can you pass around the 't' (in a cookie)? (Find out)
- Opaque binary blobs
- memcache--no invalidation
- ddb - 10-20ms vs ddb+memcache = 2ms

Typically transactor is deployed at the same location as peers, preferably close to the storage service

## Storage engines

Data storage is done in Fressian format (edn is serialized).

It works best with distributed storage engines.

### AWS


### Heroku Postgres


# Additional Notes

Make data immutable, control *change*, understand it.

How? Queues and Messages.

Messages are data, Clojure maps.

In a Pedestal application, application state is stored in a tree. This tree is the application's **information model**.  The value of the model is immutable, so change takes the form of state transitions which are implemented as pure functions. The functions which perform these state transitions are run each time a new transform message is delivered on the input queue.

A **transform message** is any message which describes a change that the receiver should make. Transform messages will usually have a target, an operation to perform and some arguments.

Peers = app processes.

What is datamic db?
> references to index roots

Datomic's notion of time: when was this fact learned? Which means your notion of time may be different.

`Seek-datoms` - a lazy, raw access of datoms

An indexing job purges data from memory and creates a new tree that includes datums from the last indexing job.

# References

## Books

## Articles & Blogs

- [Architecture of Datomic](http://www.infoq.com/articles/Architecture-Datomic)
- [Overview of Datomic](http://www.slideshare.net/StampedeCon/datomic-a-modern-database-stampedecon-2014)
- [Official Blog](http://blog.datomic.com/?view=flipcard)

## Tutorials

- [Datomic Walkthrough](http://www.danneu.com/posts/authordb-datomic-tutorial/)
- [Day of Datomic](https://github.com/Datomic/day-of-datomic)
- [Interactive Datalog Tutorials](http://www.learndatalogtoday.org)
- [Simple Blog example](https://gist.github.com/a2ndrade/5651419)

## Videos

- [Introduction to Datomic, by Rich Hickey](http://docs.datomic.com/tutorial_video.html#!prettyPhoto/0/)
- [Datalog Queries](https://www.youtube.com/watch?v=bAilFQdaiHk)
- [Datomic Up & Running](https://www.youtube.com/watch?v=ao7xEwCjrWQ)
