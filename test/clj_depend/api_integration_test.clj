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
           (api/analyze {:project-root (io/file (io/resource "without-violations"))}))))

  (testing "should fail when there is at least one violation"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message              "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))}))))

  (testing "should fail when given a different configuration as a parameter"
    (is (= {:result-code 1
            :message     "\"sample.controller.foo\" should not depends on \"sample.logic.foo\""
            :violations  [{:namespace            'sample.controller.foo
                           :dependency-namespace 'sample.logic.foo
                           :message              "\"sample.controller.foo\" should not depends on \"sample.logic.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:controller {:defined-by         ".*\\.controller\\..*"
                                                                    :accessed-by-layers #{}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{}}}}}))))

  (testing "should succeed when given a different configuration as a parameter"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:controller {:defined-by         ".*\\.controller\\..*"
                                                                    :accessed-by-layers #{:logic}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{}}}}}))))

  (testing "should fail when given a different configuration as a parameter without source-paths"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message              "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :config       {:layers {:controller {:defined-by         ".*\\.controller\\..*"
                                                              :accessed-by-layers #{}}
                                                 :logic      {:defined-by         ".*\\.logic\\..*"
                                                              :accessed-by-layers #{:controller}}}}}))))

  (testing "should succeed when the namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail even when only the namespace that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message              "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :namespaces   #{'sample.logic.foo}}))))

  (testing "should succeed when the files that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/controller/foo.clj"))}}))))

  (testing "should fail even when only the files that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace 'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/logic/foo.clj"))}}))))

  (testing "should succeed when the files that has the violation is included but namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/logic/foo.clj"))}
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail when a cyclic dependency is identified"
    (is (= {:result-code 2
            :message     "Circular dependency between sample.logic.foo and sample.controller.foo"}
           (api/analyze {:project-root (io/file (io/resource "with-cyclic-dependency"))
                         :source-paths #{"src"}})))))

(deftest analyze-namespaced

  (testing "should succeed when there are no violations"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "without-violations-namespaced"))}))))

  (testing "should fail when there is at least one violation"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message              "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))}))))

  (testing "should fail when given a different configuration as a parameter"
    (is (= {:result-code 1
            :message     "\"sample.controller.foo\" should not depends on \"sample.logic.foo\""
            :violations  [{:namespace            'sample.controller.foo
                           :dependency-namespace 'sample.logic.foo
                           :message              "\"sample.controller.foo\" should not depends on \"sample.logic.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations-namespaced"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:controller {:defined-by         #{"sample.controller.foo"}
                                                                    :namespaced true
                                                                    :accessed-by-layers #{}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{}}}}}))))

  (testing "should succeed when given a different configuration as a parameter"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:controller {:defined-by         #{"sample.controller.foo"}
                                                                    :namespaced true
                                                                    :accessed-by-layers #{:logic}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{}}}}}))))

  (testing "should succeed when the namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail even when only the namespace that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message              "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :namespaces   #{'sample.logic.foo}}))))

  (testing "should succeed when the files that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :files        #{(io/file (io/resource "with-violations-namespaced/src/sample/controller/foo.clj"))}}))))

  (testing "should fail even when only the files that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""
            :violations  [{:namespace 'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message "\"sample.logic.foo\" should not depends on \"sample.controller.foo\""}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :files        #{(io/file (io/resource "with-violations-namespaced/src/sample/logic/foo.clj"))}}))))

  (testing "should succeed when the files that has the violation is included but namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-namespaced"))
                         :files        #{(io/file (io/resource "with-violations-namespaced/src/sample/logic/foo.clj"))}
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail when a cyclic dependency is identified"
    (is (= {:result-code 2
            :message     "Circular dependency between sample.logic.foo and sample.controller.foo"}
           (api/analyze {:project-root (io/file (io/resource "with-cyclic-dependency-namespaced"))
                         :source-paths #{"src"}})))))