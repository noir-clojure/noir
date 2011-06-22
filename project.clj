(defproject noir "1.0.0"
            :description "Noir - a clojure web framework"
            :dependencies [[org.clojure/clojure "1.2.1"]
                           [org.clojure/clojure-contrib "1.2.0"]
                           [compojure "0.6.2"]
                           [ring "0.3.7"]
                           [cssgen "0.2.4"]
                           [hiccup "0.3.5"]
                           [clj-stacktrace "0.2.2"]
                           [ring-reload-modified "0.1.0"]
                           [net.java.dev.jets3t/jets3t "0.8.0"]
                           [org.mindrot/jbcrypt "0.3m"]]
            :dev-dependencies [[marginalia "0.5.1"]
                               [org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]
                               [swank-clojure "1.2.1"]]
            :main noir.example)
