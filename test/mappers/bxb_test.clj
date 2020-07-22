(ns mappers.bxb-test
  (:require [mappers.bxb :as sut]
            [clojure.test :as t]
            [matcho.core :as matcho]))

(defmacro *get= [src pth patt]
  `(let [res# (sut/*get ~src ~pth)]
     (do (t/is (= res# ~patt))
         res#)))

(defmacro *put= [dst pth v patt]
  `(let [res# (sut/*put ~dst ~pth ~v)]
     (do (t/is (= res# ~patt))
         res#)))

(defmacro xget=
  ([mapping src default patt]
   `(let [res# (sut/xget ~mapping ~src ~default)]
      (do (matcho/match res# ~patt)
          res#)))
  ([mapping src patt]
   `(let [res# (sut/xget ~mapping ~src)]
      (do (matcho/match res# ~patt)
          res#))))

(defmacro xput=
  ([mapping src default patt]
   `(let [res# (sut/xput ~mapping ~src ~default)]
      (do (matcho/match res# ~patt)
          res#)))
  ([mapping src patt]
   `(let [res# (sut/xput ~mapping ~src)]
      (do (matcho/match res# ~patt)
          res#))))

(t/deftest test-bxb

  (*get= {:a 1} [:a] 1)
  (*get= {:a {:b {:c 1}}} [:a :b :c] 1)

  (*get= [1] [0] 1)
  (*get= {:a [{:b [:x :x]}
              {:b [0 1]}]}
         [:a 1 :b 1] 1)

  (*get= {:a [{:sys "a" :v 0} {:sys "b" :v 1}]}
         [:a [:map-by {:sys "b"}] :v]
         1)

  (*get= {:a [{:sys "a" :v 0} {:sys "b" :v 1}]}
         [:a [:map-by {:sys "x"}] :v]
         nil)

  (*put= {} [:a] 1 {:a 1})

  (*put= {} [:a :b] 1 {:a {:b 1}})

  (*put= {} [:a 0 :b] 1 {:a [{:b 1}]})

  ;; (*put= {} [:a 1] 1 {:a [nil 1]})

  (*put= {} [:a [:map-by {:sys "b"}] :v] 1
         {:a [{:sys "b" :v 1}]})

  (*put= {:a [{:sys "a" :v 0}]} [:a [:map-by {:sys "b"}] :v] 1
         {:a [{:sys "a" :v 0}
              {:sys "b" :v 1}]})

  (*put= {:a [{:sys "b" :v 0}]}
         [:a [:map-by {:sys "b"}] :v] 1
         {:a [{:sys "b" :v 1}]})

  (def mapping
    [[:name 0 :family]  [:family]
     [:name 0 :given 0] [:given]
     [:identifier [:map-by {:system "npi"}] :value] [:npi]])

  (def src {:name [{:family "smith" :given ["john" "m"]}
                   {:family "second"}]
            :birthDate "bd"
            :identifier [{:system "amd" :value "amd-id"}
                         {:system "npi" :value "npi-id"}]})

  (def dest {:family "smith" :given "john" :npi "npi-id"})

  (xget= mapping src
         {:family "smith", :given "john", :npi "npi-id"})

  (xget= mapping src {:justified "ups"}
         {:family "smith", :given "john", :npi "npi-id" :justified "ups"})

  (xput= mapping dest
         {:name [{:family "smith", :given ["john"]} nil?],
          :birthDate nil?
          :identifier [{:system "npi", :value "npi-id"}]})

  (xput= mapping
         {:family "smith*", :given "john*", :npi "npi-id*" :justified "ups"}
         src
         {:name [{:family "smith*", :given ["john*" "m"]}
                 {:family "second"}],
          :birthDate "bd"
          :identifier [{:system "amd"}
                       {:system "npi", :value "npi-id*"}]})

  (t/is (= {}
       (sut/xput [[:a] [:b]] {})))
  
  (t/is (= ::sut/nil
           (sut/*get {:a nil} [:a] {:preserve-nil? true})))

  (t/is (= ::sut/nil
           (sut/*get {:a {:b nil}} [:a :b] {:preserve-nil? true})))

  (t/is (= {:a nil}
           (sut/xput [[:a] [:b]] {:b nil})))

  (t/is (= {:a {:d nil}}
           (sut/xput [[:a :d] [:b :c]] {:b {:c nil}})))

  (t/is (= {}
           (sut/xput [[:a :d] [:b :c]] {:b {}})))


  )
