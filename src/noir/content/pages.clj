(ns noir.content.pages
  (:use noir.core
        noir.content.defaults
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpartial header []
            [:div#header 
             [:h1 [:a (image "/img/logo.png" "Noir")]]
             [:h2 "The Clojure web framework"]])

(defpage "/" []
         (noir-layout
           (header)
           [:div.left
            [:code "(defpage \"/woot\" [] (html [:p \"woot\"]))"]
            ]
           [:div.right
            [:p "Noir is a web framework built on top of compojure, hiccup and other
                libraries to provide a complete solution for creating websites in
                Clojure."]
            ]
            
            ))
