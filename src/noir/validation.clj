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
    (every? has-value? vs)))

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
  (re-matches #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" v))


(defn valid-file?
  "Returns true if a valid file was supplied"
  [m]
  (and (:size m)
       (> (:size m) 0)
       (:filename m)))


(defn valid-number?
  "Returns true if the string can be cast to a Long"
  [v]
  (try
    (Long/parseLong v)
    true
    (catch Exception e
      false)))


(defn greater-than?
  "Returns true if the string represents a number > given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))


(defn less-than?
  "Returns true if the string represents a number < given."
  [v n]
  (and (valid-number? v)
       (> (Long/parseLong v) n)))

(declare ^:dynamic *errors*)

;;errors and rules
(defn get-errors
  "Get the errors for the given field. This will return a vector of all error strings or nil."
  [& [field]]
  (if field
    (get @*errors* field)
    (apply concat (vals @*errors*))))

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
  contain errors, return false. If no fields are supplied return true if any errors exist."
  [& field]
  (if-not (seq field)
    (not (empty? @*errors*))
    (some not-nil? (map get-errors field))))

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
