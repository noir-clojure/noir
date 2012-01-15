(ns noir.statuses
  "If no pages are defined that match a request, a status page is used based on the
  the HTTP status code of the response. This contains the function necessary to get
  or set these status pages."
  (:require [noir.content.defaults :as defaults]
            [ring.util.response :as ring-resp]))

(def status-pages (atom {404 (defaults/not-found)
                         500 (defaults/internal-error)}))

(defn get-page 
  "Gets the content to display for the given status code"
  [code]
  (get @status-pages code))

(defn set-page! 
  "Sets the content to be displayed if there is a response with the given status
  code. This is used for custom 404 pages, for example."
  [code content]
  (swap! status-pages assoc code content))

(defn status-response [orig]
  (let [{:keys [status headers]} orig
        content (or (get-page status) (get-page 404))
        headers (merge {"Content-Type" "text/html; charset=utf-8"}
                       headers)
        final (-> orig
                  (assoc :headers headers)
                  (assoc :body content))]
    final))

(defn wrap-status-pages [handler]
  (fn [request]
    (let [{status :status body :body :as resp} (handler request)]
      (if (and 
            resp
            (not= status 200)
            (not body))
        (status-response resp)
        resp))))

