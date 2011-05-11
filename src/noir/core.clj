(ns noir.core
  (:use hiccup.core
        compojure.core
        ring.middleware.file-info
        ring.middleware.file)
  (:require [compojure.handler :as handler]
            [clojure.string :as string]))

(def noir-routes (atom []))

(defn keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (subs (str kw) 1))))

(defmacro defpage [route destruct & body]
  (let [[action# url#] (if (vector? route)
                         [(keyword->symbol "compojure.core" (first route)) (second route)]
                         ['compojure.core/GET route])]
        `(swap! noir-routes conj (~action# ~url# {params# :params} ((fn [~destruct]
                                                                  ~@body)
                                                                  params#)))))

(defmacro defpartial [moniker params & body]
  `(defn ~moniker ~params
     (html
       ~@body)))

(defn wrap-noir [rts]
  (def final-routes
    (-> rts
      (handler/site)
      ;;(wrap-file "./public")
      (wrap-file-info))))

(defn init-routes []
  (let [rts @noir-routes]
      (wrap-noir (apply routes rts))))

