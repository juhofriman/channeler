(ns channeler.server
  (:gen-class)
  (:require [org.httpkit.server :as httpkit]
            [channeler.handler :as handler]))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server
  ([listen-port]
   (start-server listen-port {}))
  ([listen-port routes]
   ;; The #' is useful when you want to hot-reload code
   ;; You may want to take a look: https://github.com/clojure/tools.namespace
   ;; and http://http-kit.org/migration.html#reload
   (do
     (println "Channeler starting: localhost:" listen-port)
     (println routes)
     (doseq [route routes] (apply handler/register-route! route))
     (reset! server (httpkit/run-server #'handler/proxy-handler {:port listen-port})))))
