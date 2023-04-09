(ns clj-depend.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import (java.io PushbackReader)))

(def ^:private default-config
  {:source-paths #{"src"}
   :layers       {}})

(defn read!
  [project-dir]
  (let [config-edn-file (io/file project-dir ".clj-depend" "config.edn")]
    (when (.exists config-edn-file)
      (with-open [reader (PushbackReader. (io/reader config-edn-file))]
        (edn/read reader)))))

(defn resolve-config!
  [project-dir config]
  (let [project-config (read! project-dir)]
    (merge default-config
           project-config
           config)))
