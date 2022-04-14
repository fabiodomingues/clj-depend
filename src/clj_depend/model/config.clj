(ns clj-depend.model.config
  (:require [schema.core :as s]))

(def Layer {:defined-by s/Str                               ; TODO: Change to s/Regex
            :accessed-by-layers #{s/Keyword}})

(def Config {:layers {s/Keyword Layer}})
