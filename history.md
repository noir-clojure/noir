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
