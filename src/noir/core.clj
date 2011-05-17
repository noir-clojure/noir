(ns noir.core
  (:use hiccup.core
        compojure.core
        ring.middleware.file-info
        ring.middleware.file)
  (:require [compojure.handler :as handler]
            [noir.cookies :as cookie]
            [noir.session :as session]
            [noir.validation :as validation]
            [clojure.string :as string]))

(def *noir-routes* (atom []))
(def *error-pages* (atom {}))

(defn keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (subs (str kw) 1))))

(defmacro defpage [route destruct & body]
  (let [[action# url#] (if (vector? route)
                         [(keyword->symbol "compojure.core" (first route)) (second route)]
                         ['compojure.core/GET route])
        gc-fn# (symbol "get-cookie")]
    `(swap! *noir-routes* conj (~action# ~url# {params# :params :as response#} ((fn [~destruct] ~@body) params#)))))

(defmacro defpartial [moniker params & body]
  `(defn ~moniker ~params
     (html
       ~@body)))

(defn error-page! [code content]
  (swap! *error-pages* assoc code content))

(defn wrap-errors [handler]
  (fn [request]
    (let [{status :status body :body :as resp} (handler request)]
      (if (and 
            (not= status 200)
            (not body))
        (assoc resp :body (get @*error-pages* status))
        resp))))

(defn wrap-noir [rts]
  (def final-routes
    (-> rts
      (handler/site)
      (session/wrap-noir-session)
      (cookie/wrap-noir-cookies)
      (validation/wrap-noir-validation)
      (wrap-errors)
      ;;(wrap-file "./public")
      (wrap-file-info))))

(defn init-routes []
  (wrap-noir (apply routes 
                    (conj @*noir-routes* (ANY "*" [] {:status 404 :body nil})))))
