(ns noir.content.defaults
  (:use noir.content.css
        hiccup.core
        hiccup.page-helpers)
  (:require [noir.core :as noir]
            [noir.exception :as ex]))

(defn noir-layout [& content]
            (html5
              [:head
               [:title "Noir"]
               [:style {:type "text/css"} (noir-css)]]
              [:body
               content]))

(defn not-found []
  (noir-layout
    [:div#not-found
     [:h1 "We seem to have lost that one."]
     [:p "Since we couldn't find the page you were looking for, check to make sure the address is correct."]]
    ))

(defn exception-item [{nams :ns fq :fully-qualified f :file line :line :as ex}]
  (let [in-ns? (and nams (re-seq
                           (re-pattern (str (:ns noir/*options*)))
                           nams))]
    (html 
      [:tr {:class (when in-ns?
                     "mine")}
        [:td.dt f " :: " line]
        [:td.dd fq]])))

(defn stack-trace [exc]
  (let [{exception :exception causes :causes} (ex/parse-ex exc)]
    (noir-layout
      [:div#exception
       [:h1 (or (:message exception) "An exception was thrown") [:span " - (" (:class exception) ")"]]
       [:table [:tbody (map exception-item (:trace-elems exception))]]
       (for [cause causes :while cause]
         [:div.cause
          [:h3 "Caused by: " (:class cause) " - " (:message cause)]
          [:table (map exception-item (:trimmed-elems cause))]])]
      )))

(defn internal-error []
  (noir-layout
    [:div#not-found
     [:h1 "Something very bad has happened."]
     [:p "We've dispatched a team of highly trained gnomes to take 
         care of the problem."]]))


