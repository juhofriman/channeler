(ns user
  (:require [channeler.handler :as handler]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn start-server
  []
  (handler/start-server))

(defn reload []
  (handler/stop-server)
  (refresh :after 'user/start-server))
