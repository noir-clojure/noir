(ns noir.example
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers)
  (:require [noir.server :as server]
            [noir.cookies :as cookie]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as session]))

(defpartial main-layout [& content]
            (html5
              [:head
               [:title "Awesome"]]
              [:body
               content]))

(defpage "/" {}
         (main-layout
           [:p "Welcome to noir."]))

(defpage "/params" {awk :awk} 
         (main-layout
           [:h2 "Params everywhere."]
           [:p "Do you have an awk get-param? " (if awk
                                                  (str "Yes you do! " awk)
                                                  "It doesn't appear so")]
           (form-to [:post "/params"]
                    (text-field "hey"))))

(defpage [:post "/params"] {hey :hey}
         (main-layout
           [:h2 "You posted something:"]
           [:p hey]))

(defpage "/json/:name" {n :name}
         (resp/json
           {:name n}))

(defpage "/cookie" []
         (cookie/put! :noir "stuff")
         (main-layout
           [:p "You created a cookie."]))

(defpage "/cookie-map" []
         (cookie/put! :noir2 {:value "more stuff" :path "/cookie-map" :expires 1})
         (main-layout
           [:p "You created another cookie."]))

(defpage "/multi-cookie" []
         (cookie/put! :a1 "awk")
         (cookie/put! :a2 "awk2")
         (main-layout
          [:p "Multiple cookies on a single page!"]))

(defpage "/cookie-vals" []
         (let [v (cookie/get :a1)]
           (main-layout
             [:p "Here's the value of one of the cookies you set: " v])))

(defpage "/session" []
         (session/put! :awe "sweet")
         (session/put! :userid 1)
         (main-layout
           [:p "session: " @session/*noir-session*]
           [:p "userid: " (session/get :userid)]))

(defpage "/session-continues" []
         (main-layout
           [:p "session: " @session/*noir-session*]
           [:p "userid: " (session/get :userid)]))

(defpage "/valid" []
         (vali/rule (= 1 2)
                    [:math "One and two are not equal"])
         (vali/rule (vali/min-length? "chris" 4)
                    [:username "A name has to be at least 4 chars!"])
         (main-layout
           [:p "Let's check your math: " (vali/errors? :math)]
           [:p "How about your name? " (or (vali/errors? :username) 
                                           "Well, it appears to be the appropriate length.")]))

(error-page! 404
             (main-layout
               [:p "We couldn't find what you were looking for!"]))

(server/start 8082)
