Tips & Tricks
=============

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

