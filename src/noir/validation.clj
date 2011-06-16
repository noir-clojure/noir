(ns noir.validation
  "Functions for validating input and setting string errors on fields. 
  All fields are simply keys, meaning this can be a general error storage and 
  retrieval mechanism for the lifetime of a single request. Errors are not 
  persisted and are cleaned out at the end of the request.")

;; validation helpers

(defn has-value? 
  "Returns true if v is truthy and not an empty string."
  [v]
  (and v (not= v "")))

(defn has-values? 
  "Returns true if all members of the collection has-value? This works on maps as well."
  [coll]
  (let [vs (if (map? coll)
             (vals coll)
             coll)]
    (map has-value? vs)))

(defn not-nil? 
  "Returns true if v is not nil"
  [v]
  (or v (false? v)))

(defn min-length? 
  "Returns true if v is greater than or equal to the given len"
  [v len]
  (>= (count v) len))

(defn max-length? 
  "Returns true if v is less than or equal to the given len"
  [v len]
  (<= (count v) len))

(defn is-email? 
  "Returns true if v is an email address"
  [v]
  (re-matches #"(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b" v))

(declare *errors*)

;;errors and rules
(defn get-errors 
  "Get the errors for the given field. This will return a vector of all error strings or nil."
  [field]
  (get @*errors* field))

(defn set-error 
  "Explicitly set an error for the given field. This can be used to 
  create complex error cases, such as in a multi-step login process."
  [field error]
  (let [merge-map (if (get-errors field)
                    {field error}
                    {field [error]})]
    (swap! *errors* #(merge-with conj % merge-map))
    nil))

(defn rule 
  "If the passed? condition is not met, add the error text to the given field:
  (rule (not-nil? username) [:username \"Usernames must have a value.\"])"
  [passed? [field error]]
  (or passed? 
      (do 
        (set-error field error)
        false)))

(defn errors? 
  "For all fields given return true if any field contains errors. If none of the fields 
  contain errors, return false"
  [& field]
  (some not-nil? (map get-errors field)))

(defn on-error 
  "If the given field has an error, execute func and return its value"
  [field func]
  (if-let [errs (get-errors field)]
    (func errs)))

;;middleware

(defn wrap-noir-validation [handler]
  (fn [request]
    (binding [*errors* (atom {})]
      (handler request))))
