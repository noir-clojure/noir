(ns noir.test.core
  (:use [noir.core]
        [noir.util.test])
  (:use [clojure.test])
  (:require [noir.util.crypt :as crypt]
            [noir.server :as server]
            [noir.util.middleware :as middleware]
            [noir.session :as session]
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
           (session/flash-put! "noir")
           (is (= "noir" (session/flash-get)))
           (is (= nil (session/flash-get)))))

(defpage "/test" {:keys [nme]}
         (str "Hello " nme))

(deftest route-test
         (-> (send-request "/test" {"nme" "chris"})
           (has-status 200)
           (has-body "Hello chris")))

(defpage "/test.json" []
         (resp/json {:json "text"}))

(deftest route-dot-test
         (-> (send-request "/test.json")
           (has-status 200)
           (has-content-type (content-types :json))
           (has-body "{\"json\":\"text\"}")))

(defpage "/utf" []
         "ąčęė")

(deftest wrap-utf
         (server/add-middleware middleware/wrap-utf-8)
         (-> (send-request "/utf")
           (has-content-type "text/html; charset=utf-8")
           (has-body "ąčęė")))

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
