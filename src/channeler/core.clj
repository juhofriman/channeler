(ns channeler.core
  (:gen-class)
  (:require [channeler.handler :refer :all]
            [clojure.tools.cli :as cli]))

(def options {})

(defn get-config 
  []
  (try
    (do (let [config (read-string (slurp ".channeler.edn"))]
          [(or (:port config) 1245) (or  (:routes config {}))]))
    (catch Exception e [1234 {}])))

(defn exec [{[pro _] :arguments}]
  (case pro
    "start" (apply start-server (get-config))))

(defn -main
  [& args]
  (exec (cli/parse-opts args options)))


