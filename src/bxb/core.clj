(ns bxb.core)

(defn *cannonize [data pth]
  (cond
    (map? data) (->> data
                     (mapcat (fn [[k v]] (*cannonize v (conj pth k))))
                     (into []))

    (sequential? data) (->> data
                            (map-indexed
                             (fn [k v]
                               (*cannonize v (conj pth k))))
                            (mapcat identity)
                            (into []))
    :else [[pth data {}]]))

(defn cannonical [data]
  (*cannonize data []))

(defn datify [els]
  (reduce (fn [acc [pth v _]]
            (assoc-in acc pth v)
            ) {} els))

(defn matches? [[e-pth v opts] pth]
  (println e-pth v pth)
  (loop [[pi & pis] pth
         [ei & eis] e-pth
         vars       {}]
    (cond
        (nil? pi) [pth v vars]
        (= ei pi) (recur pis eis vars)
        (symbol? pi) (recur pis eis (assoc vars pi ei)))))

(defn transform-el [[e-pth v vars] pth]
  [(reduce (fn [to-pth i]
             (conj to-pth 
                   (if (symbol? i)
                     (if-let [k (get vars i)]
                       k
                       (throw (Exception. (str "Could not resolve " i))))
                     i))
             ) [] pth)
   v vars])

(defn apply-tr [[from to _] els & [back]]
  (reduce (fn [acc el]
            (conj acc
                  (if-let [el' (matches? el (if back to from))]
                    (transform-el el' (if back from to))
                    el)))
          [] els))

(defn forward [trs data]
  (datify
   (reduce (fn [acc tr]
             (apply-tr tr acc))
           (cannonical data)
           trs)))

(defn backward [trs data]
  (datify
   (reduce (fn [acc tr]
             (apply-tr tr acc true))
           (cannonical data)
           (reverse trs))))
