(ns noir.server.handler
  "Handler generation functions used by noir.server and other ring handler libraries."
  (:use [compojure.core :only [routes ANY]]
        ring.middleware.reload-modified)
  (:import java.net.URLDecoder)
  (:require [compojure.route :as c-route]
            [hiccup.core :as hiccup]
            [hiccup.middleware :as hiccup-middleware]
            [noir.core :as noir]
            [noir.content.defaults :as defaults]
            [noir.cookies :as cookie]
            [noir.exception :as exception]
            [noir.request :as request]
            [noir.statuses :as statuses]
            [noir.options :as options]
            [noir.session :as session]
            [noir.validation :as validation]))

(defonce middleware (atom []))

(defn- spec-routes []
  [(c-route/resources "/" {:root (options/get :resource-root "public")})
   (ANY "*" [] {:status 404 :body nil})])

(defn- wrap-route-updating [handler]
  (if (options/dev-mode?)
    (wrap-reload-modified handler ["src"])
    handler))

(defn- wrap-base-url [handler]
  (let [url (options/get :base-url)]
    (hiccup-middleware/wrap-base-url handler url)))

(defn- wrap-custom-middleware [handler]
  (reduce (fn [cur [func args]] (apply func cur args))
          handler
          (seq @middleware)))

(defn- pack-routes []
  (apply routes (concat (vals @noir/pre-routes) (vals @noir/noir-routes) @noir/post-routes (spec-routes))))

(defn- init-routes [opts]
  (binding [options/*options* (options/compile-options opts)]
    (->
      (if (options/dev-mode?)
        (fn [request]
          ;; by doing this as a function we can ensure that any routes added as the
          ;; result of a modification are evaluated on the first reload.
          ((pack-routes) request))
        (pack-routes))
      (request/wrap-request-map)
      (wrap-custom-middleware))))

(defn add-custom-middleware
  "Add a middleware function to all noir handlers."
  [func & args]
  (swap! middleware conj [func args]))

(defn wrap-noir-middleware
  "Wrap a base handler in all of noir's middleware"
  [handler opts]
  (binding [options/*options* (options/compile-options opts)]
    (->
      handler
      (wrap-base-url)
      (session/wrap-noir-session)
      (cookie/wrap-noir-cookies)
      (validation/wrap-noir-validation)
      (statuses/wrap-status-pages)
      (wrap-route-updating)
      (exception/wrap-exceptions)
      (options/wrap-options opts))))

(defn base-handler
  "Get the most basic Noir request handler, only adding wrap-custom-middleware and wrap-request-map."
  [& [opts]]
  (init-routes opts))
