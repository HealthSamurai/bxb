(ns mappers.simple-test
  (:require [mappers.simple :as sut]
            [matcho.core :as matcho]
            [clojure.test :refer :all]))

(def mapping
  {:path [:a :b]
   :vec1  [:a :vec {}]
   :vec2  [:a :vec {}]})

(defmacro match-m [subj pat]
  `(let [res# (mappers.simple/apply-mapping mapping ~subj {:nulls #{"None"}})]
     (matcho/match res# ~pat)))

(deftest mappers-simple-test
  (match-m {:path "a1"} {:a {:b "a1"}})
  (match-m {:path "None"} {})
  (match-m {:vec1 "a1"
            :vec2 "a2"} 
           {:a {:vec ["a1" "a2"]}})

  ;; TODO: add more tests


  )

