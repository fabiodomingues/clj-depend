(ns clj-depend.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.io PushbackReader)))

(defn read!
  [project-dir]
  (let [config-edn-file (io/file project-dir ".clj-depend" "config.edn")]
    (when (.exists config-edn-file)
      (with-open [reader (PushbackReader. (io/reader config-edn-file))]
        (edn/read reader)))))
