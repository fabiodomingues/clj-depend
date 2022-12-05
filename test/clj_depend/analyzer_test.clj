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

(def config-with-namespaces {:layers {:a {:namespaces         #{'foo.a.bar}
                                          :accessed-by-layers #{}}
                                      :b {:namespaces         #{'foo.b.bar 'foo.b.baz}
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
             :message "\"foo.c.bar\" should not depend on \"foo.b.bar\""}]
           (analyzer/analyze {:config           config
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations}))))

  (testing "should return violations when there is any forbidden access when using :access-layers instead of :accessed-by-layers"
    (is (= [{:namespace 'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :message "\"foo.c.bar\" should not depend on \"foo.b.bar\""}]
           (analyzer/analyze {:config           {:layers {:a {:defined-by         ".*\\.a\\..*"
                                                              :accesses-layers #{:b :c}}
                                                          :b {:defined-by         ".*\\.b\\..*"
                                                              :accesses-layers #{:c}}
                                                          :c {:defined-by         ".*\\.c\\..*"
                                                              :accesses-layers #{}}}}
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations}))))

  (testing "should return zero violations when layer dependencies are not covered by any other layer"
    (is (= []
           (analyzer/analyze {:config           {:layers {:a {:namespaces #{'foo.a.bar}
                                                              :accessed-by-layers #{}}}}
                              :namespaces       namespaces
                              :dependency-graph dependency-graph})))))

(deftest analyze-config-with-namespaces-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config           config-with-namespaces
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace 'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :message "\"foo.c.bar\" should not depend on \"foo.b.bar\""}]
           (analyzer/analyze {:config           config-with-namespaces
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations})))))
