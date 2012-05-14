(ns noir.server.handler
  "Handler generation functions used by noir.server and other ring handler libraries."
  (:use [compojure.core :only [routes ANY]]
        ring.middleware.reload
        ring.middleware.flash)
  (:import java.net.URLDecoder)
  (:require [compojure.route :as c-route]
            [hiccup.middleware :as hiccup]
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
(defonce wrappers (atom []))

;;***************************************************
;; Wrappers
;;***************************************************

(defn wrappers-for [& urls]
  (let [url-set (set urls)]
    (group-by :url (filter #(url-set (:url %)) @wrappers))))

(defn all-wrappers []
  (group-by :url @wrappers))

(defn wrappers->fn [wrapped]
  (let [wrapped (if (coll? (first wrapped))
                  wrapped
                  [wrapped])]
    (apply comp (map :func (reverse wrapped)))))

(defn try-wrap [ws route]
  (if ws
    (let [func (wrappers->fn ws)]
      (func route))
    route))

(defn add-route-middleware [rts]
  (let [ws (all-wrappers)]
    (for [[route-name route] rts]
      (try-wrap (ws route-name) route))))

(defn wrap-route [url func & params]
  (swap! wrappers conj {:url (noir/route->name url) :func #(apply func % params)}))

;;***************************************************
;; Other middleware
;;***************************************************

(defn- wrap-route-updating [handler]
  (if (options/dev-mode?)
    (wrap-reload handler ["src"])
    handler))

(defn- wrap-custom-middleware [handler]
  (reduce (fn [cur [func args]] (apply func cur args))
          handler
          (seq @middleware)))

;;***************************************************
;; Route packing
;;***************************************************

(defn- spec-routes []
  (let [ws (wrappers-for :resources :catch-all)
        resource-opts (merge {:root "public"} (options/get :resource-options {}))
        resources (c-route/resources "/" resource-opts)
        catch-all (ANY "*" [] {:status 404 :body nil})]
    [(try-wrap (:resources ws) resources)
     (try-wrap (:catch-all ws) catch-all)]))

(defn- pack-routes []
  (apply routes (concat (add-route-middleware @noir/pre-routes)
                        (add-route-middleware @noir/noir-routes)
                        (add-route-middleware @noir/post-routes)
                        @noir/compojure-routes)))

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
    (-> handler
        (hiccup/wrap-base-url (options/get :base-url))
        (session/wrap-noir-flash)
        (session/wrap-noir-session)
        (cookie/wrap-noir-cookies)
        (validation/wrap-noir-validation)
        (statuses/wrap-status-pages)
        (wrap-route-updating)
        (exception/wrap-exceptions)
        (options/wrap-options opts))))

;; We want to not wrap these particular routes in session and flash middleware.
(defn wrap-spec-routes
  "Wrap a handler in noir's resource and catch-all routes."
  [handler opts]
  (routes handler
          (-> (apply routes (spec-routes))
              (cookie/wrap-noir-cookies)
              (validation/wrap-noir-validation)
              (hiccup/wrap-base-url (options/get :base-url))
              (statuses/wrap-status-pages)
              (exception/wrap-exceptions)
              (options/wrap-options opts))))

(defn base-handler
  "Get the most basic Noir request handler, only adding wrap-custom-middleware and wrap-request-map."
  [& [opts]]
  (init-routes opts))
