(ns noir.validation)

;; validation helpers

(defn has-value? [v]
    (and v (not= v "")))

(defn has-values? [obj]
  (map has-value? (vals obj)))

(defn not-nil? [v]
  (or v (false? v)))

(defn min-length? [v len]
  (>= (count v) len))

(defn max-length? [v len]
  (<= (count v) len))

(defn is-email? [v]
  (re-matches #"(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b" v))

(declare *errors*)

;;errors and rules
(defn set-error [field error]
  (swap! *errors* #(merge-with conj % {field error})))

(defn rule [passed? [field error]]
  (or passed? 
      (do 
        (set-error field error)
        false)))

(defn get-errors [field]
  (get @*errors* field))

(defn errors? [& field]
  (some not-nil? (map get-errors field)))

(defn on-error [field body]
  (if (get-errors field)
    body))

;;middleware

(defn wrap-noir-validation [handler]
  (fn [request]
    (binding [*errors* (atom {})]
      (handler request))))
