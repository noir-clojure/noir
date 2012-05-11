(ns noir.test.validation
  (:use [noir.util.test])
  (:use [clojure.test])
  (:require [noir.validation :as vali]))

(deftest error-counting
  (with-noir
    (is (not (vali/errors?)))
    (vali/set-error :a "oh no")
    (is (vali/errors?))
    (is (vali/errors? :a))
    (is (not (vali/errors? :b)))))

(deftest get-all-errors
  (with-noir
    (vali/set-error :a "blah")
    (vali/set-error :b "cool")
    (is (= (set (vali/get-errors)) (set ["blah" "cool"])))))

(deftest is-email-case-insensitive
  (with-noir
   (is (vali/is-email? "me@here.com"))
   (is (vali/is-email? "Me@here.com"))
   (is (vali/is-email? "me@Here.coM"))
   (is (vali/is-email? "ME@HERE.COM"))))
