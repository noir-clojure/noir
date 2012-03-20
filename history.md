##Changes for 1.3.0-beta1
* BREAKING CHANGE: flashes now last the length of one non-resource request
* BREAKING CHANGE: switched to the latest hiccup: form-helpers and page-helpers have been split apart. See http://github.com/weavejester/hiccup for more details.
* BREAKING CHANGE: clj-json has been replaced with cheshire.
* BREAKING CHANGE: noir.util.middleware was removed as wrap-utf8 is done by default in ring.
* BREAKING CHANGE: noir.util.s3 has been removed. See https://github.com/weavejester/clj-aws-s3 for a replacement.
* Added noir.session/get! to have a destructive get like the old flashes
* Added noir.server/wrap-route to wrap middleware around specific routes
* Added noir.core/custom-handler* for adding dynamic route functions to the routing table.
* Added noir.util.test/send-request-map for sending full ring maps
* Refactored noir.server so that the jetty dependency can be excluded.
* Refactored noir.response/* so that all functions compose
* Fixed all generated content-types are utf-8

##Changes for 1.2.2
* Added an argless form of (noir.validation/errors?) that returns all errors
* Added the ability to define routes with vars
* Refactored defpage to allow for better errors when a param is passed incorrectly
* Refactored url-for to be more robust
* Fixed s3 var being dynamic
* Fixed an issue with utf-8 routes being encoded incorrectly
* Moved to ring 1.0.1 and compojure 1.0.0
    * Fixes issue with no routes being loaded resulting in a 500
    * Fixes issue with file names containing spaces being unreachable

##Changes for 1.2.1
* BREAKING CHANGE: (url-for) now takes a map of params instead of key-value pairs: (url-for foo {:id 2})
* Changed noir.content.pages to noir.content.getting-started
* Added noir.response/jsonp
* Added :base-url option to noir.server so that you can run noir at different root urls
* Added noir.session/swap! to do atomic updates to the session
* Updated noir.content to be prettier/more informative 
* Fixed pre-route to use ANY by default
* Fixed issue that cause complex pre-routes not work
* Fixed a couple of doc strings to be clearer
* Refactored the way noir.core parses urls for routes to be significantly simpler
* Removed cssgen dependency
* Moved to latest Ring

##Changes for 1.2.0

* Refactored for Clojure 1.3.0 support
* Refactored server to enable custom noir handler creation
* Added url decoding for routes. (defpage "/hey how" ...) will work now.
* Added noir.util.gae to get Noir up on Google App Engine
* Added named routes 
* Added noir.request/ring-request
* Added url-for to query named routes
* Added noir.server/load-view-ns
* Added a :resource-root option to the server
* Added a :cookie-attrs option to the server
* Added post-route
* Added signed cookies
* Added compojure-route and custom-handler to handle integration with other libs
* Changed noir.validation/errors? will now return if any errors exist if no fields are supplied.
* Fixed noir.validation/is-email? to use a better regex
* Fixed and improved noir.util.s3
* Fixed incorrect header setting for noir.response/xml
* Fixed custom middleware preserves order
* Fixed bugs in cookie handling that would cause incorrect retrieval
* Fixed some issues with exceptions to make the 500 page more resilient
* Moved to latest compojure/ring/hiccup
* Added tons of tests


##Changes for 1.1.0

* Added session/flash-put! and sesion/flash-get
* Added alternative session storage via the :session-store server option
* Removed dependency on contrib
* Added defaults for session/get and cookies/get
* Added gen-handler for interop with other ring-based libraries
* Added test utilities under noir.util.test
* Added noir.util.middleware
* Moved to latest compojure/ring/hiccup
* Added server/stop server/restart
* Fixed bug where server/start wasn't returning a server object
