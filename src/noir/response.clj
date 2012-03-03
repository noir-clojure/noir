(ns noir.response
  "Simple response helpers to change the content type, redirect, or return a canned response"
  (:refer-clojure :exclude [empty])
  (:require [cheshire.core :as json]
            [noir.options :as options]))

(defn- tomap [content]
  (if (map? content) content {:body content}))

(defn set-header
  "Add a key value pair to the response headers"
  [key value content]
  (let [resp (tomap content)
        headers (or (some :headers resp) {})]
    (assoc resp :headers (assoc headers key value))))

(defn content-type
  "Wraps the response with the given content type and sets the body to the content."
  [ctype content]
  (set-header "Content-Type" ctype content))

(defn xml
  "Wraps the response with the content type for xml and sets the body to the content."
  [content]
  (content-type "text/xml" content))

(defn json
  "Wraps the response in the json content type and generates JSON from the content"
  [content]
  (content-type "application/json" (json/generate-string content)))

(defn jsonp
  "Generates JSON for the given content and creates a javascript response for calling
  func-name with it."
  [func-name content]
  (content-type "application/javascript" 
                (str func-name "(" (json/generate-string content) ");")))

(defn status
  "Wraps the content in the given status code"
  [code content]
  (assoc (tomap content) :status code))

(defn redirect
  "A header redirect to a different url"
  [url]
  (status 302 (set-header "Location" (options/resolve-url url) "")))

(defn empty
  "Return a successful, but completely empty response"
  []
  (status 200 ""))
