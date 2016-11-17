(ns channeler.handler
  (:gen-class)
  (:require [org.httpkit.server :as httpkit]
            [org.httpkit.client :as httpclient]))

(def routes (atom {}))

(defn register-route!
  [uri target]
  (swap! routes #(assoc % uri target)))

(defn reset-routes! [] (reset! routes {}))

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

(defn app [{uri :uri query-string :query-string :as req}]
  
  (if-let [route (proxy-url @routes uri query-string)]
    (do
      (println (str uri ": Proxying "  route))
      @(httpclient/get (:url route) {:headers (:headers route)}  process-proxy))
    {:status 404
     :headers {"content-type" "text/plain"}
     :body (str "No such route " uri)}))



(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server []
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (reset! server (httpkit/run-server #'app {:port 1234})))
