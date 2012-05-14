(defproject noir "1.3.0-beta7"
  :description "Noir - a clojure web framework"
  :url "http://webnoir.org"
  :codox {:exclude [noir.exception noir.content.defaults
                    noir.content.getting-started]}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [compojure "1.0.4"]
                 [bultitude "0.1.5"]
                 [cheshire "4.0.0"]
                 [ring "1.1.0"]
                 [hiccup "1.0.0"]
                 [clj-stacktrace "0.2.4"]
                 [org.mindrot/jbcrypt "0.3m"]])
