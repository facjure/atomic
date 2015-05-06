Transactions
============

Datomic transactions are ACID: Atomic, Consistent, Isolated, and Durable.

If you're performing CRUD operations, there is no `U` & `D` in Dataomic, only
Create and Read. Update is considered as 'Retract'ing of an existing attribute.

Transaction requests are data structures. Each operation to the db is
represented as a tuple of four things:

    [op entity-id attribute value]
    ;; op is one of :db/add and :db/retract.

Sample:

    {:db/id entity-id
     attribute value
     attribute value
     ... }

Create - `db:add`

To add data to an existing entity, build a transaction using :db/add implicitly
with the map structure (or explicitly with the list structure). The following
example shows creating data using a transaction:

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

Notes:

- Every tx is yet another datum
- `transact` takes a vector of vectors: [Op, Ent, Attr, Val]; consider it a
  vector of assertions
- `transact` can be sync or async; both return a promise
- Sync transactions have an implicit timeout
- Single value vs multi-valued attribute => db cardinality
- Within a tx, a tempid (-100) denotes the same "new" entity; scope of a 
  tempid is a single tx
- Tx itself is an entity!
- Tuples within a tx is not ordered
- A single tx can and will contain multiple attribute changes
