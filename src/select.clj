(ns select
  (:use [unit :only [units]])
  (:use [geom :only [xywh]])
  (:use [clojure.contrib.math :only [abs]])
  (:import (java.awt.geom Point2D$Float Rectangle2D$Float)))

(def selected-units (atom []))
(def selection (atom nil))

(defn selection-rectangle
  "Given two points, create a rectangle the encompasses both points. (Might be a dupe of xywh)"
  []
  (let [{:keys [start end]} @selection]
    (if (and start end)
      (let [start-x (.getX start)
            start-y (.getY start)
            end-x (.getX end)
            end-y (.getY end)]
        (Rectangle2D$Float. (min start-x end-x) (min start-y end-y) (abs (- start-x end-x)) (abs (- start-y end-y)))
        ))))

(defn sel-contains-unit?
  "Check if a unit is contained within a selection rectangle"
  [sel-rect unit]
  (let [unit-rect (.getBounds2D (:shape @unit))]
    (.contains sel-rect unit-rect)))

(defn recalc-selection
  "Update the collection of selected units when the selection box is changed"
  [ctrl?]
  ; check for Ctrl before clearing selection
  (let [units @units sel-rect (selection-rectangle)]
    (if sel-rect
      (do
        (if (not ctrl?)
          (swap! selected-units empty))
        (swap! selected-units concat (filter (partial sel-contains-unit? (selection-rectangle)) units))))))

(defn mouse-dragged
  "Handles updating room selections on mouse drag"
  [mouse-event] (
    ; store the end of the selection
    (swap! selection assoc :end (Point2D$Float. (.getX mouse-event) (.getY mouse-event)))

    ; store the start of the selection if we haven't already
    (let [start (:start @selection)]
      (if (nil? start)
        (swap! selection assoc :start (Point2D$Float. (.getX mouse-event) (.getY mouse-event)))))

    (recalc-selection (.isControlDown mouse-event))))

(defn unit-selected?
  "Check if a unit is in the selected units collection"
  [unit]
  (some #(= % unit) @selected-units))

(defn union-selections
  "takes two selections or four points and returns a rectangle that encompasses both/all of them;
   handles an empty new selection (i.e. selection is cancelled)"
  ([sel-old sel-new]
    (cond
      (:start sel-new) (union-selections (:start sel-new) (:end sel-new) (:start sel-old) (:end sel-old))
      (:start sel-old) (xywh sel-old)
      :default nil))

  ([p1 p2 p3 p4]
    (let [[[x1 y1] [x2 y2] [x3 y3] [x4 y4]] (map (fn [p] [(.getX p) (.getY p)]) [p1 p2 p3 p4])
          min-x (min x1 x2 x3 x4)
          min-y (min y1 y2 y3 y4)
          max-x (max x1 x2 x3 x4)
          max-y (max y1 y2 y3 y4)]
      [min-x min-y (- max-x min-x) (- max-y min-y)])))

(defn select-unit
  "Create a new selection with this unit or add it to the existing selection based on the Ctrl key flag"
  [unit ctrl?]
  (if (not ctrl?)
    (swap! selected-units empty))

  ; remove selected unit from selection on ctrl+LMB
  (if (unit-selected? unit)
      (swap! selected-units (partial remove #(= unit %)))
      (swap! selected-units conj unit)))

