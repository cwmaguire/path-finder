
(ns unit
  (:use
    [geom :only [xy-delta]]
    [grid :only [square-size resolve-to-square]]
    [path :only [best-path]])
  (:import (java.awt.geom Rectangle2D$Float Point2D$Float)))

(def units (atom []))
;(def unit-moves (atom nil))
(def repaints (atom []))

(def unit-size square-size)

(def unit-debug (ref nil))

(defn debug
  "Given a list, apply str to the list and append to unit-debug"
  [& strs]
  (dosync
    (ref-set unit-debug (apply str (interpose " " strs)))))

(defn get-unit
  "Given a mouse event, return any unit at the mouse event x,y"
  [mouse-event]
  (let [p (Point2D$Float. (.getX mouse-event) (.getY mouse-event))]
    ;(debug (.getButton mouse-event))
    (first (filter #(. (:shape (deref %)) contains p) @units))))

(defn create-unit
  "Given a mouse event and a ctrl flag (for the Ctrl keyboard key), create a new unit and either select it or add it
   to the existing room selections"
  [mouse-event type]
  (let [new-unit (atom {:type type
                        :shape (Rectangle2D$Float. (resolve-to-square (.getX mouse-event)) (resolve-to-square (.getY mouse-event)) unit-size unit-size)})]
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
  [coord]
  ;(debug "occupied? " square)
  (seq (filter (fn [unit-ref] (= coord (get-coords unit-ref))) @units)))

(defn inc-rect
  "Given a rectangle return a rectangle that is one pixel bigger
  than the original towards 0,0"
  [rect]
  (Rectangle2D$Float. (dec (.getX rect)) (dec (.getY rect)) (+ 2 (.getWidth rect)) (+ 2 (.getHeight rect))))

(defn rect-coords
  "Debug: return the x,y,w,h of a rect2d$float"
  [rect]
  [(.getX rect) (.getY rect) (.getHeight rect) (.getWidth rect)])

(defn create-repaint-rect
  "takes two rectangles and returns the union"
  [rect1 rect2]
  (let [rect-union (inc-rect (.createUnion rect1 rect2))]
    ;(apply debug "create-repaint-rect: " (map rect-coords [rect1 rect2 rect-union]))
    rect-union))

(defn new-shape
  "create a new shape with the given coords"
  [{:keys [x y]}]
  (Rectangle2D$Float. x y unit-size unit-size))

(defn move-unit-along-path
  "Given a path, move the unit along the path"
  [unit path]

    (debug "move-unit-along-path: " path)
    
    (cond
      (not (seq path)) true ; we're there!
   
      (occupied? (first path)) false ; we got blocked

      :else
        (let [new-shape (new-shape (first path))
              old-shape (:shape @unit)]
          (swap! unit assoc :shape new-shape)
          (Thread/sleep 30)
          (swap! repaints conj (create-repaint-rect old-shape new-shape))
          (recur unit (next path)))))

(defn move-unit
  "Move a unit."
  [{:keys [unit move] :as unit-move}]

  ;(debug "move-unit; move: " move)

    (if (not (move-unit-along-path unit (best-path unit (get-coords unit) move occupied? square-size)))
      (recur unit-move)))



