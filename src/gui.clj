(ns paint)

(ns gui
  ;(:use [paint :only [paint-panel draw-panel-paint]])
  (:use [paint :only [paint-panel]])
  (:use [event :only [mouse-adapter mouse-motion-adapter create-selection-action-listener]])
  (:use [path :only [path-debug]])
  ;(:use [clojure.contrib.math :only [abs]])
  (:import (javax.swing JFrame JPanel JTextArea WindowConstants ButtonGroup JRadioButton SwingUtilities JTabbedPane JScrollPane)
           (java.awt.event MouseAdapter MouseMotionAdapter)
           (java.awt Dimension BorderLayout))
  )

(def main-frame (JFrame. "Path Finder"))
(def debug-frame (JFrame. "PF - Debug"))

(def toolbar-panel (JPanel.))
;(def draw-panel (proxy (JPanel) [] (paintComponent [g2d] (draw-panel-paint this g2d))))

(def create-unit-radio-button (JRadioButton.))
(def create-obstacle-radio-button (JRadioButton.))
(def create-selection-button-group (ButtonGroup.))

(def debug-paint-text-area (doto (JTextArea.) (.setPreferredSize (Dimension. 500 500))))
;(def debug-tab-panel (JTabbedPane.))

;(defn debug [& strs] (.setText text-area (str (.getText text-area) (apply str strs) "\n")))

(defn append-debug
  "given a list of debug statements, append the statements to the specified text area"
  [text-area _ _ _ new-state]
  (if (seq new-state)
    (dosync
      (.setText text-area (str (.getText text-area) new-state "\n"))
      )))

(add-watch path-debug ::path-debug-watch (partial append-debug debug-paint-text-area))
;(remove-watch path-debug ::path-debug-watch)

(defn run
  "Adds mouse listeners, debug frame, draw frame; shows frames"
  []
  (SwingUtilities/invokeLater
    (fn []
      ;(.addMouseListener draw-panel mouse-adapter)
      ;(.addMouseMotionListener draw-panel mouse-motion-adapter)

      (.addMouseListener paint-panel mouse-adapter)
      (.addMouseMotionListener paint-panel mouse-motion-adapter)

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

      (doto debug-frame
        (.setLayout (new BorderLayout))
        (.add (doto (JTabbedPane.)
                (.setPreferredSize (Dimension. 500 500))
                (.addTab "Paint", (JScrollPane. debug-paint-text-area)))
          BorderLayout/CENTER)
        (.setPreferredSize (Dimension. 500 500))
        (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
        (.setVisible true))

      (doto paint-panel
         (.setOpaque true)
         (.setLayout (new BorderLayout))
         (.setPreferredSize (Dimension. 500 500)))

      (doto main-frame
        (.add toolbar-panel BorderLayout/NORTH)
        (.add paint-panel BorderLayout/CENTER)
        .pack
        (.setPreferredSize (Dimension. 500 500))
        (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
        (.setVisible true))

      (doto debug-frame
        (.setPreferredSize (Dimension. 500 500))
        (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
        (.setVisible true)))))


