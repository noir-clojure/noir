(ns noir.util.crypt
  "Simple functions for hashing strings and comparing them. Typically used for storing passwords."
  (:refer-clojure :exclude [compare])
  (:require [ring.util.codec :as codec])
  (:import org.mindrot.jbcrypt.BCrypt))

(defn gen-salt
  "Generate a salt for BCrypt's hashing purposes."
  ([] (BCrypt/gensalt))
  ([rounds] (BCrypt/gensalt rounds)))

(defn gen-hash
  "Hash the given string with a generated or supplied salt. Uses BCrypt hashing algorithm.
The given salt (if supplied) needs to be in a format acceptable by Bcrypt. It's recommended 
that one uses `gen-salt` for generating salts."
  ;; generate a salt
  ([raw] (gen-hash (gen-salt) raw))
  ([salt raw]
     (BCrypt/hashpw raw salt)))

(defn compare 
  "Compare a raw string with an already hashed string"
  [raw hashed]
  (BCrypt/checkpw raw hashed))
