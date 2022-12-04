(ns clj-depend.analyzer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzer :as analyzer]
            [clj-depend.dependency :as dependency]))

(def ns-deps-a [{:name         'foo.a.bar
                 :dependencies ['foo.b.bar 'foo.c.bar]}])

(def ns-deps-b [{:name         'foo.b.bar
                 :dependencies ['foo.b.baz]}
                {:name         'foo.b.baz
                 :dependencies []}])

(def ns-deps-c [{:name         'foo.c.bar
                 :dependencies []}])

(def ns-deps-c-with-violation [{:name         'foo.c.bar
                                :dependencies ['foo.b.bar]}])

(def config {:layers {:a {:defined-by         ".*\\.a\\..*"
                          :accessed-by-layers #{}}
                      :b {:defined-by         ".*\\.b\\..*"
                          :accessed-by-layers #{:a}}
                      :c {:defined-by         ".*\\.c\\..*"
                          :accessed-by-layers #{:a :b}}}})

(def config-with-namespaces {:layers {:a {:namespaces         #{'foo.a.bar}
                                          :accessed-by-layers #{}}
                                      :b {:namespaces         #{'foo.b.bar 'foo.b.baz}
                                          :accessed-by-layers #{:a}}
                                      :c {:defined-by         ".*\\.c\\..*"
                                          :accessed-by-layers #{:a :b}}}})

(def ns-deps (concat ns-deps-a ns-deps-b ns-deps-c))
(def namespaces (map :name ns-deps))
(def dependency-graph (dependency/dependencies-graph ns-deps))

(def ns-deps-with-violations (concat ns-deps-a ns-deps-b ns-deps-c-with-violation))
(def namespaces-with-violations (map :name ns-deps-with-violations))
(def dependency-graph-with-violations (dependency/dependencies-graph ns-deps-with-violations))

(deftest analyze-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config           config
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return zero violations when there is no layers declared"
    (is (= []
           (analyzer/analyze {:config           {:layers {}}
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return zero violations when a namespace is accessed by unconfigured layers."
    (is (= []
           (analyzer/analyze {:config           {:layers {:c {:defined-by         ".*\\.c\\..*"
                                                              :accessed-by-layers #{}}}}
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace 'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :message "\"foo.c.bar\" should not depends on \"foo.b.bar\""}]
           (analyzer/analyze {:config           config
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations})))))

(deftest analyze-config-with-namespaces-test

  (testing "should return zero violations when there is no forbidden access"
    (is (= []
           (analyzer/analyze {:config           config-with-namespaces
                              :namespaces       namespaces
                              :dependency-graph dependency-graph}))))

  (testing "should return violations when there is any forbidden access"
    (is (= [{:namespace 'foo.c.bar
             :dependency-namespace 'foo.b.bar
             :message "\"foo.c.bar\" should not depends on \"foo.b.bar\""}]
           (analyzer/analyze {:config           config-with-namespaces
                              :namespaces       namespaces-with-violations
                              :dependency-graph dependency-graph-with-violations})))))

(deftest analyze-config-with-modules-test
  (testing "should return violations when there is any forbidden access"
    (let [config {:modules {:module-a {:defined-by "module-a\\..*"}
                            :module-b {:defined-by "module-b\\..*"}}
                  :layers  {:layer-a {:defined-by         ".*\\.layer-a\\..*"
                                      :accessed-by-layers #{}}
                            :layer-b {:defined-by         ".*\\.layer-b\\..*"
                                      :accessed-by-layers #{:layer-a}}}}
          ns-deps [{:name 'module-a.layer-a.foo :dependencies ['module-a.layer-b.bar]}
                         {:name 'module-a.layer-b.bar :dependencies []}
                         {:name 'module-b.layer-a.foo :dependencies ['module-b.layer-b.bar]}
                         {:name 'module-b.layer-b.bar :dependencies []}]
          namespaces (map :name ns-deps)
          dependency-graph (dependency/dependencies-graph ns-deps)]

      (is (= []
             (analyzer/analyze {:config           config
                                :namespaces       namespaces
                                :dependency-graph dependency-graph})))))

  (testing "should return violations when there is any forbidden access"
    (let [config {:modules {:module-a {:defined-by "module-a\\..*"}
                            :module-b {:defined-by "module-b\\..*"}}
                  :layers  {:layer-a {:defined-by         ".*\\.layer-a\\..*"
                                      :accessed-by-layers #{}}
                            :layer-b {:defined-by         ".*\\.layer-b\\..*"
                                      :accessed-by-layers #{:layer-a}}}}
          ns-deps [{:name 'module-a.layer-a.foo :dependencies ['module-a.layer-b.bar]}
                   {:name 'module-a.layer-b.bar :dependencies []}
                   {:name 'module-b.layer-a.foo :dependencies ['module-b.layer-b.bar]}
                   {:name 'module-b.layer-b.bar :dependencies ['module-a.layer-b.bar]}]
          namespaces (map :name ns-deps)
          dependency-graph (dependency/dependencies-graph ns-deps)]

      (is (= [{:namespace            'module-b.layer-b.bar
               :dependency-namespace 'module-a.layer-b.bar
               :message              "\"module-b.layer-b.bar\" should not depends on \"module-a.layer-b.bar\""}]
             (analyzer/analyze {:config           config
                                :namespaces       namespaces
                                :dependency-graph dependency-graph}))))))


; Como definir quando um módulo é comum?
; Como definir quando um módulo é o main e vê todos os outros?
; Caso onde uma camada do module-a é acessado por 1 ou mais camadas do modulo-b
