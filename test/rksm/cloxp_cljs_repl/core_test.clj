(ns rksm.cloxp-cljs-repl.core-test
  (:require [rksm.cloxp-cljs-repl.core :refer :all]
            [clojure.core.async :as async :refer [go go-loop <!! >!! thread]]
            [rksm.cloxp-projects.lein :as lein]
            [rksm.cloxp-com.server :as server]
            [clojure.test :refer :all]))

; (def ^:dynamic *server*)

; (defn fixture [test]
;   (let [server (server/ensure-server! :port 8084)]
;     (try 
;       (test)
;       (finally (server/stop-server! server)))))

; (use-fixtures :each fixture)

; (defn run-cljs-tests
;   []
;   (subp/async-proc "slimerjs" "public/slimerjs-test.js"))

; (deftest all-cljs-tests
;   (let [proc (run-cljs-tests)
;         _ (subp/wait-for proc)
;         code (subp/exit-code proc)
;         out (subp/stdout proc)
;         [_ test pass fail error]
;         (re-find #":test ([0-9]+), :pass ([0-9]+), :fail ([0-9]+), :error ([0-9]+)" out)
;         [test pass fail error] (->> [test pass fail error]
;                                 (map #(or % "nil"))
;                                 (map read-string))]
;     (println [test pass fail error])
;     (is (= 0 code))
;     (is (= 0 fail))
;     (is (= 0 error))))

; (comment
; (test-ns *ns*)
; )
