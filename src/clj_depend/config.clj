(ns clj-depend.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.io PushbackReader)))

(defn read!
  [project-dir]
  (let [f (io/file project-dir ".clj-depend" "config.edn")]
    (when (.exists f)
      (with-open [r (PushbackReader. (io/reader f))]
        (edn/read r)))))
