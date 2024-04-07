(ns clj-depend.analyzers.rule)

(defn ^:private violation
  [namespace
   dependency
   message]
  {:namespace            namespace
   :dependency-namespace dependency
   :message              (or message
                             (str \" namespace \" " should not depend on " \" dependency \"))})

(defn ^:private should-not-depend-on-dependency?
  [should-not-depend-on
   dependency]
  (cond
    (string? should-not-depend-on)
    (re-find (re-pattern should-not-depend-on) (str dependency))

    (symbol? should-not-depend-on)
    (= should-not-depend-on dependency)))

(defn ^:private dependency-violates-the-rule?
  [dependency
   {:keys [should-not-depend-on]}]
  (some #(should-not-depend-on-dependency? % dependency) should-not-depend-on))

(defn ^:private violations-by-rule
  [{:keys [message] :as rule}
   namespace
   dependencies]
  (->> dependencies
       (filter #(dependency-violates-the-rule? % rule))
       (map #(violation namespace % message))))

(defn ^:private rule-applies-to-namespace?
  [{:keys [namespaces defined-by]}
   namespace]
  (or (some #{namespace} namespaces)
      (when defined-by (re-find (re-pattern defined-by) (str namespace)))
      (and (empty? namespaces) (empty? defined-by))))

(defn analyze
  [{:keys [rules]}
   namespace
   dependencies]
  (->> (filter #(rule-applies-to-namespace? % namespace) rules)
       (keep #(violations-by-rule % namespace dependencies))
       flatten))
