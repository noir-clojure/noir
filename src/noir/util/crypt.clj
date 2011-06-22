(ns noir.util.crypt
  "Simple functions for encrypting strings and comparing them. Typically used for storing passwords."
  (:refer-clojure :exclude [compare])
  (:require [ring.util.codec :as codec])
  (:import java.security.MessageDigest java.security.SecureRandom))

(defn gen-salt []
  (let [salt (byte-array 8)]
    ;; generate a truly random sequence of bytes
    (.. SecureRandom (getInstance "SHA1PRNG") (nextBytes salt))
    salt))

(defn ext-salt [stored]
  (byte-array (take 8 (codec/base64-decode stored))))

(defn encrypt 
  "Encrypt the given string with a generated or supplied salt. Uses SHA-256 encryption."
  ;; generate a salt
  ([raw] (encrypt (gen-salt) raw))
  ([salt raw]
    ;; append the salt to the front
   (str (codec/base64-encode salt) 
        (codec/base64-encode (let [mdig (. MessageDigest getInstance "SHA-256")
                                   raw-bytes (codec/base64-decode raw)]
                               (. mdig reset)
                               ;; use the salt to encrypt
                               (. mdig update salt)
                               (. mdig digest raw-bytes))))))

(defn compare 
  "Compare a raw string with an already encrypted string"
  [raw encrypted]
  (= (encrypt (ext-salt encrypted) raw) encrypted))

