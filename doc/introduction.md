Introduction
============

> Store a collection of facts
  that do not change, add
  new facts like memory
  superseding old facts
  build a snapshot
  of facts
  that collect knowledge over time
  and reveal meaning

Datomic considers a database to be an information system, where information is a
set of facts, and facts are things that have happened. Datomic is a
[CP](http://en.wikipedia.org/wiki/CAP_theorem) system that stores a collection
of Facts as events, and each event describes what happened at what _time_. Once
stored in memory facts _don't_ change.

Naturally, Datomic supports only two operations: _add_ and _retract_ facts.

Old facts can be superseded by new facts over time and the state of the database
is a value defined by the set of facts _in effect_ at a given moment in time. As
a result Datomic’s data model—based on immutable facts stored over time, enables
a physical design that is different from most sql or nosql databases: instead of
processing all requests in a single server, Datomic distributes
_read_, _write_ and _query processing_ across different components, and swaps
storage engines. This separation is achieved by creating distinct apis for each
concern with the notion of local and remote snapshots of data. Since data is always a
snapshot in time (with the same value), you can ask questions like
_what is the current policy?_ or _what happened to the user as of last year_
from a local repository.

By using a richer api for reads directly from clojure and its data structures,
and delegating storage to distributed engines like Couchbase, Dynamodb, Riak,
and others, Datomic further virtualizes cloud storage providers.
