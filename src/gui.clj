(ns gui
  (refer paint :only '[draw-panel-paint])
  (import (javax.swing JFrame JPanel WindowConstants ButtonGroup JRadioButton))
  (import (java.awt Dimension BorderLayout)))

(def main-frame (JFrame.))
(def debug-frame (JFrame.))

(def toolbar-panel (JPanel.))
(def create-unit-radio-button (JRadioButton.))
(def create-obstacle-radio-button (JRadioButton.))
(def create-selection-button-group (ButtonGroup.))

(.setActionCommand create-unit-radio-button "Unit")
(.setSelected create-unit-radio-button true)
(.setActionCommand create-obstacle-radio-button "Obstacle")
(.add create-selection-button-group create-unit-radio-button)
(.add create-selection-button-group create-obstacle-radio-button)
(.add toolbar-panel create-unit-radio-button)
(.add toolbar-panel create-obstacle-radio-button)

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


