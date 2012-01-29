(ns noir.exception
  "Functions to handle exceptions within a Noir server gracefully."
  (:use clj-stacktrace.core
        clj-stacktrace.repl)
  (:require [clojure.string :as string]
            [noir.options :as options]
            [noir.statuses :as statuses]
            [noir.content.defaults :as defaults]))

(defn- route-fn? [k]
  (and k
       (re-seq #".*--" k)))

(defn- key->route-fn [k]
  (if (route-fn? k)
    (let [with-slahes (-> k
                        (string/replace #"!dot!" ".")
                        (string/replace #"--" "/")
                        (string/replace #">" ":")
                        (string/replace #"<" "*"))
          separated (string/replace with-slahes #"(POST|GET|HEAD|ANY|PUT|DELETE)" #(str (first %1) " :: "))]
      separated)
    k))

(defn- ex-item [{anon :annon-fn func :fn nams :ns clj? :clojure f :file line :line :as ex}]
  (let [func-name (if (and anon func (re-seq #"eval" func))
                    "anon [fn]"
                    (key->route-fn func))
        ns-str (if clj?
                 (if (route-fn? func)
                   (str nams " :: " func-name)
                   (str nams "/" func-name))
                 (str (:method ex) "." (:class ex)))
        in-ns? (and nams (re-seq
                           (re-pattern (str (options/get :ns)))
                           nams))]
    {:fn func-name
     :ns nams
     :in-ns? in-ns?
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
               (assoc cause :trimmed-elems (map ex-item (:trimmed-elems cause))))}))

(defn wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (.printStackTrace e)
        (let [content (if (options/dev-mode?)
                        (try
                          (defaults/stack-trace (parse-ex e))
                          (catch Throwable e
                            (statuses/get-page 500)))
                        (statuses/get-page 500))]
          {:status 500
           :headers {"Content-Type" "text/html"}
           :body content})))))
