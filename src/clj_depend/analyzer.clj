(ns clj-depend.analyzer)

(defn ^:private should-not-depend-on-dependency?
  [should-not-depend-on dependency]
  (cond
    (string? should-not-depend-on)
    (re-find (re-pattern should-not-depend-on) (str dependency))

    (symbol? should-not-depend-on)
    (= should-not-depend-on dependency)))


(defn ^:private rule-applies-to-namespace?
  [{:keys [namespaces defined-by]}
   namespace]
  (or (some #{namespace} namespaces)
      (when defined-by (re-find (re-pattern defined-by) (str namespace)))))

(defn ^:private rule-violations
  [{:keys [rules]}
   namespace
   dependencies]
  (let [namespace-related-rules (filter #(rule-applies-to-namespace? % namespace) rules)]
    (keep (fn [{:keys [should-not-depend-on] :as rule}]
            (keep (fn [should-not-depend-on]
                    (keep (fn [dependency-namespace]
                            (when (should-not-depend-on-dependency? should-not-depend-on dependency-namespace)
                              {:namespace            namespace
                               :dependency-namespace dependency-namespace
                               :message              (str \" namespace \" " should not depend on " \" dependency-namespace \")})) dependencies)) should-not-depend-on)) namespace-related-rules)))

(defn- layer-cannot-access-dependency-layer?
  [config layer dependency-layer]
  (when-let [accesses-layers (get-in config [:layers layer :accesses-layers])]
    (not-any? (partial = dependency-layer) accesses-layers)))

(defn- dependency-layer-cannot-be-accessed-by-layer?
  [config dependency-layer layer]
  (when-let [accessed-by-layers (get-in config [:layers dependency-layer :accessed-by-layers])]
    (not-any? (partial = layer) accessed-by-layers)))

(defn- violate?
  [config
   {:keys [layer dependency-layer]}]
  (and (not= layer dependency-layer)
       (and (not (nil? layer))
            (not (nil? dependency-layer)))
       (or (dependency-layer-cannot-be-accessed-by-layer? config dependency-layer layer)
           (layer-cannot-access-dependency-layer? config layer dependency-layer))))

(defn- namespace-belongs-to-layer?
  [config namespace layer]
  (let [namespaces (get-in config [:layers layer :namespaces])
        defined-by (get-in config [:layers layer :defined-by])]
    (or (some #{namespace} namespaces)
        (when defined-by (re-find (re-pattern defined-by) (str namespace))))))

(defn- layer-by-namespace
  [config namespace]
  (some #(when (namespace-belongs-to-layer? config namespace %) %) (keys (:layers config))))

(defn- layer-and-namespace [config namespace dependency-namespace]
  (when-let [layer (layer-by-namespace config namespace)]
    {:namespace            namespace
     :layer                layer
     :dependency-namespace dependency-namespace
     :dependency-layer     (layer-by-namespace config dependency-namespace)}))

(defn- layer-violations
  [config namespace dependencies]
  (->> dependencies
       (map #(layer-and-namespace config namespace %))
       (filter #(violate? config %))
       (map (fn [{:keys [namespace dependency-namespace layer dependency-layer] :as violation}]
              (assoc violation :message (str \" namespace \" " should not depend on " \" dependency-namespace \" " (layer " \" layer \" " on " \" dependency-layer \" ")"))))))

(defn ^:private circular-dependency-violations
  [namespace dependencies dependencies-by-namespace]
  (->> dependencies-by-namespace
       (filter (fn [[k _]] (contains? dependencies k)))
       (filter (fn [[_ v]] (contains? v namespace)))
       (map (fn [[k _]] {:namespace namespace :message (str "Circular dependency between " \" namespace \" " and " \" k \")}))))

(defn- violations
  [config dependencies-by-namespace namespace]
  (let [dependencies (get dependencies-by-namespace namespace)
        circular-dependency-violations (circular-dependency-violations namespace dependencies dependencies-by-namespace)
        layer-violations (layer-violations config namespace dependencies)
        rule-violations (rule-violations config namespace dependencies)]
    (not-empty (concat circular-dependency-violations layer-violations rule-violations))))

(defn analyze
  "Analyze namespaces dependencies."
  [{:keys [config dependencies-by-namespace]}]
  (flatten (keep #(violations config dependencies-by-namespace %) (keys dependencies-by-namespace))))
