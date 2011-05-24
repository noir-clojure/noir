(ns noir.server
  (:use compojure.core
        ring.adapter.jetty
        ring.middleware.file-info
        ring.middleware.reload-modified
        ring.middleware.file)
  (:require [compojure.handler :as handler]
            [compojure.route :as c-route]
            [noir.core :as noir]
            [noir.content.defaults :as defaults]
            [noir.cookies :as cookie]
            [noir.session :as session]
            [noir.validation :as validation]))

(declare init-routes)
(def spec-routes (routes (c-route/resources "/")
                         (ANY "*" [] {:status 404 :body nil})))
(def *error-pages* (atom {404 (defaults/not-found)
                          500 (defaults/internal-error)}))

(defn error-page 
  "Gets the content to display for the given error code"
  [code]
  (get @*error-pages* code))

(defn set-error! 
  "Sets the content to be displayed if there is a response with the given status
  code. This is used for custom 404 pages, for example."
  [code content]
  (swap! *error-pages* assoc code content))

(defn dev-mode? []
  (= (:mode noir/*options*) :dev))

(defn- wrap-status-pages [handler]
  (fn [request]
    (let [{status :status body :body :as resp} (handler request)]
      (if (and 
            (not= status 200)
            (not body))
        (assoc resp :body (or (error-page status) (error-page 400)))
        resp))))

(defn- wrap-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (println e)
        (let [content (if (dev-mode?)
                        (defaults/stack-trace e)
                        (error-page 500))]
          {:status 500
           :headers {"Content-Type" "text/html"}
           :body content})))))

(defn- wrap-options [handler opts]
  (fn [request]
    (binding [noir/*options* opts]
      (handler request))))

(defn- wrap-route-updating [handler]
  (if (dev-mode?)
    (wrap-reload-modified (fn [request]
                   (init-routes noir/*options*)
                   (handler request))
                 ["src"])
    handler))


(defn- init-routes [opts]
  (binding [noir/*options* opts]
    (def final-routes
      ;; this has to be done as a function to delay the evaluation of the list of routes 
      ;; until the very end, so that we can add routes and get them on the first request
      ;; as opposed to requiring two refreshes to see a new page.
      (-> (fn [request] 
            ((apply routes (conj (vec (vals @noir/*noir-routes*)) spec-routes)) request))
        (handler/site)
        (session/wrap-noir-session)
        (cookie/wrap-noir-cookies)
        (validation/wrap-noir-validation)
        (wrap-status-pages)
        (wrap-file-info)
        (wrap-route-updating)
        (wrap-exceptions)
        (wrap-options opts)))))

(defn start [port opts]
  (init-routes opts)
  (run-jetty (var final-routes) {:port port}))

