(ns noir.server
  (:gen-class)
  (:use ring.adapter.jetty)
  (:require [noir.core :as noir]))

(defn start [port]
  (noir/init-routes)
  (run-jetty (var noir/final-routes) {:port port}))
