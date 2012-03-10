(ns noir.server
  "A collection of functions to handle Noir's server and add middleware to the stack."
  (:use compojure.core
        [clojure.java.io :only [file]]
        [clojure.tools.namespace :only [find-namespaces-in-dir find-namespaces-on-classpath]]
        [ring.middleware.multipart-params])
  (:require [compojure.handler :as compojure]
            [noir.server.handler :as handler]))

(defn gen-handler
  "Get a full Noir request handler for use with plugins like lein-ring or lein-beanstalk.
  If used in a definition, this must come after views have been loaded to ensure that the
  routes have already been added to the route table."
  [& [opts]]
  (-> (handler/base-handler opts)
      (handler/wrap-noir-middleware opts)
      (handler/wrap-spec-routes opts)
      (compojure/api)
      (wrap-multipart-params)))

(defn load-views
  "Require all the namespaces in the given dir so that the pages are loaded
  by the server."
  [& dirs]
  (doseq [dir dirs
          n (find-namespaces-in-dir (file dir))]
    (require n)))

(defn load-views-ns
  "Require all the namespaces prefixed by the namespace symbol given so that the pages
  are loaded by the server."
  [& ns-syms]
  (doseq [ns-sym ns-syms
          n (find-namespaces-on-classpath)
          :let [pattern (re-pattern (name ns-sym))]
          :when (re-seq pattern (name n))]
    (require n)))

(defn add-middleware
  "Add a middleware function to the noir server. Func is a standard ring middleware
  function, which will be passed the handler. Any extra args to be applied should be
  supplied along with the function."
  [func & args]
  (apply handler/add-custom-middleware func args))

(defn wrap-route
  "Add a middleware function to a specific route. Route is a standard route you would
  use for defpage, func is a ring middleware function, and args are any additional args
  to pass to the middleware function. You can wrap the resources and catch-all routes by
  supplying the routes :resources and :catch-all respectively:

  (wrap-route :resources some-caching-middleware)"
  [route middleware & args]
  (apply handler/wrap-route route middleware args))

(defn start
  "Create a noir server bound to the specified port with a map of options and return it.
  The available options are:

  :mode - either :dev or :prod
  :ns - the root namepace of your project
  :jetty-options - any extra options you want to send to jetty like :ssl?
  :base-url - the root url to prepend to generated links and resources
  :resource-options - a map of options for the resources route (:root or :mime-types)
  :session-store - an alternate store for session handling
  :session-cookie-attrs - custom session cookie attributes"
  [port & [opts]]
  ;; to allow for jetty to be excluded as a dependency, it is included
  ;; here inline.
  (require 'ring.adapter.jetty)
  (println "Starting server...")
  (let [run-fn (resolve 'ring.adapter.jetty/run-jetty) ;; force runtime resolution of jetty
        jetty-opts (merge {:port port :join? false} (:jetty-options opts))
        server (run-fn (gen-handler opts) jetty-opts)]
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
