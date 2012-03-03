(ns noir.test.core
  (:use [noir.core]
        [compojure.core]
        [hiccup.core :only [html]]
        [hiccup.page-helpers :only [link-to]]
        [noir.util.test])
  (:use [clojure.test])
  (:require [noir.util.crypt :as crypt]
            [noir.server :as server]
            [noir.session :as session]
            [noir.request :as request]
            [noir.options :as options]
            [noir.response :as resp]
            [noir.cookies :as cookies]
            [noir.validation :as vali]))

(deftest hashing
  (let [pass (crypt/encrypt "password")]
    (is (crypt/compare "password" pass))))

(deftest session-get-default
  (with-noir
    (is (nil? (session/get :noir)))
    (is (= "noir" (session/get :noir "noir")))))

(deftest cookies
  (with-noir
    (cookies/put! :noir2 "woo")
    (is (= "woo" (cookies/get :noir2)))
    (is (nil? (cookies/get :noir)))
    (is (= "noir" (cookies/get :noir "noir")))))

(deftest cookies-get-signed
  (with-noir
    (is (nil? (cookies/get :noir3)))
    (cookies/put-signed! "s3cr3t-k3y" :noir3 "stored-value")
    ;; Check default behavior for bad keys.
    (is (nil? (cookies/get-signed "b4d-k3y" :noir3)))
    (is (= "noir" (cookies/get-signed "b4d-k3y" :noir3 "noir")))
    ;; Check retrieval of good value.
    (is (= "stored-value" (cookies/get-signed "s3cr3t-k3y" :noir3)))
    ;; Modify value,
    (cookies/put! :noir3 "changed-value")
    (is (nil? (cookies/get-signed "s3cr3t-k3y" :noir3)))))

(deftest options-get-default
  (with-noir
    (is (nil? (options/get :noir)))
    (is (= "noir" (options/get :noir "noir")))))

(deftest json-resp
  (with-noir
    (-> (resp/json {:noir "web"})
        (has-content-type (content-types :json))
        (has-body "{\"noir\":\"web\"}"))))

(deftest flash-lifetime
  (with-noir
    (session/flash-put! :test "noir")
    (is (= "noir" (session/flash-get :test)))))

(defpage "/test" {:keys [nme]}
  (str "Hello " nme))

(defpage "/request" {}
  (let [req (request/ring-request)]
    (is req)
    (is (map? req))
    (is (:uri req))))

(deftest request-middleware
  (send-request "/request"))

(deftest route-test
  (-> (send-request "/test" {"nme" "chris"})
      (has-status 200)
      (has-body "Hello chris")))

(deftest route-test
  (-> (send-request "/test" {"nme" "chris"})
      (has-status 200)
      (has-body "Hello chris")))

(defpage "/test.json" []
  (resp/json {:json "text"}))

(deftest route-dot-test
  (-> (send-request "/test.json")
      (has-status 200)
      (has-content-type "application/json")
      (has-body "{\"json\":\"text\"}")))

