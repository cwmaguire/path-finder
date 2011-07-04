
(ns unit
  (:use [geom :only [xy-delta]])
  (:use [grid :only [square-size resolve-to-square]])
  (:import (java.awt.geom Rectangle2D$Float Point2D$Float)))

(def units (atom []))
;(def unit-moves (atom nil))
(def repaints (atom []))

(def unit-size (* 3 square-size))

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
    (swap! units conj new-unit)))

(defn unit-ref-shape-xywh
  "Given unit ref, return the x, y, w, h of the unit's shape"
  ([unit-ref] (unit-ref-shape-xywh unit-ref 0))
  ([unit-ref padding] (let [shape (:shape @unit-ref)] [(.getX shape) (.getY shape) (+ padding (.getWidth shape)) (+ padding (.getHeight shape))])))


(defn move-unit
  "Work in progress. Move a unit. "
  [unit-move]
  (let [{:keys [unit move] :as unit-move} unit-move
        shape (:shape @unit)
        x-orig (.getX shape)
        y-orig (.getY shape)
        h (.getHeight shape)
        w (.getWidth shape)
        x-move (:x move)
        y-move (:y move)
        [dx dy] (xy-delta x-orig y-orig x-move y-move)
        new-shape (Rectangle2D$Float. (+ x-orig dx) (+ y-orig dy) w h)
        x-dest (.getX new-shape)
        y-dest (.getY new-shape)]
    ;(debug "Moving unit from [" x-orig "," y-orig "] to [" x-dest "," y-dest "]")
    (swap! unit assoc :shape new-shape)

    ; store clipping range to redraw any area where a unit has moved
    (swap! repaints conj {:x (min x-orig x-dest) :y (min y-orig y-dest) :w (+ unit-size (- (max x-orig x-dest) (min x-orig x-dest))) :h (+ unit-size (- (max y-orig y-dest) (min y-orig y-dest)))})
    ))
