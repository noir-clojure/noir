(ns noir.options
  "Allows access to Noir's server options"
  (:refer-clojure :exclude [get]))

(declare *options*)
(def default-opts {:ns (gensym)
                   :mode :dev})

(defn compile-options
  [opts]
  (if (map? opts)
    (merge default-opts opts)
    default-opts))

(defn get 
  "Get an option from the noir options map"
  ([k default]
   (clojure.core/get *options* k default))
  ([k]
   (clojure.core/get *options* k)))

(defn dev-mode? 
  "Returns if the server is currently in development mode"
  []
  (= (get :mode) :dev))

(defn wrap-options [handler opts]
  (let [final-opts (compile-options opts)]
    (fn [request]
      (binding [*options* final-opts]
        (handler request)))))

