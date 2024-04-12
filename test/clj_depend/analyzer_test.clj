(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]))

(deftest analyze-test
  (testing "should not return violations when there are no circular dependencies and no layers/rules violated"
    (is (empty? (analyzer/analyze {:config                      {:layers {}
                                                                 :rules  []}
                                   :namespaces-and-dependencies [{:namespace 'foo.a.bar :dependencies #{}}
                                                                 {:namespace 'foo.b.bar :dependencies #{'foo.a.bar}}
                                                                 {:namespace 'foo.any :dependencies #{'foo.a.bar}}
                                                                 {:namespace 'foo.a-test :dependencies #{'lib.x.y.z}}]
                                   :namespaces-to-be-analyzed   #{'foo.a.bar 'foo.b.bar 'foo.any 'foo.a-test}}))))

  (testing "should return violations when there are circular dependencies or layers/rules violated"
    (is (match? (m/in-any-order [{:namespace            'foo.a.bar
                                  :dependency-namespace 'foo.any
                                  :message              "Circular dependency between \"foo.a.bar\" and \"foo.any\""}
                                 {:namespace            'foo.any
                                  :dependency-namespace 'foo.a.bar
                                  :message              "Circular dependency between \"foo.any\" and \"foo.a.bar\""}
                                 {:namespace            'foo.b.bar
                                  :layer                :b
                                  :dependency-namespace 'foo.a.bar
                                  :dependency-layer     :a
                                  :message              "\"foo.b.bar\" should not depend on \"foo.a.bar\" (layer \":b\" on \":a\")"}
                                 {:namespace            'foo.a-test
                                  :dependency-namespace 'lib.x.y.z
                                  :message              "\"foo.a-test\" should not depend on \"lib.x.y.z\""}])
                (analyzer/analyze {:config                      {:layers {:a {:defined-by      ".*\\.a\\..*"
                                                                              :accesses-layers #{}}
                                                                          :b {:defined-by      ".*\\.b\\..*"
                                                                              :accesses-layers #{}}}
                                                                 :rules  [{:namespaces #{'foo.a-test} :should-not-depend-on #{'lib.x.y.z}}]}
                                   :namespaces-and-dependencies [{:namespace 'foo.a.bar :dependencies #{'foo.any}}
                                                                 {:namespace 'foo.b.bar :dependencies #{'foo.a.bar}}
                                                                 {:namespace 'foo.any :dependencies #{'foo.a.bar}}
                                                                 {:namespace 'foo.a-test :dependencies #{'lib.x.y.z}}]
                                   :namespaces-to-be-analyzed   #{'foo.a.bar 'foo.b.bar 'foo.any 'foo.a-test}})))))
