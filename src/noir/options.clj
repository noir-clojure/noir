(ns noir.options
  (:refer-clojure :exclude [get]))

(declare *options*)
(def default-opts {:ns (gensym)
                   :mode :dev})

(defn get [k]
  "Get an option from the noir options map"
  (clojure.core/get *options* k))

(defn dev-mode? []
  "Returns if the server is currently in development mode"
  (= (get :mode) :dev))

(defn wrap-options [handler opts]
  (fn [request]
    (binding [*options* (merge default-opts opts)]
      (handler request))))

