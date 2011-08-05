(ns noir.session
  "Stateful session handling functions. Uses a memory-store by
  default, but can use a custom store by supplying a :session-store
  option to server/start. All keys are stored as strings."
  (:refer-clojure :exclude [get remove])
  (:use ring.middleware.session
        ring.middleware.session.memory)
  (:require [noir.options :as options]))

(declare *noir-session*)
(defonce mem (atom {}))

(defn put! 
  "Associates the key with the given value in the session"
  [k v]
  (swap! *noir-session* assoc (name k) v))

(defn get 
  "Get the key's value from the session, returns nil if it doesn't exist."
  ([k] (get k nil))
  ([k default]
    (clojure.core/get @*noir-session* (name k) default)))

(defn clear! 
  "Remove all data from the session and start over cleanly."
  []
  (reset! *noir-session* {}))

(defn remove!
  "Remove a key from the session"
  [k]
  (swap! *noir-session* dissoc (name k)))

(defn flash-put!
  "Store a value with a lifetime of one retrieval (on the first flash-get,
  it is removed). This is often used for passing small messages to pages
  after a redirect."
  [v]
  (put! :_flash v))

(defn flash-get
  "Retrieve the flash stored value. This will remove the flash from the
  session."
  []
  (let [flash (get :_flash)]
    (remove! :_flash)
    flash))

(defn noir-session [handler]
  (fn [request]
    (binding [*noir-session* (atom (:session request))]
      (when-let [resp (handler request)]
        (assoc resp :session @*noir-session*)))))

(defn assoc-if [m k v]
  (if (not (nil? v))
    (assoc m k v)
    m))

(defn wrap-noir-session [handler]
  (-> handler
    (noir-session)
    (wrap-session
     (assoc-if {:store (options/get :session-store (memory-store mem))}
               :cookie-attrs (options/get :session-cookie-attrs)))))
