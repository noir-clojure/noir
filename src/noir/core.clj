(ns noir.core
  "Functions to work with partials and pages."
  (:use hiccup.core
        compojure.core)
  (:use [clojure.contrib.except :only (throwf)])
  (:require [clojure.string :as str])
  (:require [clojure.string :as string]
            [compojure.route :as c-route]))

(defonce noir-routes (atom {}))
(defonce route-funcs (atom {}))
(defonce pre-routes (atom (sorted-map)))
(defonce spec-routes [(c-route/resources "/")
                        (ANY "*" [] {:status 404 :body nil})])

(defn- keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (name kw))))

(defn- route->key [action rte]
  (let [action (string/replace (str action) #".*/" "")]
    (str action (-> rte
                  (string/replace #"\." "!dot!")
                  (string/replace #"/" "--")
                  (string/replace #":" ">")
                  (string/replace #"\*" "<")))))

;; (defpage "/foo/:id" {})
;; (defpage [:get "/foo/:id"] {})
;; (defpage foo "/foo/:id" {})
;; (defpage foo [:post "/foo/:id"])

;; this would be a good candidate for match, once it's ready (https://github.com/swannodette/match)

(defn parse-args
  "parses the arguments to defpage. Returns a map containing the keys :name :action :url :destruct :body"
  [args]
  (let [m (if (symbol? (first args))
            {:fn-name (first args)}
            {})
        args (if (symbol? (first args))
               (rest args)
               args)
        m (merge m (if (vector? (first args))
                     (let [[action url] (first args)]
                       {:action (keyword->symbol "compojure.core" action)
                        :url url})
                     {:action 'compojure.core/GET
                      :url (first args)}))
        m (if (:fn-name m)
            m
            (assoc m :fn-name (symbol (route->key (-> m :action) (-> m :url)))))
        args (rest args)
        destruct (first args)
        m (assoc m :destruct destruct)
        args (rest args)
        body args
        m (assoc m :body body)]
    m))

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

(defn route-arguments
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
              (str/replace path (str k) (str v))) url route-args)))

(defmacro url-for
  "given a named route, i.e. (url-for foo), where foo is a named
  route, i.e.  (defpage foo \"/foo/:id\"), returns the url for the
  route. If the route takes arguments, the second argument must be a
  map of route arguments to values

  (url-for foo :id 3) => \"/foo/3\" "
  ([route-fn & {:as arg-map}]
    (let [curr-ns *ns*]
      `(url-for* (ns-resolve ~curr-ns (quote ~route-fn)) ~arg-map)))) ;; use ns-resolve to resolve at runtime (rather than compile time), to avoid circular dependencies between views.

(defn render 
  "Renders the content for a route by calling the page like a function
  with the given param map. Accepts either '/vals' or [:post '/vals']"
  [route & [params]]
  (let [{fn-name :route-fn} (parse-args route)
        func (get @route-funcs (keyword fn-name))]
    (func params)))

(defmacro pre-route 
  "Adds a route to the beginning of the route table and passes the entire request
  to be destructured and used in the body. These routes are the only ones to make
  an ordering gaurantee. They will always be in order of ascending specificity (e.g. /* , 
  /admin/* , /admin/user/*) Pre-routes are usually used for filtering, like redirecting 
  a section based on privileges:

  (pre-route '/admin/*' {} (when-not (is-admin?) (redirect '/login')))"
  [route destruct & body]
  (let [{action# :action url# :url} (parse-args route)]
    `(swap! pre-routes assoc ~url# (~action# ~url# {:as request#} ((fn [~destruct] ~@body) request#)))))
