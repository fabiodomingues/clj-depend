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

(def project-without-violations (project "without-violations"))
(def project-without-violations-expected-result {:output    ["Identified 0 violations"]
                                                 :exit-code 0})

(def project-with-violations (project "with-violations"))
(def project-with-violations-expected-result {:output    ["Identified 1 violations"
                                                          "- \"sample.logic.foo\" depends on \"sample.controller.foo\""]
                                              :exit-code 1})

(def project-with-cyclic-dependency (project "with-cyclic-dependency"))
(def project-with-cyclic-dependency-expected-result {:output    ["Circular dependency between \"sample.logic.foo\" and \"sample.controller.foo\""]
                                                     :exit-code 2})

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
    (let [result (call-clj-depend project-without-violations)]
      (is (= project-without-violations-expected-result
             result))))

  (testing "should fail when there is at least one violation"
    (let [result (call-clj-depend project-with-violations)]
      (is (= project-with-violations-expected-result
             result))))

  (testing "should fail when a cyclic dependency is identified"
    (let [result (call-clj-depend project-with-cyclic-dependency)]
        (is (= project-with-cyclic-dependency-expected-result
               result)))))