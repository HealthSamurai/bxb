(ns repatch.core-test
  (:require [clojure.test :refer :all])
  (:require [repatch.core :as sut :refer [diff patch]]))

(defmacro diff=
  ([old new expected]
   `(let [r# (diff ~old ~new)]
      (is (= r# ~expected))
      r#))
  ([ctx old new expected]
   `(let [r# (diff ~ctx ~old ~new)]
      (is (= r# ~expected))
      r#)))

(defmacro patch=
  ([obj patch expected]
   `(let [r# (patch ~obj ~patch)]
      (is (= r# ~expected))
      r#)))

(deftest diff-test
  (diff= {:a 1} {:a 2} {:a 2})
  (patch= {:a 1} {:a 2} {:a 2})


  (diff= {:a 1 :b 2} {:a nil :b 2} {:a ["delete" 1]})
  (patch= {:a 1 :b 2}
          {:a ["delete" 1]}
          {:b 2} )

  (diff= {:a 1} {:a 1} nil)
  (patch= {:a 1} nil {:a 1})

  (diff= {:a 1 :b 1} {:a 1 :b 2} {:b 2})
  (patch= {:a 1 :b 1} {:b 2} {:a 1 :b 2})

  (diff= {:x {:a 1} :y 1} {:x {:a 2}}
         {:x {:a 2}})
  (patch= {:x {:a 1} :y 1} {:x {:a 2}}
          {:x {:a 2} :y 1})

  (diff= {:x {:a 1 :b 1} :y 1} {:x {:a 1 :b 2}}
         {:x {:b 2}})
  (patch= {:x {:a 1 :b 1} :y 1} {:x {:b 2}}
          {:x {:a 1 :b 2} :y 1})

  (diff= {:x {:a 1}} {:x nil}
         {:x ["delete" {:a 1}]})
  (patch= {:x {:a 1} :y 1} {:x ["delete"]}
          {:y 1})

  (patch= {:x {:a 1} :y 1} {:x ["delete"]}
          {:y 1})

  (diff= {:given ["A" "B"]}
         {:given ["A" "B"]}
         nil)

  (diff= {:given ["A" "B"]}
         {:given ["A" "C"]}
         {:given ["change" ["A" "B"] ["A" "C"]]})
  (patch= {:given ["A" "B"]}
          {:given ["change" ["A" "B"] ["A" "C"]]}
          {:given ["A" "C"]})

  (diff= {:given ["A" "B"]}
         {:given ["A" nil]}
         {:given ["change" ["A" "B"] ["A"]]})

  (patch= {:given ["A" "B"]}
          {:given ["change" ["A" "B"] ["A"]]}
          {:given ["A"]})

  (diff= {:schema {:tags {:type :set}}}
         {:tags ["tag-1" "tag-2"]}
         {:tags ["tag-2" "tag-3"]}
         {:tags ["batch" ["conj" "tag-3"] ["disj" "tag-1"]]})

  (patch= {:tags ["tag-1" "tag-2"]}
          {:tags ["batch" ["conj" "tag-3"] ["disj" "tag-1"]]}
          {:tags ["tag-2" "tag-3"]})

  (patch= {:tags ["tag-1" "tag-x" "tag-2"]}
          {:tags ["disj" "tag-x"]}
          {:tags ["tag-1" "tag-2"]})

  (patch= {:tags ["tag-1" "tag-2"]}
          {:tags ["conj" "tag-3"]}
          {:tags ["tag-1" "tag-2" "tag-3"]})

  (patch= {:tags ["tag-1" "tag-2"]}
          {:tags ["conj" "tag-2"]}
          {:tags ["tag-1" "tag-2"]})

  (diff= {:name [{:given "A"} {:given "B"}]}
         {:name [{:given "A"} {:given "C"}]}
         {:name ["nth" 1 ["patch" {:given "C"}]]})

  (patch= {:a {:b 1}}
          {:a ["patch" {:b 2}]}
          {:a {:b 2}})

  (patch= {:name [{:given "A"} {:given "B"}]}
          {:name ["nth" 1 ["patch" {:given "C"}]]}
          {:name [{:given "A"} {:given "C"}]})

  (diff= {:name [{:given "A"} {:given "B"}]}
         {:name [{:given "A"}]}
         {:name ["nth" 1 ["delete" {:given "B"}]]})

  (patch= {:name [{:given "A"} {:given "B"}]}
          {:name ["nth" 1 ["delete" {:given "B"}]]}
          {:name [{:given "A"}]})

  (diff= {:name [{:given "A"} {:given "B"}]}
         {:name []}
         {:name ["batch"
                 ["nth" 0 ["delete" {:given "A"}]]
                 ["nth" 1 ["delete" {:given "B"}]]]})

  (diff= {:name [{:given "A"} {:given "B"}]}
         {:name [{:given "X"} {:given "Y"} {:given "Z"}]}
         {:name ["batch"
                 ["nth" 0 ["patch" {:given "X"}]]
                 ["nth" 1 ["patch" {:given "Y"}]]
                 ["nth" 2 ["change" nil {:given "Z"}]]]})

  (patch= {:name [{:given "A"} {:given "B"}]}
          {:name ["batch"
                  ["nth" 0 ["patch" {:given "X"}]]
                  ["nth" 1 ["patch" {:given "Y"}]]
                  ["nth" 2 ["change" nil {:given "Z"}]]]}
          {:name [{:given "X"} {:given "Y"} {:given "Z"}]})

  (diff= {:name [{:given "A"} {:given "B"}]}
         {:name [{:given "C"} {:given "A"}]}
         {:name ["batch"
                 ["nth" 0 ["patch" {:given "C"}]]
                 ["nth" 1 ["patch" {:given "A"}]]]})

  (patch= {:name [{:given "A"} {:given "B"}]}
          {:name ["batch"
                  ["nth" 0 ["patch" {:given "C"}]]
                  ["nth" 1 ["patch" {:given "A"}]]]}
          {:name [{:given "C"} {:given "A"}]})

  (diff= {:schema {:x {:type :key :key :system}}}
         {:x [{:system "ssn" :value 1} {:system "mrn" :value 1}]}
         {:x [{:system "mrn" :value 1} {:system "ssn" :value 2}]}
         {:x ["find" {:system "ssn"} ["patch" {:value 2}]]})

  (patch= {:x [{:system "ssn" :value 1} {:system "mrn" :value 1}]}
          {:x ["find" {:system "ssn"} ["patch" {:value 2}]]}
          {:x [{:system "ssn" :value 2} {:system "mrn" :value 1}]})

  (patch= {:x [ {:system "mrn" :value 1}]}
          {:x ["find" {:system "ssn"} ["patch" {:value 2}]]}
          {:x [{:system "mrn" :value 1} {:system "ssn" :value 2}]})

  (diff= {:schema {:x {:type :key :key :system}}}
         {:x [{:system "ssn" :value 1}]}
         {:x [{:system "ssn" :value 2}]}
         {:x ["find" {:system "ssn"} ["patch" {:value 2}]]})

  (patch= {:x [{:system "ssn" :value 1}]}
          {:x ["find" {:system "ssn"} ["patch" {:value 2}]]}
          {:x [{:system "ssn" :value 2}]})

  (diff= {:schema {:x {:type :key :key :system}}}
         {:x [{:system "mrn" :value 1} {:system "ssn" :value 1}]}
         {:x [{:system "mrn" :value 1} {:system "ssn" :value 2}]}
         {:x ["find" {:system "ssn"} ["patch" {:value 2}]]})

  (patch= {:x [{:system "mrn" :value 1} {:system "ssn" :value 1}]}
          {:x ["find" {:system "ssn"} ["patch" {:value 2}]]}
          {:x [{:system "mrn" :value 1} {:system "ssn" :value 2}]})

  (diff= {:schema {:x {:type :key :key :system}}}
         {:x [{:system "mrn" :value 1} {:system "ssn" :value 1}]}
         {:x [{:system "mrn" :value 1}]}
         {:x ["find" {:system "ssn"} ["delete" {:system "ssn", :value 1}]]})

  (patch= {:x [{:system "mrn" :value 1} {:system "ssn" :value 1}]}
          {:x ["find" {:system "ssn"} ["delete" {:system "ssn", :value 1}]]}
          {:x [{:system "mrn" :value 1}]})

  (diff= {:schema {:x {:type :key :key :system}}}
         {:x []}
         {:x [{:system "mrn" :value 1}]}
         {:x ["conj" {:system "mrn", :value 1}]})
  (patch= {:x []}
          {:x ["conj" {:system "mrn", :value 1}]}
          {:x [{:system "mrn" :value 1}]})
  
  
  (diff= {:schema {:x {:type :key :key :system}}}
         {}
         {:x [{:system "mrn" :value 1}]}
         {:x ["into" [{:system "mrn", :value 1}]]})

  (patch= {}
          {:x ["into" [{:system "mrn", :value 1}]]}
          {:x [{:system "mrn" :value 1}]})

  (patch= {:x [{:system "ssn" :value 1}]}
          {:x ["conj" {:system "mrn", :value 1}]}
          {:x [{:system "ssn" :value 1}
               {:system "mrn" :value 1}]})

  ;; TODO: handle this case
  ;; (patch= {:x [{:system "mrn" :value 1}]}
  ;;         {:x ["conj" {:system "mrn", :value 2}]}
  ;;         {:x [{:system "ssn" :value 2}]})



  (diff= {:schema {:x {:type :key :key :system}}}
         {:x [{:system "ssn" :value 1}]}
         {:x [{:system "mrn" :value 1}]}
         {:x ["batch"
              ["find" {:system "ssn"} ["delete" {:system "ssn", :value 1}]]
              ["conj" {:system "mrn", :value 1}]]})

  (patch= {:x [{:system "ssn" :value 1}]}
          {:x ["batch"
               ["find" {:system "ssn"} ["delete" {:system "ssn", :value 1}]]
               ["conj" {:system "mrn", :value 1}]]}
          {:x [{:system "mrn" :value 1}]})

  (diff= {:schema {:patient {:coverages {:type :key
                                         :key  :id}}}}
         {:id "C1",
          :name [{:given ["Jhon" "M"], :family "Old"}],
          :patient {:id "pt-1",
                    :coverages [{:id "cov-1",
                                 :group_id "group-id-1"}]}}
         {:id "C1",
          :name [{:given ["Jhon" "M"], :family "Changed"}],
          :patient {:id "pt-1",
                    :coverages [{:id "cov-1",
                                 :group_id "changed-group-id-1"}]}}

         {:name ["nth" 0 ["patch" {:family "Changed"}]],
          :patient {:coverages
                    ["find"
                     {:id "cov-1"}
                     ["patch" {:group_id "changed-group-id-1"}]]}})

  )



