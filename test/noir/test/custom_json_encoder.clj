(ns noir.test.custom-json-encoder
    (:use cheshire.custom))

(add-encoder java.awt.Color
    (fn [c jsonGenerator]
        (.writeString jsonGenerator (str c))))