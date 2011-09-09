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
