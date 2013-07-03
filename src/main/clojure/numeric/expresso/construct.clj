(ns numeric.expresso.construct
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic.protocols]
        [clojure.core.logic :exclude [is] :as l]
        [clojure.test])
  (:require [clojure.core.logic.fd :as fd]
            [clojure.walk :as walk]
            [clojure.core.logic.unifier :as u]
            [numeric.expresso.utils :as utils]))
(declare ex*)

(defn- express-list 
  ([[op & exprs]]
    (cons op (map ex* exprs))))

(defn ex* 
  ([expr]
    (cond 
      ;; an operation with child expressions
      (sequential? expr)
        (let [childs (express-list expr)]
          childs)
        
      ;; a symbol
      (symbol? expr)
        expr
        
      ;; else must be a constant
      :else
        expr)))

(defn properties [s-exp]
  (:properties (meta (first s-exp))))

(defn propertieso [s-exp q]
  (project [s-exp]
           (== q (properties s-exp))))

(defmulti props identity)
(defmethod props :default [_] nil)
(defmethod props 'clojure.core/* [_] [:associative :commutative :n-ary])
(defmethod props 'clojure.core/+ [_] [:associative :commutative :n-ary])
(defmethod props 'clojure.core/- [_] [:n-ary [:inverse-of 'clojure.core/+]])
(defmethod props 'clojure.core// [_] [:n-ary [:inverse-of 'clojure.core/*]])

#_(def props {'clojure.core/* [:associative :commutative :n-ary]
            'clojure.core/+ [:associative :commutative :n-ary]
            'clojure.core/- [:n-ary [:inverse-of 'clojure.core/+]]
            'clojure.core// [:n-ary [:inverse-of 'clojure.core/*]]})

(defn extract [c]
  (mapcat #(if (and (coll? %) (= (first %) ::seq-match)) (second %) [%]) c))


(defn splice-in-seq-matchers [expr]
  (walk/postwalk (fn [expr] (if (coll? expr) (extract expr) expr)) expr))

(defn ex [symb & args]
  (list* (with-meta symb {:properties (props symb)}) args))

(def °)

(defn expo 
  "Creates an expression with the given operator and parameters"
  ([op params exp]
    (conso op params exp)))

(derive 'e/ca+ 'e/ca-op)
(derive 'e/ca* 'e/ca-op)
(derive 'clojure.core/+ 'e/ca+)
(derive 'clojure.core/* 'e/ca*)
(derive 'clojure.core/- 'e/-)
(derive 'clojure.core// 'e/div)
(derive `° 'e/ao-op)


(defn var-to-symbol [v]
  (let [s (str v)
        erg (-> (.substring s 2 (.length s)) symbol)]
    erg))


(defn replace-with-expresso-sexp [s s-exp]
  (if (and (coll? s-exp) (s (first s-exp)))
    (let [f (first s-exp)
          symb (if-let [r (resolve f)] (var-to-symbol r) f)]
      (list* `ex (list 'quote symb) (rest s-exp)))
    s-exp))

(defmacro with-expresso [s & code]
  (let [s-set (set s)]
    `(do 
       ~@(clojure.walk/postwalk #(replace-with-expresso-sexp s-set %) code))))



