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
(defn get-errors [field]
  (get @*errors* field))

(defn set-error [field error]
  (let [merge-map (if (get-errors field)
                    {field error}
                    {field [error]})]
    (swap! *errors* #(merge-with conj % merge-map))
    nil))

(defn rule [passed? [field error]]
  (or passed? 
      (do 
        (set-error field error)
        false)))

(defn errors? [& field]
  (some not-nil? (map get-errors field)))

(defn on-error [field func]
  (if-let [errs (get-errors field)]
    (func errs)))

;;middleware

(defn wrap-noir-validation [handler]
  (fn [request]
    (binding [*errors* (atom {})]
      (handler request))))
