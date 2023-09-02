(ns clj-depend.utils.directory
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [clj-depend.api :as clj-depend]
            [clojure.string :as str])
  (:import
    (java.io File)))

(defn ^:private find-uncle-file
  "Finds an ancestor directory of file f containing a file uncle."
  [f uncle]
  (let [f (if (string? f) (io/file f) f)
        uncle (if (string? uncle) uncle (.getPath uncle))
        d0 (if (.isDirectory f) f (.getParentFile f))]
    (loop [dir d0]
      (when dir
        (if (.exists (io/file dir uncle))
          (.getAbsolutePath dir)
          (recur (.getParentFile dir)))))))

(defn ^:private namespace-to-source
  "Converts the namespace object to a source (.clj) file path."
  [ns]
  (when-let [name (try (-> ns ns-name str) (catch Exception e))]
    (let [tokens (.split name "\\.")]
      (str (apply str (interpose File/separator (map munge tokens))) ".clj"))))

(defn ^:private find-resource
  [file]
  (loop [cl (.getContextClassLoader (Thread/currentThread))]
    (when cl
      (if-let [url (.getResource cl file)]
        url
        (recur (.getParent cl))))))

(defn project-dir
  "Returns the absolute file path of the parent of the src directory
   enclosing the current source file (or namespace's) package dirs.
   If running from a jar, returns the enclosing directory."
  ([file]
   (when-let [url (find-resource file)]
     (let [stub (.replace (.getFile url) file "")]
       (->
         (if (.endsWith stub ".jar!/")
           (.substring stub 5 (- (.length stub) 2))
           stub)
         io/file
         .getParentFile
         .getAbsolutePath))))
  ([]
   (or (project-dir *file*)
       (project-dir (namespace-to-source *ns*))
       (find-uncle-file (io/file ".") "project.clj")
       (find-uncle-file (io/file ".") "deps.edn"))))
