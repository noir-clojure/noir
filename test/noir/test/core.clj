(ns noir.test.core
  (:use [noir.core]
        [noir.test.util])
  (:use [clojure.test])
  (:require [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.options :as options]
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