(deftest parsing-defpage
  (is (= (parse-args '[foo "/" [] "hey"])
         {:fn-name 'foo
          :url "/"
          :action 'compojure.core/GET
          :destruct []
          :body '("hey")}))
  (is (= (parse-args '["/" [] "hey"])
         {:fn-name 'GET--
          :url "/"
          :action 'compojure.core/GET
          :destruct []
          :body '("hey")}))
  (is (= (parse-args '[foo [:post "/"] [] "hey"])
         {:fn-name 'foo
          :url "/"
          :action 'compojure.core/POST
          :destruct []
          :body '("hey")}))
  (is (= (parse-args '[[:post "/"] [] "hey" "blah"])
         {:fn-name 'POST--
          :url "/"
          :action 'compojure.core/POST
          :destruct []
          :body '("hey" "blah")}))
  (is (= (parse-args '["/test" {} "hey"])
         {:fn-name 'GET--test
          :url "/test"
          :action 'compojure.core/GET
          :destruct {}
          :body '("hey")}))
  (is (thrown? Exception (parse-args '["/" 3 3])))
  (is (thrown? Exception (parse-args '["/" '() 3])))
  (is (thrown? Exception (parse-args '[{} '() 3]))))

;(defpage "/utf" [] "ąčęė")

(deftest url-for-before-def
  (is (= "/one-arg/5" (url-for route-one-arg {:id 5}))))

(defpage foo "/foo" []
  "named-route")

(pre-route "/pre" []
           (resp/status 403 "not allowed"))

(post-route "/post-route" []
            (resp/status 403 "not allowed"))

(defpage "/not-post-route" [] "success")
(post-route "/not-post-route" [] "fail")

(defpage "/pre" [] "you should never see this")

(compojure-route (ANY "/compojure" [] "compojure-route"))

(deftest pre-route-test
  (-> (send-request [:post "/pre"])
      (has-status 403)
      (has-body "not allowed"))
  (-> (send-request "/pre")
      (has-status 403)
      (has-body "not allowed")))

(deftest compojure-route-test
  (-> (send-request "/compojure")
      (has-status 200)
      (has-body "compojure-route")))

(deftest post-route-test
  (-> (send-request "/post-route")
      (has-status 403)
      (has-body "not allowed"))
  (-> (send-request "/not-post-route")
      (has-status 200)
      (has-body "success")))

(deftest named-route-test
  (-> (send-request "/foo")
      (has-status 200)
      (has-body "named-route")))

(defpage [:post "/post-route"] {:keys [nme]}
  (str "Post " nme))

(deftest render-test
  (is (= "named-route" (render foo)))
  (is (= "Hello chris" (render "/test" {:nme "chris"})))
  (is (= "Post chris") (render [:post "/post-route"] {:nme "chris"})))

(deftest route-post-test
  (-> (send-request [:post "/post-route"] {"nme" "chris"})
      (has-status 200)
      (has-body "Post chris")))

(defpage named-route-with-post [:post "/foo"] []
  "named-post")

(deftest named-route-post-test
  (-> (send-request [:post "/post-route"] {"nme" "chris"})
      (has-status 200)
      (has-body "Post chris")))

(defpage route-one-arg "/one-arg/:id" {id :id})
(def two-args "/two-args/:arg1/:arg2")

(deftest url-args
  (is (= "/foo" (url-for foo)))
  (is (= "/one-arg/cool"(url-for "/one-arg/:blah" {:blah "cool"})))
  (is (= "/one-arg/5" (url-for route-one-arg {:id 5})))
  (is (= "/star/blah/cool" (url-for "/star/*" {:* "blah/cool"})))
  (is (= "/two-args/blah/cool" (url-for two-args {:arg2 "cool" :arg1 "blah"})))
  ;; make sure only subset matters
  (is (= "/one-arg/5" (url-for route-one-arg {:id 5 :name "chris"})))
  ;; make sure order doesn't matter
  (is (= "/two-args/blah/cool" (url-for two-args {:arg1 "blah" :arg2 "cool"}))))

(deftest url-for-throws
  (is (thrown? Exception (url-for route-one-arg))))

(defpage "/base-url" []
  (html
    (link-to "/hey" "link")))

(deftest base-url
  (binding [options/*options* {:base-url "/woohoo"}]
    (-> (send-request "/base-url")
        (has-status 200)
        (has-body "<a href=\"/woohoo/hey\">link</a>"))))

(deftest jsonp
  (-> (resp/jsonp "jsonp245" {:pinot "noir"}) 
      (has-content-type "application/javascript")
      (has-body "jsonp245({\"pinot\":\"noir\"});")))

(defpage "/with%20space" []
  "space")

(deftest route-decoding
  (-> (send-request "/with%20space")
      (has-status 200)
      (has-body "space")))

(defpage "/wrap-route" [] "hey!")
(defn interceptor [handler]
  (fn [req]
    (let [resp (handler req)]
      (assoc resp :body "intercepted"))))

(deftest wrap-route-middleware
  (-> (send-request "/wrap-route")
      (has-body "hey!"))

  (server/wrap-route "/wrap-route" interceptor)

  (-> (send-request "/wrap-route")
      (has-body "intercepted")))

;(deftest wrap-utf
;  (-> (send-request "/utf")
;      (has-content-type "text/html; charset=utf-8")
;      (has-body "ąčęė"))
;  ;;Technically this middleware is unnecessary now due to some changes in ring.
;  ;;but this provides a nice test for custom middleware.
;  (server/add-middleware middleware/wrap-utf-8)
;  (-> (send-request "/utf")
;      (has-content-type "text/html; charset=utf-8; charset=utf-8")
;      (has-body "ąčęė")))

(deftest valid-emails
  (are [email] (vali/is-email? email)
       "testword@domain.com"
       "test+word@domain.com"
       "test_word@domain.com"
       "test'word@domain.com"
       "test`word@domain.com"
       "test#word@domain.com"
       "test=word@domain.com"
       "test|word@domain.com"
       "testword@test.domain.com"
       "te$t@test.com"
       "t`e's.t&w%o#r{d@t.e.s.t"
       "x@x.xx"))

(deftest invalid-emails
  (are [email] (not (vali/is-email? email))
       ".test@domain.com"
       "-@-.com"
       "test"
       "test.@domain.com"
       "test@com"))

(defpage "/different/content-type" []
  (resp/content-type "application/vcard+xml" (resp/xml (html [:vcards]))))

(deftest different-content-type
  (-> (send-request "/different/content-type")
      (has-content-type "application/vcard+xml")
      (has-body "<vcards />")))

(defpage "/different/status" []
  (resp/status 201 "Something was created"))

(deftest different-header
  (-> (send-request "/different/status")
      (has-status 201)))
