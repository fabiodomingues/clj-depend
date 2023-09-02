(ns clj-depend.cljtest
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [clj-depend.api :as clj-depend]
            [clj-depend.utils.directory :as directory]))

(defn ^:private report-analysis-result
  [{:keys [result-code message]}]
  (t/with-test-out
    (if (zero? result-code)
      (t/inc-report-counter :pass)
      (do (t/inc-report-counter :fail)
          (println "\nFAIL in" (reverse (map #(:name (meta %)) t/*testing-vars*)))
          (when (seq t/*testing-contexts*) (println (t/testing-contexts-str)))
          (println message)))))

(defn ^:private analyze
  [arch]
  (clj-depend/analyze {:project-root (io/file (directory/project-dir))
                       :config       (merge arch {:source-paths #{"src"}})}))

; API

(defn may-only-access-layers
  [& layers]
  {:access-layers (set layers)})

(defn may-only-be-accessed-by-layers
  [& layers]
  {:accessed-by-layers (set layers)})

(defn may-not-be-accessed-by-any-layer
  []
  {:accessed-by-layers #{}})

(defn where-layer
  [layer-name & restrictions]
  (fn [arch]
    (let [layer (get-in arch [:layers layer-name])]
      (update-in arch [:layers] assoc layer-name (apply merge layer restrictions)))))

(defn layer
  [name value]
  (fn [arch]
    (assoc arch :layers (assoc (:layers arch) name value))))

(defn defined-by
  [expr]
  {:defined-by expr})

(def layered-architecture
  {:layers {}})

(defmacro def-layered-architecture
  [name & body]
  `(t/deftest ~name (-> layered-architecture
                        ~@(map (fn [expr]
                                 `(~expr)) body)
                        (#'analyze)
                        (#'report-analysis-result))))
