Rules
=====

Rules are analagous to functions. Instead of having forms, rules simply enclose
a bunch of constraints (datalog). Abstracting reusable parts of queries into
rules, enable query composition.

Consider this:

    [?p :person/name ?name]
    [?a :article/body ?p]
    [?a :article/title ?title]

Changed into a rule:

    [(blog-post ?name ?title)
     [?p :person/name ?name]
     [?a :article/body ?p]
     [?a :article/title ?title]]

Rule composition is powerful. Datalog rules can even implement graph traversal
algorithms.
