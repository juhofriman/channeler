(ns channeler.core
  (:gen-class)
  (:require [channeler.server :refer :all]
            [clojure.tools.cli :as cli]))


(defn read-config
  [f]
  (try
    (do (let [config (read-string (slurp f))]
          [(or (:port config) 1245) (or  (:routes config {}))]))
    (catch Exception e [1234 {}])))

(defn exec [{[pro _] :arguments}]
  (case pro
    "start" (apply start-server (read-config ".channeler.edn"))))

(defn -main
  [& args]
  (exec (cli/parse-opts args {})))


