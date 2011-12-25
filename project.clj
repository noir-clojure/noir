(defproject noir "1.2.2"
            :description "Noir - a clojure web framework"
            :dependencies [[org.clojure/clojure "[1.2.1],[1.3.0]"]
                           [compojure "1.0.0"]
                           [org.clojure/tools.namespace "0.1.0"]
                           [clj-json "0.4.3"]
                           [ring "1.0.1"]
                           [hiccup "0.3.7"]
                           [clj-stacktrace "0.2.3"]
                           [ring-reload-modified "0.1.1"]
                           [net.java.dev.jets3t/jets3t "0.8.1"]
                           [org.mindrot/jbcrypt "0.3m"]]
            :dev-dependencies [[org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]]
            :autodoc {:name "Noir" :page-title "Noir Docs" :load-except-list [#"noir\/content.*"]})
