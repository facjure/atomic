;; see http://stackoverflow.com/questions/14724991/modelling-multiple-many-to-many-relationships-in-datomic

(require '[datomicd
           .api :as d])

(def uri "datomic:mem://test")
(d/create-database uri)
(def conn (d/connect uri))

(d/transact conn [ ;; User
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :user/username
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/unique :db.unique/value
                  :db/index true
                  :db/doc "This user's username"
                  :db.install/_attribute :db.part/db}
                 ;; Category
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :category/name
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "This category's name"
                  :db.install/_attribute :db.part/db}
                 ;; Article
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :article/title
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/fulltext true
                  :db/doc "This article's title"
                  :db.install/_attribute :db.part/db}
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :article/author
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc "This article's author"
                  :db.install/_attribute :db.part/db}
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :article/category
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many
                  :db/doc "This article's categories"
                  :db.install/_attribute :db.part/db}
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :article/body
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/fulltext true
                  :db/doc "This article's body"
                  :db.install/_attribute :db.part/db}
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :article/comments
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/many
                  :db/isComponent true
                  :db/doc "This article's comments"
                  :db.install/_attribute :db.part/db}
                 ;; Comment
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :comment/author
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc "This comment's author"
                  :db.install/_attribute :db.part/db}
                 {:db/id (d/tempid :db.part/db)
                  :db/ident :comment/body
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/fulltext true
                  :db/doc "This comment's body"
                  :db.install/_attribute :db.part/db}])


(d/transact conn
            [;; A user writing an article
             {:db/id #db/id [:db.part/user -100]
              :user/username "john.smith"}
             {:db/id #db/id [:db.part/user -200]
              :category/name "Functional Programming"}
             {:db/id #db/id [:db.part/user -300]
              :article/title "Monads in Pictures"
              :article/author #db/id [:db.part/user -100]
              :article/category #db/id [:db.part/user -200]
              :article/body "http://bit.ly/13lW7WF"}
             ;; A user posting a comment
             {:db/id #db/id [:db.part/user -101]
              :user/username "kate.nash"}
             {:db/id #db/id [:db.part/user -400]
              :comment/author #db/id [:db.part/user -101]
              :comment/body "Great article!"
              :article/_comments #db/id [:db.part/user -300]}
             ;; Another user writing an article
             {:db/id #db/id [:db.part/user -102]
              :user/username "alex.hill"}
             {:db/id #db/id [:db.part/user -201]
              :category/name "Clojure News"}
             {:db/id #db/id [:db.part/user -301]
              :article/title "Clojure Conj DC 2013"
              :article/author #db/id [:db.part/user -102]
              :article/category #db/id [:db.part/user -201]
              :article/body "See http://clojure-conj.org/"}
             ;; Two users posting comments
             {:db/id #db/id [:db.part/user -103]
              :user/username "scott.carter"}
             {:db/id #db/id [:db.part/user -401]
              :comment/author #db/id [:db.part/user -103]
              :comment/body "Looking forward to it"
              :article/_comments #db/id [:db.part/user -301]}
             {:db/id #db/id [:db.part/user -402]
              :comment/author #db/id [:db.part/user -102]
              :comment/body "Me too!"
              :article/_comments #db/id [:db.part/user -301]}])

;; Find user categories
(d/q '[:find ?cid ?c
       :in $ ?u
       :where
       [?uid :user/username ?u]
       [?aid :article/category ?cid]
       [?aid :article/author ?uid]
       [?cid :category/name ?c]]
     (d/db conn) "john.smith")
;; > #{[17592186045419 "Functional Programming"]}

;; Find user articles
(d/q '[:find ?aid ?a
       :in $ ?u
       :where
       [?uid :user/username ?u]
       [?aid :article/author ?uid]
       [?aid :article/title ?a]]
     (d/db conn) "alex.hill")
;; > #{[17592186045425 "Clojure Conj DC 2013"]}

;; Find users for category x
(d/q '[:find ?uid ?u
       :in $ ?c
       :where
       [?aid :article/category ?cid]
       [?cid :category/name ?c]
       [?aid :article/author ?uid]
       [?uid :user/username ?u]]
     (d/db conn) "Clojure News")
;; > #{[17592186045423 "alex.hill"]}

;; Find articles for category x
(d/q '[:find ?aid ?a
       :in $ ?c
       :where
       [?aid :article/category ?cid]
       [?cid :category/name ?c]
       [?aid :article/title ?a]]
     (d/db conn) "Functional Programming")
;; > #{[17592186045420 "Monads in Pictures"]}

; Find articles and their comments
(d/q '[:find ?aid ?a ?coid
       :where
       [?aid :article/comments ?coid]
       [?aid :article/title ?a]]
     (d/db conn))
;; > #{["Monads in Pictures" "Great article!"] ["Clojure Conj DC 2013" "Looking forward to it"] ["Clojure Conj DC 2013" "Me too!"]}
