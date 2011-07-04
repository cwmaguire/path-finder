(ns gui
  (:use [paint :only [paint-panel draw-panel-paint]])
  (:use [event :only [mouse-adapter mouse-motion-adapter create-selection-action-listener]])
  ;(:use [clojure.contrib.math :only [abs]])
  (:import (javax.swing JFrame JPanel JTextArea WindowConstants ButtonGroup JRadioButton SwingUtilities)
           (java.awt.event MouseAdapter MouseMotionAdapter)
           (java.awt Dimension BorderLayout))
  )

(def main-frame (JFrame.))
(def debug-frame (JFrame.))

(def toolbar-panel (JPanel.))
(def draw-panel (proxy (JPanel) [] (paintComponent [g2d] (draw-panel-paint this g2d))))
(reset! paint-panel draw-panel)

(def create-unit-radio-button (JRadioButton.))
(def create-obstacle-radio-button (JRadioButton.))
(def create-selection-button-group (ButtonGroup.))

(def text-area (new JTextArea))

(defn debug [& strs] (.setText text-area (str (.getText text-area) (apply str strs) "\n")))

(defn run
  "Creates mouse listeners, debug frame, draw frame; shows frames; schedules
   move function"
  []
  (SwingUtilities/invokeLater (fn [] (do
    (.addMouseListener draw-panel mouse-adapter)
    (.addMouseMotionListener draw-panel mouse-motion-adapter)

    (doto create-unit-radio-button
      (.setActionCommand "Unit")
      (.setSelected true)
      (.addActionListener create-selection-action-listener))

    (doto create-obstacle-radio-button
      (.setActionCommand "Obstacle")
      (.addActionListener create-selection-action-listener))

    (doto toolbar-panel
      (.add create-obstacle-radio-button)
      (.add create-unit-radio-button))

    (doto create-selection-button-group
      (.add create-unit-radio-button)
      (.add create-obstacle-radio-button))

  (doto draw-panel
    (.setOpaque true)
    (.setLayout (new BorderLayout))
    (.setPreferredSize (Dimension. 500 500)))

    (doto main-frame
      (.add toolbar-panel BorderLayout/NORTH)
      (.add draw-panel BorderLayout/CENTER)
      .pack
      (.setPreferredSize (Dimension. 500 500))
      (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
      (.setVisible true))

    (doto debug-frame
      (.setPreferredSize (Dimension. 500 500))
      (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
      (.setVisible true)))

    ; wrapping draw moves in an anonymous function lets me update draw-moves on the fly since the thread holds a reference
    ; to the anonymous function, not draw-moves
    ; I'm guessing the anonymous function is a closure and calls the real draw-moves even after I re-def it

    ;; !! switch to do-moves as a watcher can do the drawing
    ;(-> (Executors/newScheduledThreadPool 1) (.scheduleWithFixedDelay (fn [] (draw-moves)) 100 100 TimeUnit/MILLISECONDS))
    )))


