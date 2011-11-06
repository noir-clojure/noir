##Changes for 1.2.1
* BREAKING CHANGE: (url-for) now takes a map of params instead of key-value pairs: (url-for foo {:id 2})
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
