
(ns unit
  (:use [geom :only [xy-delta]])
  (:use [grid :only [square-size resolve-to-square]])
  (:use [path :only [best-path]])
  (:import (java.awt.geom Rectangle2D$Float Point2D$Float)))

(def units (atom []))
;(def unit-moves (atom nil))
(def repaints (atom []))

(def unit-size square-size)

(defn get-unit
  "Given a mouse event, return any unit at the mouse event x,y"
  [mouse-event]
  (let [p (Point2D$Float. (.getX mouse-event) (.getY mouse-event))]
    ;(debug (.getButton mouse-event))
    (first (filter #(. (:shape (deref %)) contains p) @units))))

(defn create-unit
  "Given a mouse event and a ctrl flag (for the Ctrl keyboard key), create a new room and either select it or add it
   to the existing room selections"
  [mouse-event]
  (let [new-unit (atom {:shape (Rectangle2D$Float. (resolve-to-square (.getX mouse-event)) (resolve-to-square (.getY mouse-event)) unit-size unit-size)})]
    (swap! units conj new-unit)
    new-unit))

(defn unit-ref-shape-xywh
  "Given unit ref, return the x, y, w, h of the unit's shape"
  ([unit-ref] (unit-ref-shape-xywh unit-ref 0))
  ([unit-ref padding]
    (let [shape (:shape @unit-ref)]
      [(.getX shape)
       (.getY shape)
       (+ padding (.getWidth shape))
       (+ padding (.getHeight shape))])))

(defn get-coords
  "return the coordinates of a unit ref"
  [unit]
  (let [shape (:shape @unit)]
    {:x (.getX shape) :y (.getY shape)}))

(defn occupied?
  "Given the coordinates of a square, return if it's occupied by a unit"
  [square]
  (filter (fn [unit-ref] (= square (get-coords unit-ref))) @units))

(defn create-repaint-rect
  "takes two rectangles and returns the union"
  [rect1 rect2]
  (.createUnion rect1 rect2))

(defn new-shape
  "create a new shape with the given coords"
  [{:keys [x y]} coords]
  (Rectangle2D$Float. x y unit-size unit-size))

(defn move-unit-along-path
  "Given a path, move the unit along the path"
  ([unit path]
    (if (not (seq path)) true) ; we're there!
    (if (occupied? (first path)) false) ; we got blocked

    (let [new-shape (new-shape (first path))]
      (swap! unit assoc :shape new-shape)
      (swap! repaints conj (create-repaint-rect (.createUnion (:shape @unit) new-shape)))
      (Thread/sleep 200)
      (recur unit (next path))
      )))

(defn move-unit
  "Move a unit."
  [{:keys [unit move] :as unit-move}]
    (if (not (move-unit-along-path unit-move (best-path unit move occupied?)))
      (recur unit-move)))



