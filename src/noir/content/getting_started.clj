(ns
  #^{:skip-wiki true}
  noir.content.getting-started
  (:use noir.core
        noir.content.defaults
        hiccup.page))

(def header-links [{:url "http://www.webnoir.org/tutorials" :text "Tutorials"}
                   {:url "http://groups.google.com/group/clj-noir" :text "Google Group"}
                   {:url "http://www.webnoir.org/docs/" :text "API"}])

(defpartial link [{:keys [url text]}]
            (link-to url text))

(defpartial link-item [lnk]
            [:li
             (link lnk)])

(defpartial logo []
            (link-to "http://www.webnoir.org/" (image "/img/noir-logo.png" "Noir")))

(defpartial header []
            [:div#header
             [:h1 (logo)]
             [:ul
              (map link-item header-links)]])

(defpage "/" []
         (noir-layout
           (header)
           [:p.announce "Noir is up and running... time to start building some websites!"]
           [:ul.content
            [:li
             [:div.right
              [:pre
              [:code
"(defpage \"/my-page\" []
  (html
    [:h1 \"This is my first page!\"]))"]]]
             [:div.left
              [:p "Time to get going with our first page. Let's open views/welcome.clj
                  and use (defpage) to add a new page to our site. With that we can go to "
                  (link-to "http://localhost:8080/my-page" "http://localhost:8080/my-page")
                  " and see our handiwork."]]]

            [:li
             [:div.right
              [:pre
              [:code
"(defpartial site-layout [& content]
  (html5
    [:head
      [:title \"my site\"]]
    [:body
      [:div#wrapper
        content]]))"]]]
             [:div.left
              [:p "We really need a layout for all our pages, so let's create a
                  partial (a function that returns html). We'll do that
                  in views/common.clj since all your views will use it."]]]

            [:li
             [:div.right
              [:pre
              [:code
"(defpage \"/my-page\" []
  (common/site-layout
    [:h1 \"Welcome to my site!\"]
    [:p \"Hope you like it.\"]))"]]]
             [:div.left
              [:p "Now we'll update our page to use the layout. Just refresh the browser
                  and you'll see your change."]]]

            [:li
             [:div.right
              [:code "[noir.content.getting-started]"]]
             [:div.left
              [:p "That's it! You've created your own page. Now get rid of this one simply by
                  removing the require for getting-started at the top."]]]]))
