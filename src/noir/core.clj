(ns noir.core
  (:use hiccup.core
        compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as c-route]
            [clojure.string :as string]))

(declare *options*)
(def *noir-routes* (atom {}))
(def *error-pages* (atom {}))

(defn keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (subs (str kw) 1))))

(defn route->key [rte]
  (str "noir-rte" (string/replace rte #"/" "--")))

(defmacro defpage [route destruct & body]
  (let [[action# url#] (if (vector? route)
                         [(keyword->symbol "compojure.core" (first route)) (second route)]
                         ['compojure.core/GET route])
        fn-name# (symbol (route->key url#))]
    `(do
       (defn ~fn-name# [~destruct]
         ~@body)
       (swap! *noir-routes* assoc ~(keyword fn-name#) (~action# ~url# {params# :params} (~fn-name# params#))))))

(defmacro defpartial [moniker params & body]
  `(defn ~moniker ~params
     (html
       ~@body)))

(defn get-error-page [code]
  (get @*error-pages* code))

(defn error-page! [code content]
  (swap! *error-pages* assoc code content))
