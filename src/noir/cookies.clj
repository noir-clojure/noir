(ns noir.cookies
  (:refer-clojure :exclude [get remove])
  (:use ring.middleware.cookies))

(def *cur-cookies* nil)
(def *new-cookies* nil)

(defn put! [k v]
  (let [props (if (map? v)
                v
                {:value v :path "/"})]
    (swap! *new-cookies* assoc k props)))

(defn keyword->str [kw]
  (subs (str kw) 1))

(defn get [k]
  (let [str-k (if (keyword? k)
                (keyword->str k)
                k)]
    (get-in *cur-cookies* [str-k :value])))

(defn noir-cookies [handler]
  (fn [request]
    (binding [*cur-cookies* (:cookies request)
              *new-cookies* (atom {})]
      (let [final (handler request)]
        (assoc final :cookies (merge (:cookies final) @*new-cookies*))))))

(defn wrap-noir-cookies [handler]
  (-> handler
    (noir-cookies)
    (wrap-cookies)))
