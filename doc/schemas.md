Schemas
=======

Datomic Schemas are _immutable_, and are represented as a map with three required attributes:

	:db/ident, :db/valueType, :db/cardinality

Let's look at an example:

    {:db/id #db/id[:db.part/db ]
     :db/ident :artist/gid
     :db/valueType :db.type/uuid
     :db/cardinality :db.cardinality/one
     :db/unique :db.unique/identity
     :db/index true
     :db/doc "The globally unique MusicBrainz ID for an artist"
     :db.install/_attribute :db.part/db}


- `#db/id` is a temp-id
- db.unique is enforced by the transactor
- valueType is a generic

Attributes are themselves _Entities_ with associated attributes.

Note that entity ids will change—don't rely on them; use `db.unique/identity`
 
## Indexes

Indexes are the magic behind Datomic's performance. They're a sorted set of
datums, and every datom is stored in two or more indexes:

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

Indexes are stored in chunks, organized into trees. The leaf where the chunk is
stored is a [fressian](https://github.com/Datomic/fressian) encoded data. Every
segment has a guuid whose value is a zipped data (chunk value).
