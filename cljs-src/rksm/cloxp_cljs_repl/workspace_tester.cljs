(ns rksm.cloxp-cljs-repl.workspace-tester
  (:require [rksm.cloxp-com.cloxp-client :as cloxp]
            [rksm.cloxp-com.messenger :as messenger]
            [rksm.cloxp-cljs-repl.eval :as repl]))

; (cloxp/start )
(cloxp/start
 :host "localhost" :port 8084
 :then-do (fn [con]
            (try 
              (do
                (messenger/add-service con "eval-js" repl/eval-js-service)
                (messenger/add-service con "load-js" repl/load-js-service)
                (println "added or what???"))
              (catch js/Error e (println e)))))
