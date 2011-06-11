(defproject noir "0.3.3"
            :description "Noir - a clojure web framework"
            :dependencies [[org.clojure/clojure "1.2.1"]
                           [org.clojure/clojure-contrib "1.2.0"]
                           [compojure "0.6.2"]
                           [ring "0.3.7"]
                           [cssgen "0.2.4"]
                           [hiccup "0.3.5"]
                           [clj-stacktrace "0.2.2"]
                           [ring-reload-modified "0.1.0"]
                           [net.java.dev.jets3t/jets3t "0.8.0"]]
            :dev-dependencies [[lein-clojars "0.6.0"]]
            :main noir.example)
