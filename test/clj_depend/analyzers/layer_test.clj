(ns clj-depend.analyzers.layer-test
  (:require [clojure.test :refer :all]
            [clj-depend.analyzers.layer :as analyzers.layer]))

(deftest analyze-test
  (testing "Given a configuration of layers using defined-by (regular expressions) and accessed-by-layer"
    (let [config {:layers {:a {:defined-by         ".*\\.a\\..*"
                               :accessed-by-layers #{}}
                           :b {:defined-by         ".*\\.b\\..*"
                               :accessed-by-layers #{:a}}}}]

      (testing "when a namespace without disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.b.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace that depends only on a namespace that is not part of any other layer is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.c.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace with disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.b.bar {'foo.b.bar #{'foo.a.bar}})]
          (testing "then no violations should have been returned"
            (is (= [{:namespace            'foo.b.bar
                     :dependency-namespace 'foo.a.bar
                     :layer                :b
                     :dependency-layer     :a
                     :message              "\"foo.b.bar\" should not depend on \"foo.a.bar\" (layer \":b\" on \":a\")"}]
                   violations)))))))

  (testing "Given a configuration of layers using namespaces and accessed-by-layer"
    (let [config {:layers {:a {:namespaces         #{'foo.a.bar}
                               :accessed-by-layers #{}}
                           :b {:namespaces         #{'foo.b.bar}
                               :accessed-by-layers #{:a}}}}]

      (testing "when a namespace without disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.b.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace that depends only on a namespace that is not part of any other layer is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.c.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace with disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.b.bar {'foo.b.bar #{'foo.a.bar}})]
          (testing "then no violations should have been returned"
            (is (= [{:namespace            'foo.b.bar
                     :dependency-namespace 'foo.a.bar
                     :layer                :b
                     :dependency-layer     :a
                     :message              "\"foo.b.bar\" should not depend on \"foo.a.bar\" (layer \":b\" on \":a\")"}]
                   violations)))))))

  (testing "Given a configuration of layers using defined-by and access-layers"
    (let [config {:layers {:a {:defined-by      ".*\\.a\\..*"
                               :accesses-layers #{:b}}
                           :b {:defined-by      ".*\\.b\\..*"
                               :accesses-layers #{}}}}]

      (testing "when a namespace without disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.b.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace that depends only on a namespace that is not part of any other layer is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.a.bar {'foo.a.bar #{'foo.c.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))

      (testing "when a namespace with disallowed dependencies is analyzed"
        (let [violations (analyzers.layer/analyze config 'foo.b.bar {'foo.b.bar #{'foo.a.bar}})]
          (testing "then the violations should have been returned"
            (is (= [{:namespace            'foo.b.bar
                     :dependency-namespace 'foo.a.bar
                     :layer                :b
                     :dependency-layer     :a
                     :message              "\"foo.b.bar\" should not depend on \"foo.a.bar\" (layer \":b\" on \":a\")"}]
                   violations)))))))

  (testing "Given a configuration to analyse only namespaces in source-paths"
    (let [config {:layers {:a {:defined-by              ".*\\.a\\..*"
                               :only-ns-in-source-paths true
                               :accesses-layers         #{:b}}
                           :b {:defined-by              ".*\\.b\\..*"
                               :accesses-layers         #{}}}}]

      (testing "when a namespace that is not in the source-paths and that matches the defined-by regular expression of a layer that is not allowed to be accessed"
        (let [violations (analyzers.layer/analyze config 'foo.b.bar {'foo.b.bar #{'foo.a.bar}})]
          (testing "then no violations should have been returned"
            (is (empty? violations)))))))

  (testing "Given a configuration with access-peer-ns? enabled (default behavior)"
    (let [config {:layers {:controller {:defined-by ".*\\.controller\\..*"}}}]

      (testing "when a namespace in the same layer depends on another namespace in the same layer"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.bar}})]
          (testing "then no violations should be returned"
            (is (= []
                   violations)))))

      (testing "when a namespace depends on itself"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.foo}})]
          (testing "then no violations should be returned"
            (is (= []
                   violations)))))))

  (testing "Given a configuration with access-peer-ns? disabled"
    (let [config {:layers {:controller {:defined-by ".*\\.controller\\..*"
                                        :access-peer-ns false}}}]

      (testing "when a namespace in the same layer depends on another namespace in the same layer"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.bar}})]
          (testing "then a violation should be returned"
            (is (= [{:namespace            'my-app.controller.foo
                     :dependency-namespace 'my-app.controller.bar
                     :layer                :controller
                     :dependency-layer     :controller
                     :message              "\"my-app.controller.foo\" should not depend on \"my-app.controller.bar\" (layer \":controller\" on \":controller\")"}]
                   violations)))))

      (testing "when a namespace depends on itself"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.foo}})]
          (testing "then no violations should be returned"
            (is (= []
                   violations)))))))

  (testing "Given a configuration with access-peer-ns? explicitly enabled"
    (let [config {:layers {:controller {:defined-by ".*\\.controller\\..*"
                                        :access-peer-ns true}}}]

      (testing "when a namespace in the same layer depends on another namespace in the same layer"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.bar}})]
          (testing "then no violations should be returned"
            (is (= []
                   violations)))))

      (testing "when a namespace depends on itself"
        (let [violations (analyzers.layer/analyze config
                                                  'my-app.controller.foo
                                                  {'my-app.controller.foo #{'my-app.controller.foo}})]
          (testing "then no violations should be returned"
            (is (= []
                   violations))))))))
