(ns noir.exception
  (:use clj-stacktrace.core
        clj-stacktrace.repl)
  (:require [clojure.string :as string]))  

(defn route-fn? [k]
  (and k
       (re-seq #"noir-rte" k)))

(defn key->route-fn [k]
  (if (route-fn? k)
    (str " :: " (string/replace (subs k 8) #"--" "/"))
    k))

(defn ex-item [{anon :annon-fn func :fn nams :ns clj? :clojure f :file line :line :as ex}]
  (let [func-name (if (and anon func (re-seq #"eval" func))
                    "anon [fn]"
                    (key->route-fn func))
        ns-str (if clj?
                 (if (route-fn? func)
                   (str nams func-name)
                   (str nams "/" func-name))
                 (str (:method ex) "." (:class ex)))]
    {:fn func-name
     :ns nams
     :fully-qualified ns-str
     :annon? anon
     :clj? clj?
     :file f
     :line line}))
    
(defn parse-ex [ex]
  (let [clj-parsed (iterate :cause (parse-exception ex))
        exception (first clj-parsed)
        causes (rest clj-parsed)]
    {:exception (assoc exception :trace-elems (map ex-item (:trace-elems exception)))
     :causes (for [cause causes :while cause]
               (assoc cause :trimmed-elems (map ex-item (:trimmed-elems cause))))}
    ))

