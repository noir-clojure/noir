(ns noir.test.core
  (:use [noir.core])
  (:use [clojure.test])
  (:require [noir.util.crypt :as crypt]))

(deftest hashing
         (let [pass (crypt/encrypt "password")]
           (is (crypt/compare "password" pass)))) 

(run-tests)
