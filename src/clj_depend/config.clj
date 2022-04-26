(ns clj-depend.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [schema.core :as s])
  (:import (java.io PushbackReader)))

(def Layer {:defined-by s/Str                               ; TODO: Change to s/Regex
            :accessed-by-layers #{s/Keyword}})

(def Config {:layers {s/Keyword Layer}})

(defn read!
  [project-dir]
  (let [f (io/file project-dir ".clj-depend" "config.edn")]
    (when (.exists f)
      (with-open [r (PushbackReader. (io/reader f))]
        (edn/read r)))))
