(defproject sample "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[com.fabiodomingues/lein-clj-depend "0.11.1"]]
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns sample.core})
