(ns clj-depend.snapshot
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import (java.io PushbackReader)))

(defn without-violations-present-in-snapshot-file!
  [violations
   {:keys [project-root]}]
  (let [snapshot-violations-file (io/file project-root ".clj-depend" "violations.edn")]
    (if (.exists snapshot-violations-file)
      (with-open [reader (PushbackReader. (io/reader snapshot-violations-file))]
        (let [snapshot-violations (edn/read reader)
              snapshot-violations-no-longer-needed (remove (fn [snapshot-violation] (some #(= snapshot-violation %) violations)) snapshot-violations)]
          (when (not-empty snapshot-violations-no-longer-needed)
            (throw (ex-info "The code has been improved, and one or more violations present in the clj-depend violations snapshot file are no longer needed. Please run clj-depend with the `--snapshot` option to update the snapshot file and commit the changes. For more information check the documentation at http://xxx.com."
                            {:reason ::snapshot-violations-no-longer-needed
                             :violations-no-longer-needed snapshot-violations-no-longer-needed})))
          (remove (fn [violation] (some #(= violation %) snapshot-violations)) violations)))
      violations)))

(defn dump-when-enabled!
  [violations
   {:keys [project-root snapshot?]}]
  (when snapshot?
    (let [snapshot-violations-file (io/file project-root ".clj-depend" "violations.edn")]
      (with-open [w (clojure.java.io/writer snapshot-violations-file)]
        (.write w (pr-str violations))))))
