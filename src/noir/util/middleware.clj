(ns noir.util.middleware
  "Helpful middleware functions")

(defn wrap-utf-8 
  "DEPRECATED - Ring does this automatically now.
  
  Adds the 'charset=utf-8' clause onto the content type declaration, allowing pages
  to display all utf-8 characters."
  {:depecrated "1.2.0"}
  [handler]
  (fn [request]
    (let [resp (handler request)]
      (update-in resp [:headers "Content-Type"] str "; charset=utf-8"))))