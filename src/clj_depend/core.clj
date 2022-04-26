(ns clj-depend.core
  (:require [schema.core :as s]
            [clojure.string :as str])
  (:use [clojure.pprint]))

(def print-pattern "- \"{NAMESPACE}\" depends on \"{VIOLATION}\"")

(s/defn print!
  [analyzer-report duration]
  (let [violations-count (count analyzer-report)]
    (when (> (count analyzer-report) 0)
      (println "Identified violations:")
      (doseq [{:keys [namespace violation]} analyzer-report]
        (println (-> print-pattern
                     (str/replace "{NAMESPACE}" namespace)
                     (str/replace "{VIOLATION}" violation)))))
    (println (format "\nclj-depend took %sms, violations: %s" duration violations-count))))
