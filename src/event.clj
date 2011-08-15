(ns event
  (:use
    [unit :only [units move-unit create-unit get-unit]]
    [select :only [selection selected-units select-unit sel-contains-unit? mouse-dragged]]
    [grid :only [resolve-to-square]])
  (:import
    (java.awt.event ActionListener)
    (java.awt.geom Point2D$Float)
    (java.awt.event MouseEvent MouseAdapter MouseMotionAdapter)))

;This is where we handle all GUI events

(def left-click-create-type (atom "Unit"))

(defn new-unit
  "Given a mouse event and a ctrl? flag (for the Ctrl keyboard key), create a new room and either select it or add it
   to the existing room selections"
  [mouse-event ctrl?]
  (select-unit (create-unit mouse-event) ctrl?))

(defn left-mouse [mouse-event]
  (let [ctrl? (.isControlDown mouse-event)]
    (if-let [unit (get-unit mouse-event)]
      (select-unit unit ctrl?)
      (new-unit mouse-event ctrl?)))
  (swap! selection assoc :start nil :end nil))

(defn right-mouse [mouse-event]
  ;(debug "resolving: x " (resolve-to-square (.getX mouse-event)) " y " (resolve-to-square (.getY mouse-event)))
  ;(swap! unit-moves concat (map (fn [unit] {:unit unit :move {:x (resolve-to-square (.getX mouse-event)) :y (resolve-to-square (.getY mouse-event))}}) @selected-units))
  (doseq [unit @selected-units]
    (-> (Thread. #(move-unit {:unit unit :move {:x (resolve-to-square (.getX mouse-event)) :y (resolve-to-square (.getY mouse-event))}}))
      (.start))
  ))

(def create-selection-action-listener
  (proxy (ActionListener) []
    (actionPerformed [actionEvent]
      (reset! left-click-create-type (.getActionCommand actionEvent)))))

(defn mouse-clicked [mouse-event]
  (if (= MouseEvent/BUTTON1 (.getButton mouse-event))
        (left-mouse mouse-event)
        (right-mouse mouse-event)))

(defn mouse-released [mouse-event] (swap! selection assoc :start nil :end nil))

(def mouse-adapter
  (proxy [MouseAdapter] []
    (mouseClicked [mouse-event] (mouse-clicked mouse-event))
    (mouseReleased [mouse-event] (mouse-released mouse-event))))

(def mouse-motion-adapter
  (proxy [MouseMotionAdapter] [] (mouseDragged [mouse-event] (mouse-dragged mouse-event))))


