(ns clj-depend.main
  (:refer-clojure :exclude [run!])
  (:require [clj-depend.internal-api :as internal-api]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def cli-options
  [["-p" "--project-root PATH" "Specify the path to the project root to clj-depend consider during analysis."
    :id :project-root
    :parse-fn io/file
    :validate [#(-> % io/file .exists) "Specify a valid path after --project-root"]]
   [nil "--source-paths PATHS" "Source paths to clj-depend consider during analysis."
    :id :source-paths
    :validate [#(not (string/includes? (str %) " ")) "Paths should be separated by comma."]
    :assoc-fn #(assoc %1 %2 (->> (string/split %3 #",")
                                 (map io/file)))]])

(defn- exit!
  [exit-code message]
  (when message (println message))
  (System/exit (or exit-code 2)))

(defn run!
  [& args]
  (let [{:keys [options]} (cli/parse-opts args cli-options)]
    (internal-api/analyze options)))

(defn main
  [& args]
  (let [{:keys [result-code message]} (apply run! args)]
    (exit! result-code message)))

(defn -main
  [& args]
  (main args))
