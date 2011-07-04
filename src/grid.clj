(ns grid)

(def square-size 6)

(defn resolve-to-square
  "Supposed to take a number and return it adjusted to align with a multiple of square-size (i.e. to align with a grid). Doesn't work well."
  [n]
  (- n (mod n square-size)))
