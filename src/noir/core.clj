(ns noir.core
  "Functions to work with partials and pages."
  (:use hiccup.core
        compojure.core)
  (:require [clojure.string :as string]
            [clojure.tools.macro :as macro]))

(defonce noir-routes (atom {}))
(defonce route-funcs (atom {}))
(defonce pre-routes (atom (sorted-map)))
(defonce post-routes (atom []))
(defonce compojure-routes (atom []))

(defn- keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (name kw))))

(defn- route->key [action rte]
  (let [action (string/replace (str action) #".*/" "")]
    (str action (-> rte
                    (string/replace #"\." "!dot!")
                    (string/replace #"/" "--")
                    (string/replace #":" ">")
                    (string/replace #"\*" "<")))))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(defn- parse-fn-name [[cur :as all]]
  (let [[fn-name remaining] (if (and (symbol? cur)
                                     (or (@route-funcs (keyword (name cur)))
                                         (not (resolve cur))))
                              [cur (rest all)]
                              [nil all])]
    [{:fn-name fn-name} remaining]))

(defn- parse-route [[{:keys [fn-name] :as result} [cur :as all]] default-action]
  (let [cur (if (symbol? cur)
              (try
                (deref (resolve cur))
                (catch Exception e
                  (throwf "Symbol given for route has no value")))
              cur)]
    (when-not (or (vector? cur) (string? cur))
      (throwf "Routes must either be a string or vector, not a %s" (type cur)))
    (let [[action url] (if (vector? cur)
                         [(keyword->symbol "compojure.core" (first cur)) (second cur)]
                         [default-action cur])
          final (-> result
                    (assoc :fn-name (if fn-name
                                      fn-name
                                      (symbol (route->key action url))))
                    (assoc :url url)
                    (assoc :action action))]
      [final (rest all)])))

(defn- parse-destruct-body [[result [cur :as all]]]
  (when-not (some true? (map #(% cur) [vector? map? symbol?]))
    (throwf "Invalid destructuring param: %s" cur))
  (-> result
      (assoc :destruct cur)
      (assoc :body (rest all))))

(defn ^{:skip-wiki true} parse-args
  "parses the arguments to defpage. Returns a map containing the keys :fn-name :action :url :destruct :body"
  [args & [default-action]]
  (-> args
      (parse-fn-name)
      (parse-route (or default-action 'compojure.core/GET))
      (parse-destruct-body)))

(defn ^{:skip-wiki true} route->name
  "Parses a set of route args into the keyword name for the route"
  [route]
  (cond
    (keyword? route) route
    (fn? route) (keyword (:name (meta route)))
    :else (let [res (first (parse-route [{} [route]] 'compojure.core/GET))]
            (keyword (:fn-name res)))))

(defmacro defpage
  "Adds a route to the server whose content is the the result of evaluating the body.
  The function created is passed the params of the request and the destruct param allows
  you to destructure that meaningfully for use in the body.

  There are several supported forms:

  (defpage \"/foo/:id\" {id :id})  an unnamed route
  (defpage [:post \"/foo/:id\"] {id :id}) a route that responds to POST
  (defpage foo \"/foo:id\" {id :id}) a named route
  (defpage foo [:post \"/foo/:id\"] {id :id})

  The default method is GET."
  [& args]
  (let [{:keys [fn-name action url destruct body]} (parse-args args)]
    `(do
       (defn ~fn-name {::url ~url
                       ::action (quote ~action)
                       ::args (quote ~destruct)} [~destruct]
         ~@body)
       (swap! route-funcs assoc ~(keyword fn-name) ~fn-name)
       (swap! noir-routes assoc ~(keyword fn-name) (~action ~url {params# :params} (~fn-name params#))))))

(defmacro defpartial
  "Create a function that returns html using hiccup. The function is callable with the given name. Can optionally include a docstring or metadata map, like a normal function declaration."
  [fname & args]
  (let [[fname args] (macro/name-with-attributes fname args)
        [params & body] args]
    `(defn ~fname ~params
       (html
        ~@body))))

(defn ^{:skip-wiki true} route-arguments
  "returns the list of route arguments in a route"
  [route]
  (let [args (re-seq #"/(:([^\/]+)|\*)" route)]
    (set (map #(keyword (or (nth % 2) (second %))) args))))

(defn url-for* [url route-args]
  (let [url (if (vector? url) ;;handle complex routes
              (first url)
              url)
        route-arg-names (route-arguments url)]
    (when-not (every? (set (keys route-args)) route-arg-names)
      (throwf "Missing route-args %s" (vec (filter #(not (contains? route-args %)) route-arg-names))))
    (reduce (fn [path [k v]]
              (if (= k :*)
                (string/replace path "*" (str v))
                (string/replace path (str k) (str v))))
            url
            route-args)))

(defn url-for-fn* [route-fn route-args]
  (let [url (-> route-fn meta ::url)]
    (when-not url
      (throwf "No url metadata on %s" route-fn))
    (url-for* url route-args)))

(defmacro url-for
  "Given a named route, e.g. (defpage foo \"/foo/:id\"), returns the url for the
  route. If the route takes arguments, the second argument must be a
  map of route arguments to values

  (url-for foo {:id 3}) => \"/foo/3\" "
  ([route & [arg-map]]
   (let [cur-ns *ns*
         route (if (symbol? route)
                 `(ns-resolve ~cur-ns (quote ~route))
                 `(delay ~route))]
   `(let [var# ~route]
      (cond
        (string? @var#) (url-for* @var# ~arg-map)
        (vector? @var#) (url-for* (second @var#) ~arg-map)
        (fn? @var#) (url-for-fn* var# ~arg-map)
        :else (throw (Exception. (str "Unknown route type: " @var#))))))))

(defn render
  "Renders the content for a route by calling the page like a function
  with the given param map. Accepts either '/vals' or [:post '/vals']"
  [route & [params]]
  (if (fn? route)
    (route params)
    (let [rname (route->name route)
          func (get @route-funcs rname)]
      (func params))))

(defmacro pre-route
  "Adds a route to the beginning of the route table and passes the entire request
  to be destructured and used in the body. These routes are the only ones to make
  an ordering guarantee. They will always be in order of ascending specificity (e.g. /* ,
  /admin/* , /admin/user/*) Pre-routes are usually used for filtering, like redirecting
  a section based on privileges:

  (pre-route '/admin/*' {} (when-not (is-admin?) (redirect '/login')))"
  [& args]
  (let [{:keys [action destruct url body]} (parse-args args 'compojure.core/ANY)
        safe-url (if (vector? url)
                   (first url)
                   url)]
    `(swap! pre-routes assoc ~safe-url (~action ~url {:as request#} ((fn [~destruct] ~@body) request#)))))

(defmacro post-route
  "Adds a route to the end of the route table and passes the entire request to
  be destructured and used in the body. These routes are guaranteed to be
  evaluated after those created by defpage and before the generic catch-all and
  resources routes."
  [& args]
  (let [{:keys [action destruct url body fn-name]} (parse-args args)]
    `(swap! post-routes conj [~(keyword fn-name) (~action ~url {:as request#} ((fn [~destruct] ~@body) request#))])))

(defn compojure-route
  "Adds a compojure route fn to the end of the route table. These routes are queried after
  those created by defpage and before the generic catch-all and resources routes.

  These are primarily used to integrate generated routes from other libs into Noir."
  [compojure-func]
  (swap! compojure-routes conj compojure-func))

(defmacro custom-handler
  "Adds a handler to the end of the route table. This is equivalent to writing
  a compojure route using noir's [:method route] syntax.

  (custom-handler [:post \"/login\"] {:as req} (println \"hello \" req))
  => (POST \"/login\" {:as req} (println \"hello\" req))

  These are primarily used to interface with other handler generating libraries, i.e. async aleph handlers."
  [& args]
  (let [{:keys [action destruct url body]} (parse-args args)]
    `(compojure-route (~action ~url ~destruct ~@body))))

(defn custom-handler*
  "Adds a handler to the end of the route table. This is equivalent to writing
  a compojure route using noir's [:method route] syntax, but allows functions
  to be created dynamically:

  (custom-handler* [:post \"/login\"] (fn [params] (println params)))

  These are primarily used to interface with other dynamic handler generating libraries"
  [route func]
  (let [[{:keys [action url fn-name]}] (parse-route [{} [route]] 'compojure.core/GET)
        fn-key (keyword fn-name)]
    (swap! route-funcs assoc fn-key func)
    (swap! noir-routes assoc fn-key (eval `(~action ~url {params# :params} (~func params#))))))
