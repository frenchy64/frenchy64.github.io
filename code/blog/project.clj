(defproject blog "0.1.0-SNAPSHOT"
  :description "Ambrose's blog"
  :url "http://frenchy64.github.io"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.typed "0.1.24"]]
  :profiles {:dev {:repl-options {:port 64406}}})
