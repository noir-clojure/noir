(ns noir.session
  "Stateful session handling functions. Uses a memory-store by
  default, but can use a custom store by supplying a :session-store
  option to server/start. All keys are stored as strings."
  (:refer-clojure :exclude [get remove swap!])
  (:use ring.middleware.session
        ring.middleware.session.memory)
  (:require [noir.options :as options]))

;; ## Session

(declare ^:dynamic *noir-session*)
(defonce mem (atom {}))

(defn put! 
  "Associates the key with the given value in the session"
  [k v]
  (clojure.core/swap! *noir-session* assoc (name k) v))

(defn get 
  "Get the key's value from the session, returns nil if it doesn't exist."
  ([k] (get k nil))
  ([k default]
    (clojure.core/get @*noir-session* (name k) default)))

(defn swap! 
  "Replace the current session's value with the result of executing f with
  the current value and args."
  [f & args]
  (apply clojure.core/swap! *noir-session* f args))

(defn clear! 
  "Remove all data from the session and start over cleanly."
  []
  (reset! *noir-session* {}))

(defn remove!
  "Remove a key from the session"
  [k]
  (clojure.core/swap! *noir-session* dissoc (name k)))

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

;; ## Flash

(declare ^:dynamic *noir-flash*)

(defn flash-put!
  "Store a value that will persist for this request and the next."
  [k v]
  (clojure.core/swap!
   *noir-flash*
   (fn [old]
     (-> old
         (assoc-in [:incoming k] v)
         (assoc-in [:outgoing k] v)))))

(defn flash-get
  "Retrieve the flash stored value. This will remove the flash from the
  session."
  ([k]
     (flash-get k nil))
  ([k not-found]
     (get-in @*noir-flash* [:incoming k] not-found)))

(defn wrap-noir-flash [handler]
  (fn [request]
    (binding [*noir-flash* (atom {:incoming (:flash request)})]
      (let [resp (handler request)
            outgoing-flash (merge (:outgoing *noir-flash*)
                                  (:flash resp))]
        (if (and resp outgoing-flash)
          (assoc resp :flash outgoing-flash)
          resp)))))