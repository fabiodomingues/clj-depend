(ns clj-depend.model.namespace
  (:require [schema.core :as s]))

(def Namespace {:name s/Str
                :dependencies [s/Str]})