Queries
=======

## Overview

Query is data. 

Queries and query results are represented as simple collection data structures
that accept a find clause, where conditions, and optional attribute values. The
syntax looks like this: find, in, and where 'data patterns':

Example: find all movie titles in the year 1987.

    [:find ?title
     :where 
        [?e :movie/year 1987]
        [?e :movie/title ?title]]

Variables are symbols: strings, numbers keywords, literals, and `#tags`

Datomic has implicit joins. You don't have to apply markers for join attributes:
mention them in the next tuple.

    [:find ?attr
     :where
        [?p :person/name]
        [?p ?a]
        [?a :db/ident ?attr]]

In the query above, checking two entity ids (`?p`) in consecutive constraints
fires off an implicit join.

The order of the data patterns does not matter.

Datomic doesn't optimize query for the size of datasets. This strategy can be
changed in code. Data is stored in chunks in a tree in storage and datalog
retrieves chunks from the storage.

## Parameterized Queries

With parameterized queries, you can pass scalars (basic types like String,
Integer, etc.,), Tuples, Collections, and Relations as 'input' to queries.

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

    (d/q 
      '[:find ?prefix
       :in [?word ...]
       :where 
          [(subs ?word 0 5) ?prefix]]
          ["hello" "clojurians"])

## Constraints

- speculative, compare-set, transactor fn (macros!)->userful for atomic operations
- transactor fns are like macros for transactors.
- you can send arbitrary fn to transactors and let it run on peers.

## Dataflow

Five types of functions: transform, emit, derive, continue and effect. 

Transform and derive functions produce values which change the state of part of
the information model. Derive and emit functions are called when parts of the
information model, which are inputs to these functions, are changed. All of the
dependencies between functions and the information model are described in a data
structure.

Each is a pure function.

## Caching

Datomic caches only immutable data, so all caches are valid forever.

