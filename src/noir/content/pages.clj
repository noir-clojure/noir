(ns noir.content.pages
  (:use noir.core
        noir.content.defaults
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpartial logo []
            [:a (image "/img/noir-logo.png" "Noir")])

(defpartial header []
            [:div#header 
             [:h1 (logo)]
             ])

(defpage "/" []
         (noir-layout
           (header)
           [:ul
            [:li
             [:div.left
              [:p "Alright, now that you're up and running, let's start building your site.
                  We can start by creating your welcome page."]
              ]
             [:div.right
              [:pre
              [:code 
"(defpage \"/welcome\" []
  (html
    [:h1 \"Welcome to my site!\"]))"]
              ]]]
            [:li
             [:div.left
              [:p "This isn't very good though, we really need a layout for all our pages
                  so we'll create a partial (a function that returns html)."]
              ]
             [:div.right
              [:pre
              [:code 
"(defpartial layout [& content]
  (html5
    [:head
      [:title \"my site\"]]
    [:body
      [:div#wrapper
        content]]))"]
              ]]]
            [:li
             [:div.left
              [:p "Now let's update our welcome page to use our layout."]
              ]
             [:div.right
              [:pre
              [:code 
"(defpage \"/welcome\" []
  (layout
    [:h1 \"Welcome to my site!\"]
    [:p \"Hope you like it.\"]))"]
              ]]]



            [:li
             [:div.left
              [:p "Ok, now you can remove this page by deleting this line:"]
              ]
             [:div.right
              [:code "[noir.content.pages :as pages]"]
              ]]

            ]
            ))
