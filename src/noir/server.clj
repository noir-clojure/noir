(ns noir.server
  "A collection of functions to handle Noir's server and add middleware to the stack."
  (:use compojure.core
        clojure.java.io
        clojure.tools.namespace
        ring.adapter.jetty
        ring.middleware.file-info
        ring.middleware.reload-modified
        ring.middleware.file)
  (:require [compojure.handler :as handler]
            [compojure.route :as c-route]
            [noir.core :as noir]
            [noir.content.defaults :as defaults]
            [noir.cookies :as cookie]
            [noir.exception :as exception]
            [noir.statuses :as statuses]
            [noir.options :as options]
            [noir.session :as session]
            [noir.validation :as validation]))

(defonce *middleware* (atom #{}))

(defn- wrap-route-updating [handler]
  (if (options/dev-mode?)
    (wrap-reload-modified handler ["src"])
    handler))

(defn- wrap-custom-middleware [handler]
  (loop [cur handler
         mware (seq @*middleware*)]
    (if-not mware
      cur
      (let [[func args] (first mware)
            neue (apply func cur args)]
        (recur neue (next mware))))))

(defn- pack-routes []
  (apply routes (concat (vals @noir/*pre-routes*) (vals @noir/*noir-routes*) noir/*spec-routes*)))

(defn- init-routes [opts]
  (binding [options/*options* (options/compile-options opts)]
    (-> 
      (if (options/dev-mode?) 
        (fn [request] 
          ;; by doing this as a function we can ensure that any routes added as the
          ;; result of a modification are evaluated on the first reload.
          ((pack-routes) request))
        (pack-routes))
      (wrap-custom-middleware)
      (handler/site)
      (session/wrap-noir-session)
      (cookie/wrap-noir-cookies)
      (validation/wrap-noir-validation)
      (statuses/wrap-status-pages)
      (wrap-file-info)
      (wrap-route-updating)
      (exception/wrap-exceptions)
      (options/wrap-options opts))))

(defn gen-handler 
  "Get a full Noir request handler for use with plugins like lein-ring or lein-beanstalk. 
  If used in a definition, this must come after views have been loaded to ensure that the
  routes have already been added to the route table."
  [& [opts]]
  (init-routes opts))

(defn load-views 
  "Require all the namespaces in the given dir so that the pages are loaded
  by the server."
  [dir]
  (let [nss (find-namespaces-in-dir (file dir))]
    (doseq [n nss]
      (require n))))

(defn add-middleware 
  "Add a middleware function to the noir server. Func is a standard ring middleware
  function, which will be passed the handler. Any extra args to be applied should be
  supplied along with the function."
  [func & args]
  (swap! *middleware* conj [func args]))

(defn start 
  "Create a noir server bound to the specified port with a map of options and return it. 
  The available options are:
  
  :mode - either :dev or :prod
  :ns - the root namepace of your project
  :session-store - an alternate store for session handling"
  [port & [opts]]
  (println "Starting server...")
  (let [server (run-jetty (init-routes opts) {:port port :join? false})]
    (println (str "Server started on port [" port "].")) 
    (println (str "You can view the site at http://localhost:" port))
    server))

(defn stop
  "Stop a noir server"
  [server]
  (.stop server))

(defn restart
  "Restart a noir server"
  [server]
  (stop server)
  (.start server))


