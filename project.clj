(defproject cloxp-cljs-repl "0.1.0-SNAPSHOT"
  :description "ClojureScript repl support for cloxp."
  :url "https://github.com/cloxp/cloxp-cljs-repl"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.rksm/cloxp-cljs "0.1.10-SNAPSHOT"]
                 [org.rksm/cloxp-repl "0.1.8-SNAPSHOT"]
                 [org.rksm/cloxp-com "0.1.9-SNAPSHOT"]
                 [org.rksm/cloxp-projects "0.1.10-SNAPSHOT"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.3.3"]]}}
  :source-paths ["src/" "cljs-src/"]
  :test-paths ["test/" "cljs-test/"]
  :scm {:url "git@github.com:cloxp/cloxp-cljs-repl.git"}
  :pom-addition [:developers [:developer
                              [:name "Robert Krahn"]
                              [:url "http://robert.kra.hn"]
                              [:email "robert.krahn@gmail.com"]
                              [:timezone "-9"]]]
  :plugins [[lein-cljsbuild "1.0.6"]]
  :cljsbuild {:builds {:default {:source-paths ["cljs-test" "cljs-src"]}}})
