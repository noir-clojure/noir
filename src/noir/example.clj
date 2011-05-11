(ns ryle.example
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers)
  (:require [noir.server :as server]))

(defpartial main-layout [& content]
            (html5
              [:head
               [:title "Awesome"]]
              [:body
               content]))

(defpage "/" {}
         (main-layout
           [:p "Welcome home son. I mean for realz."]))

(defpage "/woot" {awk :awk} 
         (main-layout
           [:h2 "We all know how awesome this is."]
           [:p "Dude, you did this: " awk]
           (form-to [:post "/woot"]
                    (text-field "hey"))
           [:p "And this can only get better."]))

(defpage [:post "/woot"] {hey :hey}
         (main-layout
           [:h2 "You posted shit."]
           [:p hey]))

(server/start 8082)
