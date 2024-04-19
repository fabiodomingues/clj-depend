(ns clj-depend.analyzers.circular-dependency-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzers.circular-dependency :as analyzers.circular-dependency]))

(deftest analyze-test
  (testing "should return violations when there is circular dependency"
    (is (= [{:namespace            'foo.a
             :dependency-namespace 'foo.b
             :message              "Circular dependency between \"foo.a\" and \"foo.b\""}]
           (analyzers.circular-dependency/analyze 'foo.a
                                                  {'foo.a #{'foo.b}
                                                   'foo.b #{'foo.a}}))))

  (testing "should not return violations when there is no circular dependency"
    (is (empty? (analyzers.circular-dependency/analyze 'foo.a
                                                       {'foo.a #{'foo.b}
                                                        'foo.b #{}})))))
