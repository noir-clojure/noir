(ns noir.content.defaults
  (:use noir.core
        noir.content.css
        hiccup.core
        hiccup.page-helpers))

(defpartial noir-layout [& content]
            (html5
              [:head
               [:title "Noir"]
               (include-css "/css/reset.css")
               [:style {:type "text/css"} (noir-css)]]
              [:body
               [:div#wrapper
                content]]))

(defpartial not-found []
  (noir-layout
    [:div#not-found
     [:h1 "We seem to have lost that one."]
     [:p "Since we couldn't find the page you were looking for, check to make sure the address is correct."]]
    ))

(defpartial exception-item [{nams :ns in-ns? :in-ns? fq :fully-qualified f :file line :line :as ex}]
      [:tr {:class (when in-ns?
                     "mine")}
        [:td.dt f " :: " line]
        [:td.dd fq]])

(defpartial stack-trace [{exception :exception causes :causes}]
    (noir-layout
      [:div#exception
       [:h1 (or (:message exception) "An exception was thrown") [:span " - (" (:class exception) ")"]]
       [:table [:tbody (map exception-item (:trace-elems exception))]]
       (for [cause causes :while cause]
         [:div.cause
          [:h3 "Caused by: " (:class cause) " - " (:message cause)]
          [:table (map exception-item (:trimmed-elems cause))]])]
      ))

(defpartial internal-error []
  (noir-layout
    [:div#not-found
     [:h1 "Something very bad has happened."]
     [:p "We've dispatched a team of highly trained gnomes to take 
         care of the problem."]]))


