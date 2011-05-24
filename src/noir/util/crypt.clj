(ns noir.util.crypt
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
  ;; generate a salt
  ([raw] (encrypt (gen-salt) raw))
  ([salt raw]
    ;; append the salt to the front
   (str (codec/base64-encode salt) 
        (codec/base64-encode (let [mdig (. MessageDigest getInstance "SHA-1")
                                   raw-bytes (codec/base64-decode raw)]
                               (. mdig reset)
                               ;; use the salt to encrypt
                               (. mdig update salt)
                               (. mdig digest raw-bytes))))))

(defn compare [raw stored]
  (= (encrypt (ext-salt stored) raw) stored))

