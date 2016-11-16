(ns channeler.handler-test
  (:require [clojure.test :refer :all]
            [channeler.handler :refer :all]))


(deftest get-matching-uri
  (testing "testing getting matching uri"
    (is (= "/" (matching-uri ["/" "/foo"] "/")))
    (is (= "/" (matching-uri ["/" "/foo"] "/baz")))
    (is (= "/foo" (matching-uri ["/" "/foo" "/baz"] "/foo")))
    (is (= "/foo" (matching-uri ["/" "/foo" "/baz"] "/foo/bar")))))

(deftest route-extraction
  (testing "getting proxied urls from routespec"
    (is (nil? (proxy-url {} "/")))
    (is (= "http://localhost:1234/"
           (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/")))
    (is (= "http://localhost:1234/foobarbaz"
           (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/foobarbaz")))
    (is (= "http://localhost:1234/bar"
           (proxy-url {"/foo" {:host "localhost" :port 1234 :path "*"}} "/foo/bar"))))
  (testing "passing GET parameters"
    (is (= "http://localhost:1234/foo?a=1&b=2"
           (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/foo" "a=1&b=2")))))


