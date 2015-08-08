(ns rksm.cloxp-cljs-repl.test-runner
  (:require [cljs-slimerjs-tester.test-runner :as runner]
            [rksm.cloxp-cljs-repl.eval-test]
            [cljs.core.async :refer [<! >!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn runner []
  (go
   (println
    "Tests done: "
    (<! (runner/runner 'rksm.cloxp-cljs-repl.eval-test)))))
