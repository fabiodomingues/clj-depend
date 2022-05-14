(defproject com.fabiodomingues/clj-depend "0.5.1"
  :description "A Clojure namespace dependency analyzer"
  :url "https://github.com/clj-depend/clj-depend"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]
                   :resource-paths ["test-resources"]}}
  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [org.clojure/tools.namespace "1.2.0"]
                 [org.clojure/tools.cli "1.0.206"]]
  :scm {:name "git" :url "https://github.com/clj-depend/clj-depend"})
