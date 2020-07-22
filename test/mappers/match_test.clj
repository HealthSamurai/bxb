(ns mappers.match-test
  (:require
   [mappers.match :as sut]
   [clojure.test :as t]))

(t/deftest test-match

  (t/is (sut/match {:a 1 :b 1} {:a 1}))
  (t/is (not (sut/match {:a 2 :b 1} {:a 1})))


  (t/is (sut/match {:a {:b 1 :c 2} :b 1} {:a {:b 1}}))
  (t/is (not (sut/match {:a {:b 2} :b 1} {:a {:b 1}})))


  (t/is (sut/match [:a :b] [:a]))
  (t/is (not (sut/match [:a :b] [:b :a])))

  (t/is (sut/match [{:a 1} {:a 2}] [{:a 1}]))
  (t/is (not (sut/match [{:a 1} {:a 2}] [{:a 2} {:a 1}])))

  )
