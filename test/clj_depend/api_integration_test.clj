(ns clj-depend.api-integration-test
  (:require [clj-depend.api :as api]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer :all])
  (:import (java.io PushbackReader)))

(deftest analyze-invalid-options
  (testing "should thrown an exception when project-root is not a file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root "./sample"}))))

  (testing "should thrown an exception when project-root is not an existent file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root (io/file "./non-existent-folder")})))))

(deftest analyze-project-with-cyclic-dependency
  (testing "should fail when a cyclic dependency is identified"
    (is (= {:result-code 1
            :message     "Circular dependency between \"sample.controller.foo\" and \"sample.logic.foo\"\nCircular dependency between \"sample.logic.foo\" and \"sample.controller.foo\"\n\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace 'sample.controller.foo
                           :dependency-namespace 'sample.logic.foo
                           :message   "Circular dependency between \"sample.controller.foo\" and \"sample.logic.foo\""}
                          {:namespace 'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :message   "Circular dependency between \"sample.logic.foo\" and \"sample.controller.foo\""}
                          {:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-cyclic-dependency"))})))))

(deftest analyze-project-without-violations
  (testing "should succeed when there are no violations"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))})))))

(deftest analyze-project-with-violation
  (testing "should fail when there is at least one violation"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))})))))

(deftest analyze-project-with-custom-config
  (testing "should fail when given a different configuration as a parameter"
    (is (= {:result-code 1
            :message     "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"
            :violations  [{:namespace            'sample.server
                           :dependency-namespace 'sample.controller.foo
                           :layer                :server
                           :dependency-layer     :controller
                           :message              "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :config       {:layers {:server     {:namespaces         #{'sample.server}
                                                              :accessed-by-layers #{}}
                                                 :controller {:defined-by         ".*\\.controller\\..*"
                                                              :accessed-by-layers #{}}
                                                 :logic      {:defined-by         ".*\\.logic\\..*"
                                                              :accessed-by-layers #{:controller}}}}}))))

  (testing "should succeed when given a different configuration as a parameter"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :config       {:layers {:server     {:namespaces         #{'sample.server}
                                                              :accessed-by-layers #{}}
                                                 :controller {:defined-by         ".*\\.controller\\..*"
                                                              :accessed-by-layers #{:logic :server}}
                                                 :logic      {:defined-by         ".*\\.logic\\..*"
                                                              :accessed-by-layers #{}}}}}))))

  (testing "should fail when given a different configuration as a parameter without source-paths"
    (is (= {:result-code 1
            :message     "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"
            :violations  [{:namespace            'sample.server
                           :dependency-namespace 'sample.controller.foo
                           :layer                :server
                           :dependency-layer     :controller
                           :message              "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :config       {:source-paths #{}
                                        :layers       {:server     {:namespaces         #{'sample.server}
                                                                    :accessed-by-layers #{}}
                                                       :controller {:defined-by         ".*\\.controller\\..*"
                                                                    :accessed-by-layers #{}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{:controller}}}}})))))

(deftest analyze-project-with-specific-namespace
  (testing "should succeed when the namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :namespaces   #{'sample.controller.foo}}))))

  (testing "should fail even when only the namespace that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :namespaces   #{'sample.logic.foo}})))))

(deftest analyze-project-with-specific-file
  (testing "should succeed when the files that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/controller/foo.clj"))}}))))

  (testing "should fail even when only the files that has the violation is included in the analysis."
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/logic/foo.clj"))}})))))

(deftest analyze-project-with-specific-file-and-namespace
  (testing "should succeed when the files that has the violation is included but namespace that has the violation is not included in the analysis"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :files        #{(io/file (io/resource "with-violations/src/sample/logic/foo.clj"))}
                         :namespaces   #{'sample.controller.foo}})))))

(deftest analyze-project-with-modular-structure
  (testing "should succeed when the files from a module doesn't access other module"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "without-violations-for-modular-structure"))}))))

  (testing "should fail when the files from a module access other module"
    (is (= {:result-code 1
            :message     "\"module1.controller.foo\" should not depend on \"module2.logic.foo\" (layer \":module1-controller\" on \":module2-logic\")"
            :violations  [{:namespace            'module1.controller.foo
                           :dependency-namespace 'module2.logic.foo
                           :layer                :module1-controller
                           :dependency-layer     :module2-logic
                           :message              "\"module1.controller.foo\" should not depend on \"module2.logic.foo\" (layer \":module1-controller\" on \":module2-logic\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-for-modular-structure"))})))))

(deftest analyze-project-with-violation-between-different-source-paths
  (testing "should succeed when the source-paths do not include the one containing violations"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-between-different-source-paths"))
                         :config       {:source-paths #{"src"}}}))))

  (testing "should fail when the source-paths include the one containing violations"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo-test\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo-test
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo-test\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-between-different-source-paths"))
                         :config       {:source-paths #{"src" "test"}}}))))

  (testing "should fail when there is no source-paths configured because the entire project is analyzed"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo-test\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo-test
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo-test\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-between-different-source-paths"))
                         :config       {:source-paths #{}}}))))

  (testing "should succeed when the source paths do not include the one containing violations even when receiving files that have violations but are not in the source-paths"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-between-different-source-paths"))
                         :files        #{(io/file (io/resource "with-violations-between-different-source-paths/test/sample/logic/foo_test.clj"))}
                         :config       {:source-paths #{"src"}}})))))

(deftest analyze-project-with-violations-when-snapshot-enabled
  (testing "should analyze the project and dump violations when snapshot is enabled"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :snapshot?    true})))

    (is (= {:violations [{:namespace 'sample.logic.foo, :dependency-namespace 'sample.controller.foo, :layer :logic, :dependency-layer :controller}]}
           (with-open [reader (PushbackReader. (io/reader (io/file (io/resource "with-violations/.clj-depend/snapshot.edn"))))]
             (edn/read reader)))))

  (testing "should analyze the project and ignore any violations that are present in the snapshot file"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))}))))

  (io/delete-file (io/file (io/resource "with-violations/.clj-depend/snapshot.edn"))))

(deftest analyze-project-without-violations-when-violations-snapshot-file-has-violations-no-longer-needed
  (testing "should analyze the project and fail due to violations in the snapshot file having violations that are no longer needed"
    (is (= {:result-code 2
            :message "The code has been improved, and one or more violations present in the clj-depend violations snapshot file are no longer needed. Please run clj-depend with the `--snapshot` option to update the snapshot file and commit the changes."}
           (api/analyze {:project-root (io/file (io/resource "without-violations-and-snapshot-violations-no-longer-needed"))})))))

(deftest analyze-project-with-violation-without-config
  (testing "should fail when there is at least one violation"
    (is (= {:result-code 1
            :message     "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"
            :violations  [{:namespace            'sample.logic.foo
                           :dependency-namespace 'sample.controller.foo
                           :layer                :logic
                           :dependency-layer     :controller
                           :message              "\"sample.logic.foo\" should not depend on \"sample.controller.foo\" (layer \":logic\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "with-violations-without-config"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:server     {:namespaces         #{'sample.server}
                                                                    :accessed-by-layers #{}}
                                                       :controller {:defined-by         ".*\\.controller\\..*"
                                                                    :accessed-by-layers #{:server}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{:controller}}}}})))))

(deftest analyze-project-with-violation-without-config-when-snapshot-is-enabled
  (testing "should fail when there is at least one violation"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations-without-config"))
                         :config       {:source-paths #{"src"}
                                        :layers       {:server     {:namespaces         #{'sample.server}
                                                                    :accessed-by-layers #{}}
                                                       :controller {:defined-by         ".*\\.controller\\..*"
                                                                    :accessed-by-layers #{:server}}
                                                       :logic      {:defined-by         ".*\\.logic\\..*"
                                                                    :accessed-by-layers #{:controller}}}}
                         :snapshot?    true})))

    (is (= {:violations [{:namespace 'sample.logic.foo, :dependency-namespace 'sample.controller.foo, :layer :logic, :dependency-layer :controller}]}
           (with-open [reader (PushbackReader. (io/reader (io/file (io/resource "with-violations-without-config/.clj-depend/snapshot.edn"))))]
             (edn/read reader))))

    (io/delete-file (io/file (io/resource "with-violations-without-config/.clj-depend/snapshot.edn")))
    (io/delete-file (io/file (io/resource "with-violations-without-config/.clj-depend")))))

(deftest check-if-the-project-is-configured
  (testing "should return true when the project is configured"
    (is (true? (api/configured? (io/file (io/resource "without-violations"))))))

  (testing "should return false when the project is not configured"
    (is (false? (api/configured? (io/file (io/resource "not-configured")))))))
