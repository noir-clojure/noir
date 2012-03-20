(ns noir.util.test
  "A set of utilities for testing a Noir project"
  (:use clojure.test)
  (:require [noir.server :as server]
            [noir.session :as session]
            [noir.validation :as vali]
            [noir.cookies :as cookies]
            [noir.options :as options]))

(def content-types {:json "application/json; charset=utf-8"
                    :html "text/html"})

(defmacro with-noir
  "Executes the body within the context of Noir's bindings"
  [& body]
  `(binding [options/*options* options/default-opts
             vali/*errors* (atom {})
             session/*noir-session* (atom {})
             session/*noir-flash* (atom {})
             cookies/*new-cookies* (atom {})
             cookies/*cur-cookies* (atom {})]
     ~@body))

(defn has-content-type
  "Asserts that the response has the given content type"
  [resp ct]
  (is (= ct (get-in resp [:headers "Content-Type"])))
  resp)

(defn has-status
  "Asserts that the response has the given status"
  [resp stat]
  (is (= stat (get resp :status)))
  resp)

(defn has-body
  "Asserts that the response has the given body"
  [resp cont]
  (is (= cont (get resp :body)))
  resp)

(defn- make-request [route & [params]]
  (let [[method uri] (if (vector? route)
                       route
                       [:get route])]
    {:uri uri :request-method method :params params}))

(defn send-request
  "Send a request to the Noir handler. Unlike with-noir, this will run
  the request within the context of all middleware."
  [route & [params]]
  (let [handler (server/gen-handler options/*options*)]
    (handler (make-request route params))))

(defn send-request-map
  "Send a ring-request map to the noir handler."
  [ring-req]
  (let [handler (server/gen-handler options/*options*)]
    (handler ring-req)))
