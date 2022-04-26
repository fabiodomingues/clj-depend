(ns clj-depend.model
  (:require [schema.core :as s]
            [clj-depend.config :as config]))

(def Namespace {:name s/Str
                :dependencies [s/Str]})

(def Context {:config     config/Config
              :namespaces [Namespace]})