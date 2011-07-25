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
            [noir.cookies :as cookies]))

(deftest hashing
         (let [pass (crypt/encrypt "password")]
           (is (crypt/compare "password" pass)))) 

(deftest session-get-default
         (with-noir
           (is (nil? (session/get :noir)))
           (is (= "noir" (session/get :noir "noir")))))

(deftest cookies-get-default
         (with-noir
           (is (nil? (cookies/get :noir)))
           (is (= "noir" (cookies/get :noir "noir")))))

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

