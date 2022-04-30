(ns clj-depend.clj-depend-integration-test
  (:require [clojure.test :refer :all]
            [clj-depend.core :as clj-depend.core]
            [leiningen.clj-depend :as leiningen]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn project
  [path]
  {:root         (.getPath (io/resource path))
   :source-paths [(.getPath (io/resource (str path "/src")))]})

(defn- call-clj-depend
  [project]
  (let [captured-exit-code (atom nil)]
    (with-redefs [clj-depend.core/exit! (fn [code] (reset! captured-exit-code code))]
      {:output    (-> (leiningen/clj-depend project)
                      (with-out-str)
                      (string/split-lines))
       :exit-code @captured-exit-code})))

(deftest clj-depend-test

  (testing "should exit successfully when there are no violations"
    (is (= {:output    ["Identified 0 violations"]
            :exit-code 0}
           (call-clj-depend (project "without-violations")))))

  (testing "should fail when there is at least one violation"
    (is (= {:output    ["Identified 1 violations"
                        "- \"sample.logic.foo\" depends on \"sample.controller.foo\""]
            :exit-code 1}
           (call-clj-depend (project "with-violations")))))

  (testing "should fail when a cyclic dependency is identified"
    (is (= {:output    ["Circular dependency between \"sample.logic.foo\" and \"sample.controller.foo\""]
            :exit-code 2}
           (call-clj-depend (project "with-cyclic-dependency"))))))