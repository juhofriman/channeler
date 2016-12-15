(ns channeler.handler
  (:gen-class)
  (:require [org.httpkit.server :as httpkit]
            [org.httpkit.client :as httpclient]))

(def routes (atom {}))

(defn register-route!
  [uri target]
  (swap! routes #(assoc-in % [:routes uri] target)))

(defn reset-routes! [] (reset! routes {}))

(defn select-matching-route-key
  "Selects matching route key from collection."
  [all-route-keys matched-key]
  (reduce #(if (.startsWith matched-key %2) %2 %1) nil all-route-keys))

(defn query-string
  "Generated query string canonical representation"
  [qs]
  (if qs (str "?" qs) ""))

(defn build-url
  [scheme uri {:keys [host port] :as spec} qs]
  (str (name scheme) "://" host ":" port (if (.startsWith uri "/") "" "/")  uri (query-string qs)))

(defn resolve-route
  ([routespec uri]
   (resolve-route routespec uri nil))
  ([routespec uri query-string]
   (if-let [uri-key (select-matching-route-key (keys routespec) uri)] 
     {:url (build-url :http (subs uri (count uri-key)) (get routespec uri-key) query-string)
      :headers (or (get-in routespec [uri-key :headers]) {})
      :outbound-headers (or (get-in routespec [uri-key :outbound-headers]) {})})))

(defn map-response
  ([response]
   (map-response {} response))
  ([outbound-headers {:keys [status headers body error]}]
   (if error
     {:status 500
      :headers {"content-type" "text/plain"}
      :body (str "Error proxying: " error)}
     {:status status
      :headers (merge (reduce-kv #(assoc %1 (name %2) %3) {} headers) outbound-headers)
      :body body})))

(defn no-such-route
  [uri]
  (let [message (str "No such route: " uri)]
    (println message)
    {:status 404
     :headers {"content-type" "text/plain"}
     :body message}))

(defn client-fn [method]
  (case method
    :get httpclient/get
    :post httpclient/post
    ; default shouldn't occur because httpkit drops illeagal methods
    (throw (Exception. (str "Unknown method " method)))))

(defn proxy-handler [{uri :uri query-string :query-string method :request-method :as req}]
  (if-let [{:keys [url headers outbound-headers]} (resolve-route (:routes @routes) uri query-string)]
    (do 
	(println "Querying: " url)
    	@((client-fn method) url {:headers headers} (partial map-response outbound-headers)))
    (no-such-route uri)))
