(ns paint
  (:use [unit :only [units repaints unit-ref-shape-xywh]])
  (:use [select :only [selection selected-units unit-selected? union-selections selection-rectangle]])
  (:use [grid])
  ;(:use [gui :only [draw-panel]])
  (:import
    (java.awt.geom Line2D$Float)
    (java.awt Color)
    (javax.swing SwingUtilities JPanel)))

(def draw-grid? true)

(defn in-clip?
  "Returns if a unit is in a clipping region shape. (Might be dupe of sel-contains-unit?)"
  [clip unit]
  (.intersects (:shape @unit) (.getX clip) (.getY clip) (.getWidth clip) (.getHeight clip)))

(defn draw-unit
  "Draw a unit on the draw panel"
  [g2d unit]
  (let [selected (unit-selected? unit) color (if selected Color/RED Color/BLACK)]
    (.setColor g2d color)
    (.draw g2d (:shape @unit))))

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

      (let [units @units
            selection @selection
            sel-start (:start selection)
            sel-end (:end selection)]
        (if units
          (doall (map (fn [unit] (draw-unit g2d unit)) (filter (partial in-clip? (.getClip g2d)) units))))

        (draw-selection g2d)))

;defined here so that it can be referred to here as well as in gui.clj
(def paint-panel (proxy (JPanel) [] (paintComponent [g2d] (draw-panel-paint this g2d))))

(defn repaint-draw-panel
  "Wrapper function for .repaint to allow (apply) to be used"
  [x y w h]
  (.repaint paint-panel x y w h))

(defn do-repaints
  "handle each required repaint"
  [key ref old-state new-state]
  (let [repaints new-state]
    (swap! repaints empty)
    (doseq [[x y w h] repaints]
      (.repaint @paint-panel x y w h))))

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
  (let [units-to-repaint (unique units-then units-now)]
    (-> (Thread.
        (fn []
          (doseq [unit units-to-repaint]
            (apply repaint-draw-panel (unit-ref-shape-xywh unit 1))
          )) "paint-changed-units")
      (.start))))

; !! use a watch to do the repainting
;(defn draw-moves
;  "Performs all moves and then repaints resulting clips"
;  []
;  (doseq [{:keys [x y w h]} (do-moves)] (debug "redraw clip x: " x " y: " y " w: " w " h: " h)(.repaint draw-panel x y (+ w 1) (+ h 1)) )
;
  ;!! remove finished moves
;  (swap! unit-moves empty))

(defn repaint-selection
  "repaints the area affected by the changing of the selection box"
  [key ref old-selection new-selection]
  (if-let [combined-selections (union-selections old-selection new-selection)]
    (.start (Thread. #(apply repaint-draw-panel (map + combined-selections [0 0 1 1]))))
    (prn "selection is nil")))

; not sure why this works even though we're not deref'ing the future
(add-watch selection "selection-watch" repaint-selection)
(add-watch units ::units-watch paint-changed-units)
(add-watch selected-units ::selected-units-watch paint-changed-units)
(add-watch repaints ::repaints-watch do-repaints)