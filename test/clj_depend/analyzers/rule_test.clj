(ns clj-depend.analyzers.rule-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzers.rule :as analyzers.rule]
            [matcher-combinators.test :refer [match?]]
            [matcher-combinators.matchers :as m]))

(deftest analyze-test
  (testing "should not return violations when there is a rule for the namespace and the constraints are satisfied"
    (are [config]
      (is (match? (m/in-any-order [{:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.bar
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.bar\""}
                                   {:namespace            'foo.a.bar
                                    :dependency-namespace 'foo.b.baz
                                    :message              "\"foo.a.bar\" should not depend on \"foo.b.baz\""}])
                  (analyzers.rule/analyze config
                                          'foo.a.bar
                                          {'foo.a.bar #{'foo.b.bar 'foo.c.bar 'foo.b.baz}})))

      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\.bar" 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{".*\\.b\\..*"}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}
      {:rules [{:namespaces #{'foo.a.bar} :should-not-depend-on #{"foo\\.b\\.bar" 'foo.b.baz}}]}
      {:rules [{:should-not-depend-on #{'foo.b.bar 'foo.b.baz}}]}))

  (testing "should return violations with custom message"
    (is (= [{:namespace            'foo.a.bar
             :dependency-namespace 'foo.b.bar
             :message              "Prefer using foo.x.bar instead of foo.b.bar"}]
           (analyzers.rule/analyze {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.b\\..*"} :message "Prefer using foo.x.bar instead of foo.b.bar"}]}
                                   'foo.a.bar
                                   {'foo.a.bar #{'foo.b.bar}}))))

  (testing "should not return violations when there is a rule for the namespace but the constraints are not satisfied"
    (is (empty? (analyzers.rule/analyze {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.d\\..*"}}]}
                                        'foo.a.bar
                                        {'foo.a.bar #{'foo.b.bar 'foo.c.bar 'foo.b.baz}}))))

  (testing "should not return violations when there is no rule for the namespaces"
    (is (empty? (analyzers.rule/analyze {:rules [{:defined-by ".*\\.a\\..*" :should-not-depend-on #{".*\\.d\\..*"}}]}
                                        'foo.a.bar
                                        {'foo.a.bar #{'foo.b.bar 'foo.c.bar 'foo.b.baz}}))))
  )
