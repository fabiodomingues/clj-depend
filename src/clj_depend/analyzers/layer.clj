(ns clj-depend.analyzers.layer)

(defn ^:private layer-cannot-access-dependency-layer?
  [config layer dependency-layer]
  (when-let [accesses-layers (get-in config [:layers layer :accesses-layers])]
    (not-any? (partial = dependency-layer) accesses-layers)))

(defn ^:private dependency-layer-cannot-be-accessed-by-layer?
  [config dependency-layer layer]
  (when-let [accessed-by-layers (get-in config [:layers dependency-layer :accessed-by-layers])]
    (not-any? (partial = layer) accessed-by-layers)))

(defn ^:private violate?
  [config
   {:keys [layer dependency-layer]}]
  (and (not= layer dependency-layer)
       (and (not (nil? layer))
            (not (nil? dependency-layer)))
       (or (dependency-layer-cannot-be-accessed-by-layer? config dependency-layer layer)
           (layer-cannot-access-dependency-layer? config layer dependency-layer))))

(defn ^:private namespace-belongs-to-layer?
  [config namespace layer]
  (let [namespaces (get-in config [:layers layer :namespaces])
        defined-by (get-in config [:layers layer :defined-by])]
    (or (some #{namespace} namespaces)
        (when defined-by (re-find (re-pattern defined-by) (str namespace))))))

(defn ^:private layer-by-namespace
  [config namespace]
  (some #(when (namespace-belongs-to-layer? config namespace %) %) (keys (:layers config))))

(defn ^:private layer-and-namespace [config namespace dependency-namespace]
  (when-let [layer (layer-by-namespace config namespace)]
    {:namespace            namespace
     :layer                layer
     :dependency-namespace dependency-namespace
     :dependency-layer     (layer-by-namespace config dependency-namespace)}))

(defn analyze
  [config namespace dependencies]
  (->> dependencies
       (map #(layer-and-namespace config namespace %))
       (filter #(violate? config %))
       (map (fn [{:keys [namespace dependency-namespace layer dependency-layer] :as violation}]
              (assoc violation :message (str \" namespace \" " should not depend on " \" dependency-namespace \" " (layer " \" layer \" " on " \" dependency-layer \" ")"))))))
