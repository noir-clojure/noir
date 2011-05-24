(ns noir.core
  (:use hiccup.core
        compojure.core)
  (:require [clojure.string :as string]))

(declare *options*)
(def *noir-routes* (atom {}))

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

(defmacro render 
  "Renders the content for a route by calling the page like a function
  with the given param map"
  [route & params]
  (let [func# (symbol (route->key route))]
    `(~func# ~(first params))))
