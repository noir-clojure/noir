(ns noir.session
  (:refer-clojure :exclude [get remove])
  (:use ring.middleware.session
        ring.middleware.session.memory))

(declare *noir-session*)
(defonce mem (atom {}))

(defn noir-session [handler]
  (fn [request]
    (binding [*noir-session* (atom (:session request))]
      (let [resp (handler request)]
        (assoc resp :session @*noir-session*)))))

(defn put! 
  "Associates the key with the given value in the session"
  [k v]
  (swap! *noir-session* assoc k v))

(defn get 
  "Get the key's value from the session, returns nil if it doesn't exist."
  [k]
  (clojure.core/get @*noir-session* k))

(defn clear! []
  (reset! *noir-session* {}))

(defn remove!
  "Remove a key from the session"
  [k]
  (swap! *noir-session* dissoc k))

(defn wrap-noir-session [handler]
  (-> handler
    (noir-session)
    (wrap-session {:store (memory-store mem)})))
