(ns atomic.db-test
  (:require [clojure.test :refer :all]
            [atomic.db :refer :all]))

(deftest test-db
  (testing "setup"
    (let [res (setup :mem "test")]
      (is (contains? res "datomic:mem://test"))))
  (testing "playground"
    (let [res (playground)]
      (is (contains? res "datomic:mem://"))))
  (testing "cleanup!"
    (let [data (delete! :mem "test")]
      (is (contains? data :done)))))
