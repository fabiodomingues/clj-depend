(ns clj-depend.analyzers.rule-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzers.rule :as analyzers.rule]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]))

(deftest analyze-test
  (testing "should return violations when at least one rule is satisfied"
    (are [config]
      (is (match? (m/in-any-order [{:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.bar
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.bar\""}
                                   {:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.baz
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.baz\""}])
                  (analyzers.rule/analyze config
                                          'foo.a.bar
                                          #{'foo.b.bar 'foo.c.bar 'foo.b.baz})))

      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\.bar" 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{"foo\\.b\\.bar" 'foo.b.baz}}]}))

  (testing "should not return violations when no rules are satisfied"
    (is (empty? (analyzers.rule/analyze {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.d\\..*"}}]}
                                        'foo.a.bar
                                        #{'foo.b.bar 'foo.c.bar 'foo.b.baz})))))