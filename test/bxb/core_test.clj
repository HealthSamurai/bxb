(ns bxb.core-test
  (:require [bxb.core :as sut]
            [clojure.test :refer :all]))


(deftest cannonical-test
  (is (= (sut/cannonical {:a 1})
         [[[:a] 1 {}]]))

  (is (= (sut/cannonical {:a {:b {:c 1}}})
         [[[:a :b :c] 1 {}]]))

  (is (= (sut/cannonical {:a [1 2 3]})
         [[[:a 0] 1 {}]
          [[:a 1] 2 {}]
          [[:a 2] 3 {}]]))

  (is (= (sut/cannonical {:a 1 :b 2})
         [[[:a] 1 {}]
          [[:b] 2 {}]]))


  )


(deftest path-transform

  (def in {:a {:c {:b 1} :d {:x 1}}})
  (def out {:a {:b {:c 1, :x {:d 1}}}})
  (is (=
       (sut/forward
        [[[:a 'x :b] [:a :b 'x]]
         [[:a 'x :x] [:a :b :x 'x]]]
        in)
       out))

  (is (=
       (sut/backward
        [[[:a 'x :b] [:a :b 'x]]
         [[:a 'x :x] [:a :b :x 'x]]]
        out)
       in))


  (sut/forward
   [[[:telecom 'i] [:contacts 'sys 'use]
     {'sys ['get '% :sys]
      'use ['get '% :use]
      '%   [['dissoc :sys 'sys :use 'use]
            ['assoc :order 'i]]}]]
   {:telecom [{:sys "phone" :use "home" :value "<home-phone>"}
              {:sys "phone" :use "work" :value "<work-phone>"}
              {:sys "mail"  :use "home" :value "<home-mail>"}]})

  {:contacts
   {:phone {:home {:order 0 :value "<home-phone>"}
            :work {:order 1 :value "<work-phone>"}}
    :mail  {:home {:order 0 :value "<home-mail>"}}}}


  )

