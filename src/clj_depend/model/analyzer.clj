(ns clj-depend.model.analyzer
  (:require [clj-depend.model.config :as model.config]
            [clj-depend.model.namespace :as model.namespace]))

(def Context {:config     model.config/Config
              :namespaces [model.namespace/Namespace]})