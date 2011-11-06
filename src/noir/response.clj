(ns noir.response
  "Simple response helpers to change the content type, redirect, or return a canned response"
  (:refer-clojure :exclude [empty])
  (:require [clj-json.core :as json]
            [noir.options :as options]))

(defn xml 
  "Wraps the response with the content type for xml and sets the body to the content."
  [content]
  {:headers {"Content-Type" "text/xml"}
   :body content})

(defn content-type
  "Wraps the response with the given content type and sets the body to the content."
  [ctype content]
  {:headers {"Content-Type" ctype}
   :body content})

(defn json 
  "Wraps the response in the json content type and generates JSON from the content"
  [content]
  {:headers {"Content-Type" "application/json"}
   :body (json/generate-string content)})

(defn jsonp
  "Generates JSON for the given content and creates a javascript response for calling 
  func-name with it."
  [func-name content]
  {:headers {"Content-Type" "application/javascript"}
   :body (str func-name "(" (json/generate-string content) ");")})

(defn status 
  "Wraps the content in the given status code"
  [code content]
  {:status code
   :body content})

(defn redirect
  "A header redirect to a different url"
  [url]
  {:status 302
   :headers {"Location" (options/resolve-url url)}
   :body ""})

(defn empty 
  "Return a successful, but completely empty response"
  []
  {:status 200
   :body ""})

