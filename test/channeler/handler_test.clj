(ns channeler.handler-test
  (:require [clojure.test :refer :all]
            [channeler.handler :refer :all]))


(deftest selecting-matching-route-key
  (testing "selecting matching route key"
    (is (nil? (select-matching-route-key ["/foo"] "/")))
    (is (= "/" (select-matching-route-key ["/" "/foo"] "/")))
    (is (= "/" (select-matching-route-key ["/" "/foo"] "/baz")))
    (is (= "/foo" (select-matching-route-key ["/" "/foo" "/baz"] "/foo")))
    (is (= "/foo" (select-matching-route-key ["/" "/foo" "/baz"] "/foo/bar")))))

(deftest route-extraction
  (testing "getting proxied urls from routespec"
    (is (nil? (proxy-url {} "/")))
    (is (= "http://localhost:1234/"
           (:url (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/"))))
    (is (= "http://localhost:1234/foobarbaz"
           (:url (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/foobarbaz"))))
    (is (= "http://localhost:1234/bar"
           (:url (proxy-url {"/foo" {:host "localhost" :port 1234 :path "*"}} "/foo/bar")))))
  (testing "passing GET parameters"
    (is (= "http://localhost:1234/foo?a=1&b=2"
           (:url (proxy-url {"/" {:host "localhost" :port 1234 :path "*"}} "/foo" "a=1&b=2"))))))

(deftest building-url
  (testing "urls build correctly"
    (is (= "http://www.wadafa.fi:8888/my-route?q=1&a=2" (build-url :http  "/my-route" {:host "www.wadafa.fi" :port 8888} "q=1&a=2")))
    (is (= "https://www.wadafa.fi:8888/my-route" (build-url :https "/my-route" {:host "www.wadafa.fi" :port 8888} nil)))))

(deftest adding-headers
  (testing "adding headers"
    (is (= {"X-test" "testing"}
           (:headers (proxy-url {"/" {:host "localhost" :port 1234 :headers {"X-test" "testing"}}} "/"))))))
