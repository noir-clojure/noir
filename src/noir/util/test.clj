(ns noir.util.test
  (:use clojure.test)
  (:require [noir.server :as server]
            [noir.session :as session]
            [noir.cookies :as cookies]
            [noir.options :as options]))

(def content-types {:json "application/json"
                    :html "text/html"})

(defmacro with-noir [& body]
  `(binding [options/*options* options/default-opts
             session/*noir-session* (atom {})
             cookies/*new-cookies* (atom {})
             cookies/*cur-cookies* (atom {})]
     ~@body))

(defn has-content-type [resp ct]
  (is (= ct (get-in resp [:headers "Content-Type"])))
  resp)
    
(defn has-status [resp stat]
  (is (= stat (get resp :status)))
  resp)

(defn has-body [resp cont]
  (is (= cont (get resp :body)))
  resp)

(defn make-request [route & [params]]
  (let [[method uri] (if (vector? route)
                       route
                       [:get route])]
    {:uri uri :request-method method :params params}))

(defn send-request [route & [params]]
  (let [handler (server/gen-handler)]
    (handler (make-request route params))))
    





