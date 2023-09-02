(ns clj-depend.test-test
  (:require [clojure.test :refer :all]
            [clj-depend.cljtest :refer :all]))

(macroexpand
  (def-layered-architecture my-architecture-test
                            (layer "api" (defined-by "clj-depend.api"))
                            (layer "internal-api" (defined-by "clj-depend.internal-api"))
                            (layer "analyzer" (defined-by "clj-depend.analyzer"))

                            (where-layer "api"
                                         (may-not-be-accessed-by-any-layer))

                            (where-layer "internal-api"
                                         (may-only-be-accessed-by-layers "api"))

                            (where-layer "analyzer"
                                         (may-only-be-accessed-by-layers "internal-api")
                                         (may-only-access-layers "api"))))



;(def-architecture my-architecture-test
;                  (layer "api" (defined-by "clj-depend.api"))
;                  (layer "internal-api" (defined-by "clj-depend.internal-api"))
;                  (layer "analyzer" (defined-by "clj-depend.analyzer"))
;
;                  (where-layer "api"
;                               (may-not-be-accessed-by-any-layer))
;
;                  (where-layer "internal-api"
;                               (may-only-be-accessed-by-layers "api"))
;
;                  (where-layer "analyzer"
;                               (may-only-be-accessed-by-layers "internal-api")
;                               (may-only-access-layers "api")))
