(ns noir.util.gae
  "Functions to help run noir on Google App Engine."
  (:use compojure.core
        [ring.middleware params
                         keyword-params
                         nested-params
                         cookies
                         session])
  (:require [noir.server.handler :as handler]
            [noir.core :as noir]
            [noir.content.defaults :as defaults]
            [noir.cookies :as cookie]
            [noir.exception :as exception]
            [noir.statuses :as statuses]
            [noir.options :as options]
            [noir.session :as session]
            [noir.validation :as validation]))


(defn gae-handler
  "Create a Google AppEngine friendly handler for Noir. Use this instead
  of server/gen-handler for AppEngine projects."
  [opts]
  (-> (handler/base-handler opts)
    (wrap-keyword-params)
    (wrap-nested-params)
    (wrap-params)
    (handler/wrap-noir-middleware opts)))
