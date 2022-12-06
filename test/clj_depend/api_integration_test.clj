(ns clj-depend.api-integration-test
  (:require [clj-depend.api :as api]
            [clj-depend.config :as config]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(deftest analyze-invalid-options
  (testing "should thrown an exception when project-root is not a file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root "./sample"}))))

  (testing "should thrown an exception when project-root is not an existent file"
    (is (thrown? AssertionError
                 (api/analyze {:project-root (io/file "./non-existent-folder")})))))

(deftest analyze-project-with-cyclic-dependency
  (testing "should fail when a cyclic dependency is identified"
    (is (= {:result-code 2
            :message     "Circular dependency between sample.logic.foo and sample.controller.foo"}
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
                         :config       (-> (config/read! (io/file (io/resource "without-violations")))
                                           (assoc-in [:layers :controller :accessed-by-layers] #{}))}))))

  (testing "should succeed when given a different configuration as a parameter"
    (is (= {:result-code 0
            :message     "No violations found!"}
           (api/analyze {:project-root (io/file (io/resource "with-violations"))
                         :config       {:source-paths #{"src"}
                                        :layers       (-> (config/read! (io/file (io/resource "with-violations")))
                                                          (assoc-in [:layers :controller :accessed-by-layers] #{:logic})
                                                          (assoc-in [:layers :logic :accessed-by-layers] #{}))}}))))

  (testing "should fail when given a different configuration as a parameter without source-paths"
    (is (= {:result-code 1
            :message     "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"
            :violations  [{:namespace            'sample.server
                           :dependency-namespace 'sample.controller.foo
                           :layer                :server
                           :dependency-layer     :controller
                           :message              "\"sample.server\" should not depend on \"sample.controller.foo\" (layer \":server\" on \":controller\")"}]}
           (api/analyze {:project-root (io/file (io/resource "without-violations"))
                         :config       (-> (config/read! (io/file (io/resource "without-violations")))
                                           (dissoc :source-paths)
                                           (assoc-in [:layers :controller :accessed-by-layers] #{}))})))))

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
