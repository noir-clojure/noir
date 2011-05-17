(ns noir.response
  (:require [clojure.contrib.json :as json]))

(defn json [content]
  {:headers {"Content-Type" "application/json"}
   :body (json/json-str content)})

(defn status [code content]
  {:status code
   :body content})
