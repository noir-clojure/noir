(ns noir.server
  "A collection of functions to handle Noir's server and add middleware to the stack."
  (:use compojure.core
        [clojure.java.io :only [file]]
        [clojure.tools.namespace :only [find-namespaces-in-dir find-namespaces-on-classpath]])
  (:require [compojure.handler :as compojure]
            [ring.adapter.jetty :as jetty]
            [noir.server.handler :as handler]))

(defn gen-handler 
  "Get a full Noir request handler for use with plugins like lein-ring or lein-beanstalk. 
  If used in a definition, this must come after views have been loaded to ensure that the
  routes have already been added to the route table."
  [& [opts]]
  (-> (handler/base-handler opts)
    (compojure/site)
    (handler/wrap-noir-middleware opts)))

(defn load-views 
  "Require all the namespaces in the given dir so that the pages are loaded
  by the server."
  [& dirs]
  (doseq [dir dirs
          n (find-namespaces-in-dir (file dir))]
    (require n)))

(defn load-views-ns
  "Require all the namespaces prefixed by the namespace symbold given so that the pages 
  are loaded by the server."
  [& ns-syms]
  (doseq [ns-sym ns-syms
          n (filter #(re-seq (re-pattern (name ns-sym)) (name %))
                    (find-namespaces-on-classpath))]
    (require n)))

(defn add-middleware 
  "Add a middleware function to the noir server. Func is a standard ring middleware
  function, which will be passed the handler. Any extra args to be applied should be
  supplied along with the function."
  [func & args]
  (swap! handler/middleware conj [func args]))

(defn start 
  "Create a noir server bound to the specified port with a map of options and return it. 
  The available options are:
  
  :mode - either :dev or :prod
  :ns - the root namepace of your project
  :session-store - an alternate store for session handling"
  [port & [opts]]
  (println "Starting server...")
  (let [server (jetty/run-jetty (gen-handler opts) {:port port :join? false})]
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


