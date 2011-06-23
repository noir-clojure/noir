(ns noir.util.crypt
  "Simple functions for hashing strings and comparing them. Typically used for storing passwords."
  (:refer-clojure :exclude [compare])
  (:import [org.mindrot.jbcrypt BCrypt]))

(defn gen-salt
  ([size]
   (BCrypt/gensalt size))
  ([]
   (BCrypt/gensalt)))

(defn encrypt 
  "Encrypt the given string with a generated or supplied salt. Uses BCrypt for strong hashing."
  ;; generate a salt
  ([salt raw] (BCrypt/hashpw raw salt))
  ([raw] (encrypt (gen-salt) raw)))

(defn compare
  "Compare a raw string with an already encrypted string"
  [raw encrypted]
  (BCrypt/checkpw raw encrypted))
