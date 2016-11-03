(ns channeler.core
  (:gen-class)
  (:require [channeler.handler :refer :all]))

(defn -main
  [& args]
  (start-server))


