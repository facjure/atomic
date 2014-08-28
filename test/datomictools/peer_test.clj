(ns datomictools.peer-test
  (:require [clojure.test :refer :all]
            [datomictools.peer :refer :all]))

(deftest test-peer
  (testing "setup"
    (let [res (setup :mem "test")]
      (is (contains? res "datomic:mem://test"))))
  (testing "playground"
    (let [res (scratch-conn)]
      (is (contains? res "datomic:mem://"))))
  (testing "cleanup!"
    (let [data (cleanup! :mem "test")]
      (is (contains? data :done)))))
