(ns gui
  (refer paint :only '[draw-panel-paint])
  (import (javax.swing JFrame JPanel WindowConstants ButtonGroup JRadioButton))
  (import (java.awt Dimension BorderLayout))
  (import (java.awt.event ActionListener)))

(def left-click-create-type (atom "Unit"))

(def main-frame (JFrame.))
(def debug-frame (JFrame.))

(def toolbar-panel (JPanel.))
(def create-unit-radio-button (JRadioButton.))
(def create-obstacle-radio-button (JRadioButton.))
(def create-selection-button-group (ButtonGroup.))
(def create-selection-action-listener
  (proxy (ActionListener) []
    (actionPerformed [actionEvent]
      (reset! left-click-create-type (.getActionCommand actionEvent)))))

(.setActionCommand create-unit-radio-button "Unit")
(.setSelected create-unit-radio-button true)
(.setActionCommand create-obstacle-radio-button "Obstacle")
(.add create-selection-button-group create-unit-radio-button)
(.add create-selection-button-group create-obstacle-radio-button)
(.add toolbar-panel create-unit-radio-button)
(.add toolbar-panel create-obstacle-radio-button)
(.addActionListener create-unit-radio-button create-selection-action-listener)
(.addActionListener create-obstacle-radio-button create-selection-action-listener)

(def draw-panel
  (doto
    (proxy (JPanel) []
      (paintComponent [g2d] (draw-panel-paint this g2d)))
    (.setOpaque true)
    (.setLayout (new BorderLayout))
    (.setPreferredSize (Dimension. 500 500))))

(doto main-frame
  (.add toolbar-panel BorderLayout/NORTH)
  (.add draw-panel BorderLayout/CENTER)
  .pack
  (.setPreferredSize (Dimension. 500 500))
  (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE)
  (.setVisible true))

(doto debug-frame
  (.setPreferredSize (Dimension. 500 500))
  (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE))


