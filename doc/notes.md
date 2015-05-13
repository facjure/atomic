Notes
=====

Datomic is information oriented and blobs are opaque to it.

Make data immutable, control *change*, understand it.

How? Queues and Messages.

Messages are data, Clojure maps.

Peers = app processes.

What is datamic db?
> references to index roots

Datomic's notion of time: when was this fact learned? Which means your notion of
time may be different.

`Seek-datoms` - a lazy, raw access of datoms

An indexing job purges data from memory and creates a new tree that includes
datums from the last indexing job.

Since data is always a snapshot in time (with the same value), you can ask
questions like _what is the current premium?_ or _what happened to the user as of
last year_ from a local repository.

By using a richer api for reads directly from clojure and its data structures,
and delegating storage to distributed engines like Couchbase, Dynamodb, Riak,
and others, Datomic further virtualizes cloud storage providers.

Additional notes:

- Careful with error vs timeout
- Can you pass around the 't' (in a cookie)? (Find out)
- Opaque binary blobs
- memcache--no invalidation

Typically, a transactor is deployed at the same location as peers, preferably
close to the storage service. Transactors can be 1-2, run in serial sync, not in
parallel. During hot failover the 2nd transactor takes over (in a few seconds
for pro); however queries continue to work.

A Transactor also handles indexing, and owns the root index; indexes are global.

Indexes are generated so peers can use it.

Peers can partition the load into different peers by "type" of
data/work. A production setup, typically, involves dynamically sharding data
across peers
