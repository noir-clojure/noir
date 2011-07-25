(defproject noir "1.1.0"
            :description "Noir - a clojure web framework"
            :dependencies [[org.clojure/clojure "1.2.1"]
                           [compojure "0.6.4"]
                           [org.clojure/tools.namespace "0.1.0"]
                           [clj-json "0.3.2"]
                           [ring "0.3.11"]
                           [cssgen "0.2.4"]
                           [hiccup "0.3.6"]
                           [clj-stacktrace "0.2.2"]
                           [ring-reload-modified "0.1.0"]
                           [net.java.dev.jets3t/jets3t "0.8.0"]
                           [org.mindrot/jbcrypt "0.3m"]]
            :dev-dependencies [[marginalia "0.6.0"]
                               [org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]]
            :autodoc {:name "Noir" :page-title "Noir Docs" :load-except-list [#"noir\/content.*"]})
