(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]))

(def ns-deps-a {'foo.a.bar #{'foo.b.bar 'foo.c.bar 'foo.b.baz}})

(def ns-deps-b {'foo.b.bar #{'foo.b.baz}
                'foo.b.baz #{}})

(def ns-deps-c {'foo.c.bar #{}})

(def ns-deps-c-with-violation {'foo.c.bar #{'foo.b.bar}})

(def config-using-accessed-by-layers {:layers {:a {:defined-by         ".*\\.a\\..*"
                                                   :accessed-by-layers #{}}
                                               :b {:defined-by         ".*\\.b\\..*"
                                                   :accessed-by-layers #{:a}}
                                               :c {:defined-by         ".*\\.c\\..*"
                                                   :accessed-by-layers #{:a :b}}}})

(def config-using-accessed-by-layers-with-namespaces {:layers {:a {:namespaces         #{'foo.a.bar}
                                                                   :accessed-by-layers #{}}
                                                               :b {:namespaces         #{'foo.b.bar 'foo.b.baz}
                                                                   :accessed-by-layers #{:a}}
                                                               :c {:defined-by         ".*\\.c\\..*"
                                                                   :accessed-by-layers #{:a :b}}}})

(def config-using-access-layers {:layers {:a {:defined-by      ".*\\.a\\..*"
                                              :accesses-layers #{:b :c}}
                                          :b {:defined-by      ".*\\.b\\..*"
                                              :accesses-layers #{:c}}
                                          :c {:defined-by      ".*\\.c\\..*"
                                              :accesses-layers #{}}}})

(def ns-deps (merge ns-deps-a ns-deps-b ns-deps-c))

(def ns-deps-with-violations (merge ns-deps-a ns-deps-b ns-deps-c-with-violation))

(deftest analyze-test
  (testing "should return zero violations when there is no layers declared"
    (is (= []
           (analyzer/analyze {:config                    {:layers {}}
                              :dependencies-by-namespace ns-deps})))))

(deftest analyze-test-using-accessed-by-layers
  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config                    config-using-accessed-by-layers
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    config-using-accessed-by-layers
                              :dependencies-by-namespace ns-deps-with-violations}))))

  (testing "should return zero violations when a layer has namespaces that depend on another namespace that is not in any layer of the configuration"
    (is (= []
           (analyzer/analyze {:config                    (update-in config-using-accessed-by-layers [:layers] dissoc :b)
                              :dependencies-by-namespace ns-deps})))))

(deftest analyze-config-with-namespaces-test
  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config                    config-using-accessed-by-layers-with-namespaces
                              :dependencies-by-namespace ns-deps}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    config-using-accessed-by-layers-with-namespaces
                              :dependencies-by-namespace ns-deps-with-violations})))))

(deftest analyze-test-using-access-layers
  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace            'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :layer                :c
             :dependency-layer     :b
             :message              "\"foo.c.bar\" should not depend on \"foo.b.bar\" (layer \":c\" on \":b\")"}]
           (analyzer/analyze {:config                    config-using-access-layers
                              :dependencies-by-namespace ns-deps-with-violations}))))

  (testing "should return zero violations when a layer has namespaces that depend on another namespace that is not in any layer of the configuration"
    (is (= []
           (analyzer/analyze {:config                    (update-in config-using-access-layers [:layers] dissoc :b)
                              :dependencies-by-namespace ns-deps-with-violations})))))

(deftest analyze-rules
  (testing "should return violations when a rule is satisfied"
    (are [config]
      (is (match? (m/in-any-order [{:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.bar
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.bar\""}
                                   {:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.baz
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.baz\""}])
                  (analyzer/analyze {:config                    config
                                     :dependencies-by-namespace ns-deps})))

      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\.bar" 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{"foo\\.b\\.bar" 'foo.b.baz}}]}))

  (testing "should return zero violations when a rule is not satisfied"
    (is (empty? (analyzer/analyze {:config                    {:rules [{:defined-by ".*\\.c\\..*" :should-not-depend-on #{".*\\.a\\..*"}}]}
                                   :dependencies-by-namespace ns-deps})))))
