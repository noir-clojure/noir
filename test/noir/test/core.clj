(ns noir.test.core
  (:use [noir.core]
        [noir.test.util])
  (:use [clojure.test])
  (:require [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.server :as server]
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

(defpage "/test" []
         "Hello")

(deftest route-test
         (let [handler (server/gen-handler)]
           (-> (handler (request "/test"))
             (has-status 200)
             (has-body "Hello"))))
