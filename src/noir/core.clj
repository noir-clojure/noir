(ns noir.core
  "Functions to work with partials and pages."
  (:use hiccup.core
        compojure.core)
  (:require [clojure.string :as string]))

(defonce noir-routes (atom {}))
(defonce route-funcs (atom {}))
(defonce pre-routes (atom (sorted-map)))
(defonce post-routes (atom (list)))

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
  (let [[fn-name remaining] (if (symbol? cur)
                              [cur (rest all)]
                              [nil all])]
        [{:fn-name fn-name} remaining]))

(defn- parse-route [[{:keys [fn-name] :as result} [cur :as all]]]
  (when-not (or (vector? cur) (string? cur))
    (throwf "Routes must either be a string or vector, not a %s" (type cur)))
  (let [[action url] (if (vector? cur)
                       [(keyword->symbol "compojure.core" (first cur)) (second cur)]
                       ['compojure.core/GET cur])
        final (-> result
                (assoc :fn-name (if fn-name
                                  fn-name
                                  (symbol (route->key action url))))
                (assoc :url url)
                (assoc :action action))]
    [final (rest all)]))

(defn- parse-destruct-body [[result [cur :as all]]]
  (when-not (some true? (map #(% cur) [vector? map? symbol?]))
    (throwf "Invalid destructuring param: %s" cur))
  (-> result
    (assoc :destruct cur)
    (assoc :body (rest all))))

(defn ^{:skip-wiki true} parse-args 
  "parses the arguments to defpage. Returns a map containing the keys :name :action :url :destruct :body"
  [args]
  (-> args
    (parse-fn-name)
    (parse-route)
    (parse-destruct-body)))

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
  (let [{fn-name# :fn-name action# :action url# :url destruct# :destruct body# :body} (parse-args args)]
    `(do
       (defn ~fn-name# {::url ~url#
                        ::action (quote ~action#)
                        ::args (quote ~destruct#)} [~destruct#]
         ~@body#)
       (swap! route-funcs assoc ~(keyword fn-name#) ~fn-name#)
       (swap! noir-routes assoc ~(keyword fn-name#) (~action# ~url# {params# :params} (~fn-name# params#))))))

(defmacro defpartial
  "Create a function that returns html using hiccup. The function is callable with the given name."
  [fname params & body]
  `(defn ~fname ~params
     (html
       ~@body)))

(defn ^{:skip-wiki true} route-arguments 
  "returns the list of route arguments in a route"
  [route]
  (->> route
       (re-seq #"/:([^\/]+)")
       (map second)
       (map keyword)))

(defn url-for* [route-fn route-args]
  (let [route-meta (-> route-fn meta)
        url (-> route-meta ::url)
        route-arg-names (route-arguments url)]
    (when (not url)
      (throwf "no url metadata on %s" route-fn))
    (when (not (every? #(contains? route-args %) route-arg-names))
      (throwf "missing route-arg for %s" (first (filter #(not (contains? route-args %)) route-arg-names))))
    (reduce (fn [path [k v]]
              (assert (keyword? k))
              (string/replace path (str k) (str v))) url route-args)))

(defmacro url-for
  "given a named route, i.e. (defpage foo \"/foo/:id\"), returns the url for the
  route. If the route takes arguments, the second argument must be a
  map of route arguments to values

  (url-for foo :id 3) => \"/foo/3\" "
  ([route-fn & {:as arg-map}]
     (let [curr-ns *ns*]
       `(do
          (let [route-fn# (ns-resolve ~curr-ns (quote ~route-fn))]
            (when-not route-fn#
              ((var throwf) "could not find route fn:" ~route-fn))
            (url-for* route-fn# ~arg-map)))))) ;; use ns-resolve to resolve at runtime (rather than compile time), to avoid circular dependencies between views.

(defn render
  "Renders the content for a route by calling the page like a function
  with the given param map. Accepts either '/vals' or [:post '/vals']"
  [route & [params]]
  (if (fn? route)
    (route params)
    (let [[{fn-name :fn-name :as res}] (parse-route [{} [route]])
          func (get @route-funcs (keyword fn-name))]
      (func params))))

(defmacro pre-route
  "Adds a route to the beginning of the route table and passes the entire request
  to be destructured and used in the body. These routes are the only ones to make
  an ordering gaurantee. They will always be in order of ascending specificity (e.g. /* ,
  /admin/* , /admin/user/*) Pre-routes are usually used for filtering, like redirecting
  a section based on privileges:

  (pre-route '/admin/*' {} (when-not (is-admin?) (redirect '/login')))"
  [& args]
  (let [{:keys [action destruct url body]} (parse-args args)]
    `(swap! pre-routes assoc ~url (~action ~url {:as request#} ((fn [~destruct] ~@body) request#)))))

(defmacro post-route
  "Adds a route to the end of the route table and passes the entire request to
   be desctructured and used in the body. These routes are guaranteed to be
   evaluated after those created by defpage and before the generic catch-all and
   resources routes."
  [& args]
  (let [{:keys [action destruct url body]} (parse-args args)]
    `(swap! post-routes conj (~action ~url {:as request#} ((fn [~destruct] ~@body) request#)))))

(defn compojure-route
  "Adds a compojure route fn to the end of the route table. These routes are queried after
   those created by defpage and before the generic catch-all and resources routes.
  
  These are primarily used to integrate generated routes from other libs into Noir."
  [compojure-func]
  (swap! post-routes conj compojure-func))

(defmacro custom-handler
  "Adds a handler to the end of the route table. This is equivalent to writing
  a compojure route using noir's [:method route] syntax.

  (custom-handler [:post \"/login\"] {:as req} (println \"hello \" req))
  => (POST \"/login\" {:as req} (println \"hello\" req))

  These are primarily used to interface with other handler generating libraries, i.e. async alpeh handlers."
  [& args]
  (let [{:keys [action destruct url body]} (parse-args args)]
    `(compojure-route (~action ~url ~destruct ~@body))))
