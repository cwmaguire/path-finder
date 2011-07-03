(ns paint
  (import
    (java.awt.geom Line2D$Float)
    (java.awt Color)))

(def square-size 6)
(def draw-grid? true)

(defn draw-grid
  "Draw a grid on the draw-panel; this is for working on path finding and would be in the final version"
  [panel g2d]
  (let [clip (.getClip g2d)
        x (.getX clip)
        y (.getY clip)
        w (.getWidth clip)
        h (.getHeight clip)
        max-x (+ x w)
        max-y (+ y h)
        prev-x (- x (mod x square-size))
        next-x (- (+ max-x square-size) (mod (+ max-x square-size) square-size))
        prev-y (- y (mod y square-size))
        next-y (- (+ max-y square-size) (mod (+ max-y square-size) square-size))]

    ; blank out clipped region
    (.setColor g2d Color/WHITE)
    (.fill g2d clip)

    (.setColor g2d (Color. 240 240 240))
    (doseq [line (map #(Line2D$Float. % %2 % %3) (range prev-x next-x square-size) (repeat y) (repeat max-y))]
      (.draw g2d line))
    (doseq [line (map #(Line2D$Float. %2 % %3 %) (range prev-y next-y square-size) (repeat x) (repeat max-x))]
      (.draw g2d line))))

(defn draw-panel-paint
  "Function to repaint the dirty regions of the draw panel"
  [panel g2d]
      (if draw-grid? (draw-grid panel g2d)))
