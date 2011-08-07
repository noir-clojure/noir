(ns noir.util.cljs
  (:use clojure.java.io)
  (:require [noir.options :as options]
            [cljs.closure :as cljsc]))

(def default-opts {:optimizations :simple
                   :output-dir "resources/public/cljs/"
                   :output-to "resources/public/cljs/bootstrap.js"})

(def last-compiled (atom 0))


(defn ext-filter [coll ext]
  (filter (fn [f]
            (let [fname (.getName f)
                  fext (subs fname (inc (.lastIndexOf fname ".")))]
              (and (.isFile f) (= fext ext)))) 
          coll))

(defn find-cljs [dir]
  (let [dir-files (-> dir file file-seq)]
    (ext-filter dir-files "cljs")))

(defn compile-cljs [src-dir]
  (let [files (find-cljs src-dir)
        opts (merge default-opts (options/get :cljsc))]
    (cljsc/build src-dir opts)
    (reset! last-compiled (System/currentTimeMillis))))
            

(defn newer? [f]
  (let [last-modified (.lastModified f)]
    (> last-modified @last-compiled)))

(defn files-updated? [dir]
  (some newer? (find-cljs dir)))

(defn wrap-cljs [handler]
  (let [src-dir "src/"]
    (compile-cljs src-dir)
    (if (options/dev-mode?)
      (fn [req]
        (when (files-updated? src-dir)
          (compile-cljs src-dir))
        (handler req))
      handler)))

