(ns geom
  (:use
    [grid :only [square-size]]
    [clojure.contrib.math :only [abs expt]]
    [clojure.contrib.generic.math-functions :only [asin sqrt]]))

; asin of "soh" - opposite over hypotenuse
; rise over sqrt of rise squared + run squared
; abs y over sqrt of abs y square plus abs x squared
(defn move-angle
  "given the delta x and delta y, calculate the trigonometric angle
  of the direction of the movement"
  [dx dy]
  (cond
    (= dy 0) 90
    :default (Math/toDegrees (asin (/ (abs dy) (sqrt (+ (expt dy 2) (expt dx 2))))))))

(defn x-arc-delta
  "Assuming eight equal arcs of a circle representing eight compass
  directions starting with E being zero and ascending counter clockwise
  (e.g. NE = 1), return the x delta of a move in the direction represented by
  the supplied arc number"
  [arc]
  (cond
    (or (= arc 2) (= arc 6)) 0     ; N and S have zero x movement
    (and (> arc 2) (< arc 6)) -1   ; NW, W and SW all have -1 x movement
    (or 7  (< arc 2)) 1))          ; SE, E and NE all have +1 x movement

(defn y-arc-delta
  "Assuming eight equal arcs of a circle representing eight compass
  directions starting with E being zero and ascending counter clockwise
  (e.g. NE = 1), return the y delta of a move in the direction represented by
  the supplied arc number"
  [arc]
  (cond
    (or (= arc 0) (= arc 4)) 0   ; E and W have zero y movement
    (and (> arc 0) (< arc 4)) -1 ; NE, N and NW all have -1 y movement
    (> arc 4) 1))               ; SW, S and SE all have +1 y movement

(defn direction-arc
  "Given a degree of direction, return a numeric representation of the arc
  containing that degree starting with E (337.5 <= deg < 22.5) at 0 and
  proceeding counter-clockwise. For instance NE (22.5 <= deg < 67.5) is 1:
  E is 0, NE is 1, N is 2, NW is 3, W is 4, SW is 5, S is 6, SE is 7"
  [deg]
  ; shift all the direction arcs so that 0 <= deg < 45 lines up neatly
  ; with E (instead of 337.5 <= deg < 22.5 which isn't divided evenly by
  ; 45 and leaves E at "two ends of the circle" requiring two tests)
  (int (/ (mod (+ deg 22.5) 360) 45)))

(defn circle-degree
  "Given an angle, the dx and dy, calculate the angle of the circle"
  [dx dy deg]
  (cond
    (and (pos? dx) (pos? dy)) (- 360 deg)
    (and (pos? dx) (zero? dy)) 0
    (and (pos? dx) (neg? dy)) deg
    (and (zero? dx) (pos? dy)) 270
    (and (zero? dx) (neg? dy)) 90
    (and (neg? dx) (pos? dy)) (+ 180 deg)
    (and (neg? dx) (zero? dy)) 180
    (and (neg? dx) (neg? dy)) (+ 90 deg)))

(defn xy-delta
  "Given source and dest coordinates calculate the x delta and y delta to
  move in the appropriate compass direction"
  [x1 y1 x2 y2]
  (let [dx (- x2 x1)
        dy (- y2 y1)
        arc (direction-arc (circle-degree dx dy (move-angle dx dy)))]
    [(* square-size (x-arc-delta arc)) (* square-size (y-arc-delta arc))]))

(defn xywh
  "convert two points, or a selection containing two points, to a list containing the
   x, y, width and height of a rectangle formed by the points"
  ([point-one point-two]
    (let [x1 (.getX point-one) x2 (.getX point-two) y1 (.getY point-one) y2 (.getY point-two)]
      [(min x1 x2) (min y1 y2) (- (max x1 x2) (min x1 x2)) (- (max y1 y2) (min y1 y2))]))
  ([selection]
    (xywh (:start selection) (:end selection))))
