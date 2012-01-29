(ns noir.util.gae
  "Functions to help run noir on Google App Engine."
  (:use [ring.middleware params
                         keyword-params
                         nested-params])
  (:require [noir.server.handler :as handler]))


(defn gae-handler 
  "Create a Google AppEngine friendly handler for Noir. Use this instead
  of server/gen-handler for AppEngine projects."
  [opts]
  (-> (handler/base-handler opts)
    (wrap-keyword-params)
    (wrap-nested-params)
    (wrap-params)
    (handler/wrap-noir-middleware opts)
    (handler/wrap-spec-routes opts)))
