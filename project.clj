(defproject noir "1.4.0-beta1"
  :description "Noir - a clojure web framework"
  :url "http://webnoir.org"
  :codox {:exclude [noir.exception noir.content.defaults
                    noir.content.getting-started]}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [lib-noir "0.1.1"]
                 [compojure "1.1.3"]
                 [bultitude "0.1.5"]
                 [ring "1.1.3"]
                 [hiccup "1.0.0"]
                 [clj-stacktrace "0.2.4"]
                 [org.clojure/tools.macro "0.1.1"]])
