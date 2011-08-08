(ns paint)

(ns gui
  (:use [paint :only [paint-panel paint-debug]])
  (:use [event :only [mouse-adapter mouse-motion-adapter create-selection-action-listener]])
  (:use [path :only [path-debug]])
  (:use [unit :only [unit-debug]])
  (:import (javax.swing JFrame JPanel JTextArea WindowConstants ButtonGroup JRadioButton SwingUtilities JTabbedPane JScrollPane)
           (java.awt.event MouseAdapter MouseMotionAdapter)
           (java.awt Dimension BorderLayout))
  )

(def main-frame (JFrame. "Path Finder"))
(def debug-frame (JFrame. "PF - Debug"))

(def toolbar-panel (JPanel.))

(def create-unit-radio-button (JRadioButton.))
(def create-obstacle-radio-button (JRadioButton.))
(def create-selection-button-group (ButtonGroup.))

(def path-debug-text-area (JTextArea.))
(def unit-debug-text-area (JTextArea.))
(def paint-debug-text-area (JTextArea.))

(defn append-debug
  "given a list of debug statements, append the statements to the specified text area"
  [text-area _ _ _ new-state]
  (if (seq new-state)
    (dosync
      (SwingUtilities/invokeLater (fn [] (.setText text-area (str (.getText text-area) new-state "\n"))))
      )))

(add-watch path-debug ::path-debug-watch (partial append-debug path-debug-text-area ))
(add-watch unit-debug ::unit-debug-watch (partial append-debug unit-debug-text-area ))
(add-watch paint-debug ::paint-debug-watch (partial append-debug paint-debug-text-area ))
;(remove-watch path-debug ::path-debug-watch)

(defn run
  "Adds mouse listeners, debug frame, draw frame; shows frames"
  []
  (SwingUtilities/invokeLater
    (fn []
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
                (.addTab "Paint", (doto (JScrollPane. paint-debug-text-area) (.setPreferredSize (Dimension. 500 500))))
                (.addTab "Path", (doto (JScrollPane. path-debug-text-area) (.setPreferredSize (Dimension. 500 500))))
                (.addTab "Unit", (doto (JScrollPane. unit-debug-text-area) (.setPreferredSize (Dimension. 500 500)))))
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
        .pack
        (.setVisible true)))))


