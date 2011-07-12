(ns noir.util.middleware
  "Helpful middleware functions")

(defn wrap-utf-8 
  "Adds the 'charset=utf-8' clause onto the content type declaration, allowing pages
  to display all utf-8 characters."
  [handler]
  (fn [request]
    (let [resp (handler request)
          ct (get-in resp [:headers "Content-Type"])
          neue-ct (str ct "; charset=utf-8")]
      (assoc-in resp [:headers "Content-Type"] neue-ct))))

