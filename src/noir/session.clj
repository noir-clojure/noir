(ns noir.session
  (:refer-clojure :exclude [get remove])
  (:use ring.middleware.session))


(declare *noir-session*)

(defn noir-session [handler]
  (fn [request]
    (binding [*noir-session* (atom (:session request))]
      (let [resp (handler request)]
        (assoc resp :session @*noir-session*)))))

(defn put! [k v]
  (swap! *noir-session* assoc k v))

(defn get [k]
  (clojure.core/get @*noir-session* k))

(defn remove [k]
  (swap! *noir-session* dissoc k))

(defn wrap-noir-session [handler]
  (-> handler
    (noir-session)
    (wrap-session)))
