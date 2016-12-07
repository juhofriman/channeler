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

; this is not matching uri, but matching route key in routespec
(defn matching-uri
  [uris uri]
  (reduce #(if (.startsWith uri %2) %2 %1) nil uris))

(defn build-url
  [uri {:keys [host port] :as spec} query-string]
  (let [qs (if query-string (str "?" query-string) "")]
    (if (.startsWith uri "/")
      (str "http://" host ":" port uri qs)
      (str "http://" host ":" port "/" uri qs))))

(defn proxy-url
  ([routespec uri]
   (proxy-url routespec uri nil))
  ([routespec uri query-string]
   (if-let [uri-key (matching-uri (keys routespec) uri)] 
     {:url (build-url (subs uri (count uri-key)) (get routespec uri-key) query-string)
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

(defn app [{uri :uri query-string :query-string method :request-method :as req}]
  
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



(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server
  ([]
   (start-server 1234))
  ([listen]
   (start-server listen empty-state))
  ([listen r]
   ;; The #' is useful when you want to hot-reload code
   ;; You may want to take a look: https://github.com/clojure/tools.namespace
   ;; and http://http-kit.org/migration.html#reload
   (do
     (println "Channeler starting: localhost:" (or listen 1234))
     (println r)
     (swap! routes (fn [old-state] (assoc old-state :routes (or r empty-state))))
     (reset! server (httpkit/run-server #'app {:port (or listen 1234)})))))
