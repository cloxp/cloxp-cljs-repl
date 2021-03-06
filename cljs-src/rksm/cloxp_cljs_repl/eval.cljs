(ns rksm.cloxp-cljs-repl.eval
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace]]
            [rksm.cloxp-com.messenger :as m]
            [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn log
  [& msgs]
  (apply println (concat ["CLOXP-REPL"] msgs)))

(defn- GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn eval-js
  [code filename line]
  (log "Evaluating" (-> code (replace #"\n" "") (.slice 0 300)) "...")
  (try
    {:status :success :value (js* "eval(~{code})")}
    (catch :default e
      {:status :exception :value (pr-str e)
       :stacktrace (if (.hasOwnProperty e "stack")
                     (.-stack e)
                     "No stacktrace available.")})))

(defn- fetch-js-for-load-file
 [base-url path provides]
 (let [enc-path (js/encodeURIComponent path)
       url (str base-url "cljs-files/" enc-path)]
   (GET url)))

(defn load-js
  [js path provides]
  (let [res (eval-js js path 1)]
    (log "loaded" path ", status: " (:status res))
    res))

(defn load-js-service
  [receiver {{:keys [provides path js]} :data, :as msg}]
  (let [base-url (replace (-> receiver :impl :url) #"^ws([^:]*)://([^/]+).*" "http$1://$2/")]
    (go
     (let [js (or js (<! (fetch-js-for-load-file base-url path provides)))
           result (load-js js path provides)]
       (m/answer receiver msg result false)))))

(defn eval-js-service
  [receiver {{:keys [code filename line]} :data, :as msg}]
  (m/answer receiver msg (eval-js code filename line) false))
