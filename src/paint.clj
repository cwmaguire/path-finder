(ns paint
  (:use
    [unit :only [units repaints unit-ref-shape-xywh]]
    [select :only [selection selected-units unit-selected? union-selections selection-rectangle]]
    [path :only [pheromones]]
    [grid])
  (:require clojure.set)
  (:import
    (java.awt.geom Line2D$Float Rectangle2D$Float)
    (java.awt Color Rectangle)
    (javax.swing SwingUtilities JPanel)))

(def draw-grid? true)
(def paint-debug (ref nil))

(defn debug
  "Given a list, apply str to the list and append to paint-debug"
  [& strs]
  (dosync
    (ref-set paint-debug (apply str (interpose " " strs)))))

(defn in-clip?
  "Returns if a unit is in a clipping region shape. (Might be dupe of sel-contains-unit?)"
  [rect clip]
  (.intersects rect (.getX clip) (.getY clip) (.getWidth clip) (.getHeight clip)))

(defn coord-to-rect
  "Given a {:x _ :y _} map return a Rectangle2D$Float rectangle
  with grid-size width and height"
  [{:keys [x y]}]
  (Rectangle2D$Float. x y square-size square-size))

(defn draw-pheromone
  "Draw a square showing a pheromone level"
  [g2d coord level]
  (if (in-clip? (coord-to-rect coord) (.getClip g2d))
    (do
      (let [blue (- 255 (min 255 (* (dec level) 50)))]
        ;(debug "level" level "blue" blue)
        (.setColor g2d (Color. 0 blue blue)))
      ;(debug "draw-pheromone - coord rect: " (coord-to-rect coord))
      (.fill g2d (coord-to-rect coord)))))

(defn draw-pheromones
  "Draw all the pheromones for all the units"
  [g2d]
  (doseq [unit (keys @pheromones)]
    (doseq [point (keys (get @pheromones unit))]
      (draw-pheromone g2d point (get-in @pheromones [unit point])))))

(defmulti draw-unit (fn [_ unit] (:type @unit)))

(defmethod draw-unit :unit [g2d unit]
  (let [selected? (unit-selected? unit)
        color (if selected? Color/RED Color/GREEN)]
    (.setColor g2d color)
    (.fill g2d (:shape @unit))))

(defmethod draw-unit :obstacle [g2d unit]
    (.setColor g2d Color/BLACK)
    (.fill g2d (:shape @unit)))

;(defn draw-unit
;  "Draw a unit on the draw panel"
;  [g2d unit]
;  (let [selected? (unit-selected? unit)
;        color (if selected? Color/RED Color/WHITE)]
;    (.setColor g2d color)
;    (.fill g2d (:shape @unit))))

(defn draw-units
  "given a Graphics2D object, draw all units within the G2D clip"
  [g2d]
  (if-let [units @units]
    (do
      ;(debug "draw-units: units" units)
      ;(debug "draw-units: clip" (.getClip g2d))
      ;(debug "draw-units: first unit in clip" @(first (filter (fn [unit] in-clip? (:shape @unit) (.getClip g2d)) units)))

      (doall
        (map
          (fn [unit] (draw-unit g2d unit))
          (filter #(in-clip? (:shape @%) (.getClip g2d)) units)))
      )))

(defn draw-selection
  "Draw a selection rectangle"
  [g2d]
    (if-let [rect (selection-rectangle)]
        (do (.setColor g2d Color/BLUE)
            (.draw g2d rect))))

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

  (if draw-grid? (draw-grid panel g2d))

  (draw-pheromones g2d)

  (draw-units g2d)

  (draw-selection g2d))

;defined here so that it can be referred to here as well as in gui.clj
(def paint-panel (proxy (JPanel) [] (paintComponent [g2d] (draw-panel-paint this g2d))))

(defn repaint-draw-panel
  "Wrapper function for .repaint to allow (apply) to be used"
  [x y w h]
  (.repaint paint-panel x y w h))

(defn new-rectangle
  "given a Rectangle2D, create a Rectangle"
  [rect2d]
  (Rectangle. (int (.getX rect2d)) (int (.getY rect2d)) (int (.getWidth rect2d)) (int (.getHeight rect2d))))

(defn do-repaints
  "handle each required repaint"
  [_ ref _ new-state]
  (let [new-repaints new-state]

    ;(debug "do-repaints: " new-repaints)

    (if (seq new-repaints)
      (do
        (swap! repaints empty)
        (doseq [rect (map new-rectangle new-repaints)]
          (SwingUtilities/invokeLater (fn [] (.repaint paint-panel rect))))))))

(defn repaint-paint-panel
  "Repaints the entire paint panel"
  [& _]
  (.repaint paint-panel))

(defn unique
  "given two collections, return a set of elements unique to both collections; e.g. [1 2 3][3 4 5] -> [1 2 4 5]"
  [xs ys]
  ;could also use the frequencies function to look for elements with more than 1 occurrence
  (apply disj (set (concat xs ys)) (clojure.set/intersection (set xs) (set ys))))

(defn unit-dimensions
  "Given a seq of units, return a seq of vectors containing x y w h coordinates for a units shape"
  [unit-refs]
  (map unit-ref-shape-xywh unit-refs)
  )

(defn paint-changed-units
  "calls repaint for units that are added to or removed from the reference (e.g. selected units, all units)"
  [key ref units-then units-now]

  ; NOT called when a single unit is changed
  ;(debug "paint-changed-units")

  (let [units-to-repaint (unique units-then units-now)]
    (-> (Thread.
        (fn []
          (doseq [unit units-to-repaint]
            (apply repaint-draw-panel (unit-ref-shape-xywh unit 1))
          )) "paint-changed-units")
      (.start))))

(defn repaint-selection
  "repaints the area affected by the changing of the selection box"
  [key ref old-selection new-selection]
  (if-let [combined-selections (union-selections old-selection new-selection)]
    (.start (Thread. #(apply repaint-draw-panel (map + combined-selections [0 0 1 1]))))))

(add-watch selection ::selection-watch repaint-selection)
(add-watch units ::units-watch paint-changed-units)
(add-watch selected-units ::selected-units-watch paint-changed-units)
(add-watch repaints ::repaints-watch do-repaints)
(add-watch pheromones ::pheromones-watch repaint-paint-panel)