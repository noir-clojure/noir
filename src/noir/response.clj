(ns noir.response
  "Simple response helpers to change the content type, redirect, or return a canned response"
  (:refer-clojure :exclude [empty])
  (:require [cheshire.core :as json]
            [noir.options :as options]))

(defn- ->map [c]
  (if-not (map? c)
    {:body c}
    c))

(defn set-headers
  "Add a map of headers to the given response. Headers must have
  string keys:

  (set-headers {\"x-csrf\" csrf}
    (common/layout [:p \"hey\"]))"
  [headers content]
  (update-in (->map content) [:headers] merge headers))

(defn content-type
  "Wraps the response with the given content type and sets the body to the content."
  [ctype content]
  (set-headers {"Content-Type" ctype} content))

(defn xml
  "Wraps the response with the content type for xml and sets the body to the content."
  [content]
  (content-type "text/xml; charset=utf-8" content))

(defn json
  "Wraps the response in the json content type and generates JSON from the content"
  [content]
  (content-type "application/json; charset=utf-8"
                (json/generate-string content)))

(defn jsonp
  "Generates JSON for the given content and creates a javascript response for calling
  func-name with it."
  [func-name content]
  (content-type "application/json; charset=utf-8"
                (str func-name "(" (json/generate-string content) ");")))

(defn status
  "Wraps the content in the given status code"
  [code content]
  (assoc (->map content) :status code))

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
