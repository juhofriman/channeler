(ns channeler.core-test
  (:require [clojure.test :refer :all]
            [channeler.core :refer :all]))

(deftest core-tests
  (testing "getting configuration"
    (let [[port routes] (read-config "something-that-does-not-exist.xxxx")]
      (is (some? port))
      (is (empty? routes)))))
