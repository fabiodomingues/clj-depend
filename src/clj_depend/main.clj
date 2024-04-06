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
   [nil "--files PATHS" "Files or directories to clj-depend consider during analysis."
    :id :files
    :validate [#(not (string/includes? (str %) " ")) "Files should be separated by comma."]
    :assoc-fn #(assoc %1 %2 (->> (string/split %3 #",")
                                 (map io/file)))]
   [nil "--namespaces NAMESPACES" "Namespaces to be analyzed. If empty, all project namespaces will be considered."
    :id :namespaces
    :validate [#(not (string/includes? (str %) " ")) "Namespaces should be separated by comma."]
    :assoc-fn #(assoc %1 %2 (->> (string/split %3 #",")
                                 (map symbol)))]
   [nil "--snapshot" "Analyze namespace dependencies and dump the violations into a snapshot file (`.clj-depend/snapshot.edn`) that is used as a reference for further analysis."
    :id :snapshot?
    :default false]])

(defn- exit!
  [exit-code message]
  (when message (println message))
  (System/exit (or exit-code 2)))

(defn run!
  [& args]
  (let [{:keys [options]} (cli/parse-opts args cli-options)]
    (internal-api/analyze options)))

(defn -main
  [& args]
  (let [{:keys [result-code message]} (apply run! args)]
    (exit! result-code message)))
