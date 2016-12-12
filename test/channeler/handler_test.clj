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

(deftest building-url
  (testing "urls build correctly"
    (is (= "http://www.wadafa.fi:8888/my-route?q=1&a=2" (build-url :http  "/my-route" {:host "www.wadafa.fi" :port 8888} "q=1&a=2")))
    (is (= "https://www.wadafa.fi:8888/my-route" (build-url :https "/my-route" {:host "www.wadafa.fi" :port 8888} nil)))))

(deftest route-extraction
  (testing "getting proxied urls from routespec"
    (is (nil? (resolve-route {} "/")))
    (is (= "http://localhost:1234/"
           (:url (resolve-route {"/" {:host "localhost" :port 1234 :path "*"}} "/"))))
    (is (= "http://localhost:1234/foobarbaz"
           (:url (resolve-route {"/" {:host "localhost" :port 1234 :path "*"}} "/foobarbaz"))))
    (is (= "http://localhost:1234/bar"
           (:url (resolve-route {"/foo" {:host "localhost" :port 1234 :path "*"}} "/foo/bar")))))
  (testing "passing GET parameters"
    (is (= "http://localhost:1234/foo?a=1&b=2"
           (:url (resolve-route {"/" {:host "localhost" :port 1234 :path "*"}} "/foo" "a=1&b=2"))))))

(deftest adding-inbound-headers
  (testing "adding headers"
    (is (= {"X-test" "testing"}
           (:headers (resolve-route {"/" {:host "localhost" :port 1234 :headers {"X-test" "testing"}}} "/"))))))

(deftest adding-outbound-headers
  (testing "adding headers"
    (is (= {"X-test" "testing"}
           (:outbound-headers (resolve-route {"/" {:host "localhost" :port 1234 :outbound-headers {"X-test" "testing"}}} "/"))))))

(deftest mapping-responses
  (testing "mapping status codes"
    (is (= 500
           (:status (map-response {:error "Mighty error"}))))
    (is (= 418
           (:status (map-response {:status 418})))))
  (testing "mapping headers"
    (is (= {"X-test" "testing"}
           (:headers (map-response {:status 200 :headers {"X-test" "testing"}}))))
    (is (= {"X-test" "outbound"}
           (:headers (map-response {"X-test" "outbound"} {:status 200 :headers {}}))))))
