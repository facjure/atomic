Datomic Concepts
================

Datomic considers a database to be an Information system.

Its architecture can be broken down into three categories:

- Create Facts: Identity, Entities, Attributes, Values, Transactions
- Find Facts: Query, DbFunctions, and Rules
- Manage Facts: Peers, Transactors, Storage Engines

This is of course a simplification. There are many fine-grained
[concepts](http://docs.datomic.com/), but it's a start.

Information is stored as a collection of events as **Facts**. Things that have
happened. Each fact describes what happened at what _time_. Once stored in
storage facts don't change.

Naturally, Datomic supports only two operations: ADD and RETRACT via
transactions.

Old facts can be superseded by new facts over time and the state of the database
is a _value_ defined by the set of facts at a given moment. As a result
Datomic’s data model—based on immutable facts stored over time, enables a
physical design different from most sql or nosql databases: instead of
processing all requests in a single server, Datomic distributes read and write
and query processing across different components, separating concerns
commonly treated as the same in most databases.

*Queries** are handled by an extended form of Datalog, a declarative,
 logic-based, and embeddable language in your application process. **Rules** are
 analagous to normal functions. Instead of having forms, rules simply enclose
 constraints (datalog). Abstracting reusable parts of queries into rules, enable
 query composition.

*Peers* handle queries and data storage. They are independent of each other.
Applications with Datomic code run in Peer(s). They handle queries, caching, and
hide the details of synchronizing updates from other peers. Peers query and
access data locally using a database value and cache data extensively using LRU
cache, representing a partial copy of all the facts. This constructs a value for
a database at a particular moment in time.

*Transactors* are a standalone processes that handles transactions and indexing with
_writes and retracts_. They are responsible for synchronizing data to Peers and
implementing ACID transactions. Implemented in a "single thread of execution",
with full serializabilty (isolation level: serialized), they write to a
write-ahead log for durability. Like a coordinator: all writes must go.

Peers read Facts from **Storage Services**, distributed engines like Couchbase,
Dynamodb, Riak, and others. Peers write new facts by asking the Transactor to
add them to the Storage Service, get notified by the Transactor about new facts,
and add them to their caches.

However, Datomic is a [CP](http://en.wikipedia.org/wiki/CAP_theorem) system,
trades-off unlimited write scalability, in-built constraints, in favor of a
flexibile data and query modeling abstraction (Datalog), with a fine
interesction of sound database and functional programming principles like ACID
transactions and Imutability.
