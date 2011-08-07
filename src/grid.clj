(ns grid)

(def square-size 18)
(def grid-size 100)

(defn resolve-to-square
  "Supposed to take a number and return it adjusted to align with a multiple of square-size (i.e. to align with a grid). Doesn't work well."
  [n]
  (- n (mod n square-size)))

(defn xy-from-square [x y] {:x (* x square-size) :y (* y square-size)})