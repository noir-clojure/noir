(ns noir.test.util
  (:require [noir.session :as session]
            [noir.cookies :as cookies]
            [noir.options :as options]))


(defmacro with-noir [& body]
  `(binding [options/*options* options/default-opts
             session/*noir-session* (atom {})
             cookies/*new-cookies* (atom {})
             cookies/*cur-cookies* (atom {})]
     ~@body))
    




