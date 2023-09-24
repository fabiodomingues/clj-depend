(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]))

(def ns-deps-a {'foo.a.bar #{'foo.b.bar 'foo.c.bar}})

(def ns-deps-b {'foo.b.bar #{'foo.b.baz}
                'foo.b.baz #{}})

(def ns-deps-c {'foo.c.bar #{}})

(def ns-deps-c-with-violation {'foo.c.bar #{'foo.b.bar}})

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

(def ns-deps (merge ns-deps-a ns-deps-b ns-deps-c))

(def ns-deps-with-violations (merge ns-deps-a ns-deps-b ns-deps-c-with-violation))

(deftest analyze-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config                    config
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return zero violations when there is no layers declared"
    (is (= []
           (analyzer/analyze {:config                    {:layers {}}
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return zero violations when a namespace is accessed by unconfigured layers."
    (is (= []
           (analyzer/analyze {:config                    {:layers {:c {:defined-by         ".*\\.c\\..*"
                                                                       :accessed-by-layers #{}}}}
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    config
                              :dependencies-by-namespace ns-deps-with-violations}))))

  (testing "should return violations when there is any forbidden access when using :access-layers instead of :accessed-by-layers"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    {:layers {:a {:defined-by      ".*\\.a\\..*"
                                                                       :accesses-layers #{:b :c}}
                                                                   :b {:defined-by      ".*\\.b\\..*"
                                                                       :accesses-layers #{:c}}
                                                                   :c {:defined-by      ".*\\.c\\..*"
                                                                       :accesses-layers #{}}}}
                              :dependencies-by-namespace ns-deps-with-violations}))))

  (testing "should return zero violations when layer dependencies are not covered by any other layer"
    (is (= []
           (analyzer/analyze {:config                    {:layers {:a {:namespaces         #{'foo.a.bar}
                                                                       :accessed-by-layers #{}}}}
                              :dependencies-by-namespace ns-deps})))))

(deftest analyze-config-with-namespaces-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config                    config-with-namespaces
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    config-with-namespaces
                              :dependencies-by-namespace ns-deps-with-violations})))))
