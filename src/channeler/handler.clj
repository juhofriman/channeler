(ns channeler.handler
  (:gen-class)
  (:require [org.httpkit.server :as httpkit]
            [org.httpkit.client :as httpclient]))

(def empty-state {:routes {} :port nil})

(def routes (atom empty-state))

(defn register-route!
  [uri target]
  (swap! routes #(assoc-in % [:routes uri] target)))

(defn reset-routes! [] (reset! routes empty-state))

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

(defn proxy-url
  ([routespec uri]
   (proxy-url routespec uri nil))
  ([routespec uri query-string]
   (if-let [uri-key (select-matching-route-key (keys routespec) uri)] 
     {:url (build-url :http (subs uri (count uri-key)) (get routespec uri-key) query-string)
      :headers (or (get-in routespec [uri-key :headers]))})))

(defn process-proxy
  [{:keys [status headers body error]}]
  (if error
    {:status 404
     :headers {"content-type" "text/plain"}
     :body (str "Error: " error)}
    {:status status
     :headers (reduce-kv #(assoc %1 (name %2) %3) {} headers)
     :body body}))

(defn proxy-handler [{uri :uri query-string :query-string method :request-method :as req}]
  
  (if-let [route (proxy-url (:routes @routes) uri query-string)]
    (do
      (println (str method " " uri ": Proxying "  route))
      (case method
        :get @(httpclient/get (:url route) {:headers (:headers route)}  process-proxy)
        :post @(httpclient/post (:url route) {:headers (:headers route)}  process-proxy)))
    (do
      (println "No such route: " uri)
      {:status 404
       :headers {"content-type" "text/plain"}
       :body (str "No such route " uri)})))
