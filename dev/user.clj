(ns user
  (:require [channeler.server :as server]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn start-server
  []
  (server/start-server))

(defn reload []
  (server/stop-server)
  (refresh :after 'user/start-server))
