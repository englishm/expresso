(ns numeric.expresso.test-construct
  (:use numeric.expresso.construct)
  (:use clojure.test)
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic.protocols]
        [clojure.core.logic :exclude [is] :as l]
        [clojure.test])
  (:require [clojure.core.logic.fd :as fd])
  (:require [clojure.core.logic.unifier :as u]))



(deftest test-expo 
  (is (= [[1 2]] (run* [q] (fresh [ex op lhs rhs]
                                  (expo '+ [1 2] ex)
                                  (expo op [lhs rhs] ex)
                                  (== q [lhs rhs]))))))


(deftest test-ex
  (is (= '(clojure.core/+ 1 2 3) (ex (+ 1 2 3))))
  (is (= '(clojure.core/+ x y z a b) (ex (+ x y z a b))))
  (is (= '(clojure.core/+ x 3)) (let [x 3] (ex (+ x ~x)))))

(deftest test-ex'
  (is (= '(clojure.core/+ 1 2 3) (ex' (+ 1 2 3))))
  (is (= '(clojure.core/+ x y z a b) (ex' (+ 'x 'y 'z 'a 'b))))
  (is (= '(clojure.core/+ x y z a b) (ex' [x y z a b] (+ x y z a b))))
  (is (= '(clojure.core/+ c 3)) (let [x 3] (ex' [c] (+ c x)))))