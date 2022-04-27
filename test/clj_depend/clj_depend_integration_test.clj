(ns clj-depend.clj-depend-integration-test
  (:require [clojure.test :refer :all]
            [clj-depend.core :as clj-depend.core]
            [leiningen.clj-depend :as lein]
            [clojure.java.io :as io]))

(deftest clj-depend-test

  (testing "should return zero violations when there are no violations"
    (let [captured-exit-code (atom nil)]
      (with-redefs [clj-depend.core/exit! (fn [code] (reset! captured-exit-code code))]
        (let [output (with-out-str (lein/clj-depend {:root         (.getPath (io/resource "without-violations"))
                                                     :source-paths [(.getPath (io/resource "without-violations/src"))]}))]
          (is (= 0 @captured-exit-code))
          (is (= "Identified 0 violations\n" output))))))

  (testing "should return violations when there is any violation"
    (let [captured-exit-code (atom nil)]
      (with-redefs [clj-depend.core/exit! (fn [code] (reset! captured-exit-code code))]
        (let [output (with-out-str (lein/clj-depend {:root         (.getPath (io/resource "with-violations"))
                                                     :source-paths [(.getPath (io/resource "with-violations/src"))]}))]
          (is (= 1 @captured-exit-code))
          (is (= "Identified 1 violations\n- \"sample.logic.foo\" depends on \"sample.controller.foo\"\n" output))))))

  (testing "should return cyclic dependency violation"
    (let [captured-exit-code (atom nil)]
      (with-redefs [clj-depend.core/exit! (fn [code] (reset! captured-exit-code code))]
        (let [output (with-out-str (lein/clj-depend {:root         (.getPath (io/resource "with-cyclic-dependency"))
                                                     :source-paths [(.getPath (io/resource "with-cyclic-dependency/src"))]}))]
          (is (= 1 @captured-exit-code))
          (is (= "Circular dependency between \"sample.logic.foo\" and \"sample.controller.foo\"\n" output)))))))