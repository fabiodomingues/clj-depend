(defproject com.fabiodomingues/clj-depend "0.1.0"
  :description "A Clojure namespace dependency analyzer"
  :url "https://github.com/fabiodomingues/clj-depend"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/test.check "1.1.1"]
                 [prismatic/schema "1.2.0"]
                 [org.clojure/tools.namespace "1.2.0"]]
  :eval-in-leiningen true)
