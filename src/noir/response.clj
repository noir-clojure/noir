(ns noir.response
  "Simple response helpers to change the content type, redirect, or return a canned response"
  (:refer-clojure :exclude [empty])
  (:require [clj-json.core :as json]))

(defn xml 
  "Wraps the response with the content type for xml and sets the body to the content."
  [content]
  {:header {"Content-Type" "text/xml"}
   :body content})

(defn content-type
  "Wraps the response with the given content type and sets the body to the content."
  [ctype content]
  {:header {"Content-Type" ctype}
   :body content})

(defn json 
  "Wraps the response in the json content type and stringifies the given content"
  [content]
  {:headers {"Content-Type" "application/json"}
   :body (json/generate-string content)})

(defn status 
  "Wraps the content in the given status code"
  [code content]
  {:status code
   :body content})

(defn redirect
  "A header redirect to a different url"
  [url]
  {:status 302
   :headers {"Location" url}
   :body ""})

(defn empty 
  "Return a successful, but completely empty response"
  []
  {:status 200
   :body ""})

