(ns noir.core
  (:use hiccup.core
        compojure.core)
  (:require [clojure.string :as string]))

(declare *options*)
(def *noir-routes* (atom {}))

(defn- keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (subs (str kw) 1))))

(defn- route->key [action rte]
  (let [action (string/replace (str action) #".*/" "")]
    (str action (string/replace rte #"/" "--"))))

(defn- parse-route [rte]
  (let [[action url] (if (vector? rte)
                         [(keyword->symbol "compojure.core" (first rte)) (second rte)]
                         ['compojure.core/GET rte])
        func (symbol (route->key action url))]
    {:action action :url url :route-fn func}))

(defmacro defpage 
  "Adds a route to the server whose content is the the result of evaluating the body.
  The function created is passed the params of the request and the destruct param allows
  you to destructure that meaningfully for use in the body. Routes can either be a string
  or a vector of [method route], such as [:post '/vals']. The default method is GET."
  [route destruct & body]
  (let [{action# :action url# :url fn-name# :route-fn} (parse-route route)]
    `(do
       (defn ~fn-name# [~destruct]
         ~@body)
       (swap! *noir-routes* assoc ~(keyword fn-name#) (~action# ~url# {params# :params} (~fn-name# params#))))))

(defmacro defpartial 
  "Create a function that returns html using hiccup. The function is then callable using the given name."
  [fname params & body]
  `(defn ~fname ~params
     (html
       ~@body)))

(defmacro render 
  "Renders the content for a route by calling the page like a function
  with the given param map. Just like with defpage, route can be a vector,
  e.g. [:post '/vals']"
  [route & params]
  (let [{func# :route-fn} (parse-route route)]
    `(~func# ~(first params))))
