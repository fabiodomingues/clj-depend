(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]))

(def namespace-a {:name         "foo.a.bar"
                  :dependencies ["foo.b.bar" "foo.c.bar"]})

(def namespace-b {:name         "foo.b.bar"
                  :dependencies []})

(def namespace-c {:name         "foo.c.bar"
                  :dependencies []})

(def namespace-c-with-violation {:name         "foo.c.bar"
                                 :dependencies ["foo.b.bar"]})

(def config {:layers {:a {:defined-by         ".*\\.a\\..*"
                          :accessed-by-layers #{}}
                      :b {:defined-by         ".*\\.b\\..*"
                          :accessed-by-layers #{:a}}
                      :c {:defined-by         ".*\\.c\\..*"
                          :accessed-by-layers #{:a :b}}}})

(def namespaces [namespace-a namespace-b namespace-c])

(def namespaces-with-violations [namespace-a namespace-b namespace-c-with-violation])

(deftest analyze-test

  (testing "should return zero violations when there is no forbidden access"
    (let [violations (analyzer/analyze config namespaces)]
      (is (= [] violations))))

  (testing "should return violations when there is any forbidden access"
    (let [violations (analyzer/analyze config namespaces-with-violations)]
      (is (= [{:namespace  "foo.c.bar"
               :violation "foo.b.bar"}] violations)))))
