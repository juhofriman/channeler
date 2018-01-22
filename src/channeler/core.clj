(ns channeler.core
  (:gen-class)
  (:require [channeler.server :refer :all]
            [clojure.tools.cli :as cli]))


(defn read-config
  [port-override file]
  (try
    (do (let [config (read-string (slurp file))]
          [(or port-override (:port config) 1234) (or  (:routes config {}))]))
    (catch Exception e [(or port-override 1234) {}])))

(defn print-usage
  [summary]
  (do
    (println "channeler COMMAND")
    (println)
    (println summary)))

(defn exec [{[pro _] :arguments {:keys [port]} :options summary :summary}]
  (case pro
    "run" (apply start-server (read-config port ".channeler.edn"))
    "help" (print-usage summary)
    (println "Unkown action:" pro)))


(defn -main
  [& args]
  (exec (cli/parse-opts args
          [[nil "--port PORT" "Port number (overrides :port in .channeler.edn)"
            :id :port
            :parse-fn #(Integer/parseInt %)]])))
