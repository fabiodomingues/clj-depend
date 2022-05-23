(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]
            [clj-depend.dependency :as dependency]))

(def ns-deps-a [{:name         'foo.a.bar
                 :dependencies ['foo.b.bar 'foo.c.bar]}])

(def ns-deps-b [{:name         'foo.b.bar
                 :dependencies ['foo.b.baz]}
                {:name         'foo.b.baz
                 :dependencies []}])

(def ns-deps-c [{:name         'foo.c.bar
                 :dependencies []}])

(def ns-deps-c-with-violation [{:name         'foo.c.bar
                                :dependencies ['foo.b.bar]}])

(def config {:layers {:a {:defined-by         ".*\\.a\\..*"
                          :accessed-by-layers #{}}
                      :b {:defined-by         ".*\\.b\\..*"
                          :accessed-by-layers #{:a}}
                      :c {:defined-by         ".*\\.c\\..*"
                          :accessed-by-layers #{:a :b}}}})

(def ns-deps (concat ns-deps-a ns-deps-b ns-deps-c))
(def namespaces (map :name ns-deps))
(def dependency-graph (dependency/dependencies-graph ns-deps))

(def ns-deps-with-violations (concat ns-deps-a ns-deps-b ns-deps-c-with-violation))
(def namespaces-with-violations (map :name ns-deps-with-violations))
(def dependency-graph-with-violations (dependency/dependencies-graph ns-deps-with-violations))

(deftest analyze-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config           config
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return zero violations when there is no layers declared"
    (is (= []
           (analyzer/analyze {:config           {:layers {}}
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return zero violations when a namespace is accessed by unconfigured layers."
    (is (= []
           (analyzer/analyze {:config           {:layers {:c {:defined-by         ".*\\.c\\..*"
                                                              :accessed-by-layers #{}}}}
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace 'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :message "\"foo.c.bar\" should not depends on \"foo.b.bar\""}]
           (analyzer/analyze {:config           config
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations}))))
  )
