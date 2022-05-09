(ns clj-depend.api-integration-test
  (:require [clj-depend.api :as api]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(deftest analyze

  (testing "should thrown an exception when project-root is not a file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root "./sample"}))))

  (testing "should thrown an exception when project-root is not an existent file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root (io/file "./non-existent-folder")}))))

  (testing "should succeed when there are no violations"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :source-paths [(.getPath (io/resource (str "without-violations" "/src")))]}))))

  (testing "should fail when there is at least one violation"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace 'sample.logic.foo
                           :violation 'sample.controller.foo}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :source-paths [(.getPath (io/resource (str "with-violations" "/src")))]}))))

  (testing "should fail when given a different configuration as a parameter"
    (is (= {:result-code 1
            :message     "\"sample.controller.foo\" should not depends on \"sample.logic.foo\""
            :violations  [{:namespace 'sample.controller.foo
                           :violation 'sample.logic.foo}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :source-paths [(.getPath (io/resource (str "without-violations" "/src")))]
                         :config       {:layers {:controller {:defined-by         ".*\\.controller\\..*"
                                                              :accessed-by-layers #{}}
                                                 :logic      {:defined-by         ".*\\.logic\\..*"
                                                              :accessed-by-layers #{}}}}}))))

  (testing "should succeed when given a different configuration as a parameter"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :source-paths [(.getPath (io/resource (str "with-violations" "/src")))]
                         :config       {:layers {:controller {:defined-by         ".*\\.controller\\..*"
                                                              :accessed-by-layers #{:logic}}
                                                 :logic      {:defined-by         ".*\\.logic\\..*"
                                                              :accessed-by-layers #{}}}}}))))

  (testing "should succeed when the namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :source-paths [(.getPath (io/resource (str "with-violations" "/src")))]
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail even when only the namespace that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace 'sample.logic.foo
                           :violation 'sample.controller.foo}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :source-paths [(.getPath (io/resource (str "with-violations" "/src")))]
                         :namespaces   #{'sample.logic.foo}}))))

  (testing "should fail when a cyclic dependency is identified"
    (is (= {:result-code 2
            :message     "Circular dependency between sample.logic.foo and sample.controller.foo"}
           (api/analyze {:project-root (io/file (io/resource "with-cyclic-dependency"))
                         :source-paths [(.getPath (io/resource (str "with-cyclic-dependency" "/src")))]})))))
