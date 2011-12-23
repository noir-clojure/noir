(ns noir.incubator.middleware
  "Helpful middleware functions that are incubating")

(defn wrap-request-loging-with-formatter [handler formatter]
  "Adds simple loging for requests.  The output shows the current time,  the request method, uri,
   and total time spent processing the request."
  (fn [{:keys [request-method uri] :as req}]
    (let [start  (System/currentTimeMillis)
          resp   (handler req)
          status (:status resp)
          finish (System/currentTimeMillis)
          total  (- finish start)]
          (formatter request-method uri total)
          resp)))

(defn- logformatter [reqmeth uri totaltime]
  "Basic logformatter for loging middleware.
  (let [line (format "[%s] %s [Status: %s] %s (%dms)" (java.util.Date.)  reqmeth uri totaltime)]
    (locking System/out (println line))))

(defn wrap-request-loging [handler]
  "Provide a default loger with a default logformatter"
  (wrap-request-loging-with-formatter handler logformatter))