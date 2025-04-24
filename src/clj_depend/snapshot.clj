(ns clj-depend.snapshot
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(defn ^:private ->snapshot-violation
  [violation]
  (select-keys violation [:namespace :dependency-namespace :layer :dependency-layer]))

(defn ^:private has-violation?
  [violations violation]
  (boolean (some #(= (->snapshot-violation violation) (->snapshot-violation %)) violations)))

(defn ^:private check-snapshot-violations-no-longer-needed
  [snapshot-violations violations]
  (when-let [snapshot-violations-no-longer-needed (not-empty (remove #(has-violation? violations %) snapshot-violations))]
    (throw (ex-info "The code has been improved, and one or more violations present in the clj-depend violations snapshot file are no longer needed. Please run clj-depend with the `--snapshot` option to update the snapshot file and commit the changes."
                    {:reason                      ::snapshot-violations-no-longer-needed
                     :violations-no-longer-needed snapshot-violations-no-longer-needed}))))

(defn without-violations-present-in-snapshot-file!
  [violations
   {:keys [project-root]}]
  (let [snapshot-file (io/file project-root ".clj-depend" "snapshot.edn")]
    (if (.exists snapshot-file)
      (let [snapshot-violations (-> snapshot-file slurp edn/read-string :violations)]
        (check-snapshot-violations-no-longer-needed snapshot-violations violations)
        (remove #(has-violation? snapshot-violations %) violations))
      violations)))

(defn dump-when-enabled!
  [violations
   {:keys [project-root snapshot?]}]
  (when snapshot?
    (let [snapshot-file (io/file project-root ".clj-depend" "snapshot.edn")]
      (io/make-parents snapshot-file)
      (spit snapshot-file {:violations (vec (map ->snapshot-violation violations))}))))
