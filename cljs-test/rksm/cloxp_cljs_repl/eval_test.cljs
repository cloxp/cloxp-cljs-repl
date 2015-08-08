(ns rksm.cloxp-cljs-repl.eval-test
  (:require [rksm.cloxp-com.net :as net]
            [rksm.cloxp-com.messenger :as m]
            [rksm.cloxp-com.async-util :as util]
            [cljs.core.async :refer [<! >! put! close! chan pub sub timeout]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [cemerick.cljs.test :refer [is deftest testing use-fixtures done]]))

(def port 8084)

(def url (str "ws://localhost:" port "/ws"))

(defn net-cleanup [t]
  (t)
  (net/remove-all-connections))

(use-fixtures :each net-cleanup)

; -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

#_(deftest ^:async foo
  (go
   (is true)
   (done)
   #_(let [{id :id, :as c} (<! (net/connect url))]
     (is (string? id))
     (go
      (let [{:keys [message error] :as answer}
            (<! (m/send c {:action "echo", :data "test"}))]
        (is (nil? error) (str error))
        (is (= (:data message) "test"))
        (net/remove-all-connections) ; FIXME why does fixure not work???
        (done))))))

(deftest ^:async eval-test
  (let [{id :id, :as c} (<! (net/connect url))]
    (go
     (let [result (<! (util/join (m/send c {:action "eval" :data {:expr "(+ 1 2)"}})))]
       (is (= [1 2 3] (map (comp :data :message) result)))
       (done)))))
