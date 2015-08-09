(ns rksm.cloxp-cljs-repl.core
  (:require [cljs.repl :as repl]
            [cljs.analyzer :as ana]
            [cljs.env :as env]
            [cljs.tagged-literals :as tags]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]
            [rksm.cloxp-repl :as cloxp-repl]
            [rksm.cloxp-cljs.analyzer :as cloxp-ana]
            [rksm.cloxp-com.server :as server]
            [rksm.cloxp-com.messenger :as msg]
            [clojure.core.async :refer [>!! <!! >! <! go]]))

(defn send-to-js
  [server client-id action data]
  (let [msg {:target client-id,
             :action action,
             :data data}]
    (if-let [result (->> msg (msg/send server) <!! :message :data)]
      (update-in result [:status] keyword)
      {:status :error :value (str "Invalid msg " msg)})))

(defrecord CloxpCljsReplEnv [server client-id]
  repl/IJavaScriptEnv
  (-setup [this opts]
          #_(eval-cljs (println "connected!") this))
  (-evaluate [this filename line js]
             (send-to-js server client-id
                         "eval-js" {:code js :filename filename :line line}))
  (-load [this provides uri]
         (let [[host port] ((juxt server/host server/port) server)]
           (send-to-js server client-id
                      "load-js" {:provides provides :path (str uri)
                                 :host host :port port})))
  (-tear-down [_]))

(defn default-repl-env
  []
  (if-let [{:keys [server id] :as connection} (first (server/all-connections))]
    (->CloxpCljsReplEnv server id)
    (throw (Exception. "No cljs-repl connection found"))))

(defn repl-env-for-client
  [client-id]
  (if-let [{:keys [server] :as con} (server/find-connection client-id)]
    (->CloxpCljsReplEnv server client-id)
    (throw (Exception. (str "No cljs-repl connection for client " client-id " found")))))

; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

(defn read-cljs-string
  "opts: {:file :line-offset :column-offset :features}"
  [cljs-string & [cljs-ns opts]]
  (let [opts (if (contains? opts :features)
               opts (assoc opts :features #{:cljs}))]
    (binding [*ns* (create-ns (or cljs-ns ana/*cljs-ns*))
              reader/*data-readers* tags/*cljs-data-readers*
              reader/*alias-map* (apply merge
                                   ((juxt :requires :require-macros)
                                    (cloxp-ana/with-compiler
                                      (ana/get-namespace (ns-name *ns*)))))]
      (cloxp-repl/read-clj-string cljs-string *ns* opts))))

(defn eval-cljs
  "returns map of {:value :out :error}"
  ([form opts]
   (eval-cljs form nil opts))
  ([form cloxp-repl-env {:keys [target-id ns-sym] :as opts}]
   (let [opts (or opts {})
         cloxp-repl-env (cond
                          cloxp-repl-env cloxp-repl-env
                          target-id (repl-env-for-client target-id)
                          :default (default-repl-env))
         c-env (or env/*compiler* (env/default-compiler-env))
         ana-env (merge (ana/empty-env) {:ns 'cljs.user})]
     (env/with-compiler-env c-env
       (try
         {:error nil
          :out ""
          :value (repl/evaluate-form cloxp-repl-env
                                     (assoc ana-env :ns (ana/get-namespace
                                                         (or ns-sym ana/*cljs-ns*)))
                                     "<cloxp-cljs-repl>"
                                     form
                                     identity ; wrap-fn
                                     opts)}
         (catch Exception e {:error e
                             :out ""
                             :value nil}))))))

(defn eval-cljs-string
  ([expr opts]
   (eval-cljs-string expr nil opts))
  ([expr cloxp-repl-env {:keys [ns-sym file line-offset column-offset] :or {file (or *file* "NO_SOURCE_FILE")} :as opts}]
   (->> (read-cljs-string expr ns-sym
                          {:features #{:cljs}
                           :line-offset (or line-offset cloxp-repl/*line-offset*)
                           :column-offset (or column-offset cloxp-repl/*column-offset*)})
     (map #(eval-read-obj cloxp-repl-env % (merge opts {:file file})))
     doall)))

(defn- eval-read-obj
  "returns a map of :parsed :value :out and :error."
  [cloxp-repl-env
   {:keys [form] :as parsed}
   & [{:keys [ns-sym line-offset throw-errors?]
       :or {line-offset 0, throw-errors? false}
       :as opts}]]
  (let [add-to-meta {} ;(select-keys parsed [:line :column :source])
        ; (eval-cljs (read-cljs-string expr ns-sym opts) cloxp-repl-env opts)
        {:keys [error] :as result} (eval-cljs form cloxp-repl-env
                                              (update-in opts [:add-meta] merge add-to-meta))]
    (if (and error throw-errors?)
      (throw error)
      (assoc result :parsed parsed))))

(defn load-namespace
  ([sym opts]
   (load-namespace sym (default-repl-env) (or opts {})))
  ([sym cloxp-repl-env opts]
   sym
   (let [c-env (env/default-compiler-env)
         ana-env (merge (ana/empty-env) {:ns 'cljs.user})]
     (env/with-compiler-env c-env
       (cljs.repl/load-namespace cloxp-repl-env sym opts)))))
