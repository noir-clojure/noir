(defproject noir "1.3.1"
  :description "Noir - a clojure web framework"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "http://webnoir.org"
  :codox {:exclude [noir.exception noir.content.defaults
                    noir.content.getting-started]}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [lib-noir "0.2.0"] 
                 [compojure "1.1.5"]
                 [bultitude "0.2.0"]
                 [ring "1.1.8"]
                 [hiccup "1.0.2"]
                 [clj-stacktrace "0.2.5"]
                 [org.clojure/tools.macro "0.1.1"]])
