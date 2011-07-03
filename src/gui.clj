(ns gui
  (refer paint)
  (import (javax.swing JFrame JPanel WindowConstants))
  (import (java.awt Dimension BorderLayout)))

(def main-frame (JFrame.))
(def debug-frame (JFrame.))

(def draw-panel
  (doto
    (proxy (JPanel) []
      (paintComponent [g2d] (draw-panel-paint this g2d)))
    (.setOpaque true)
    (.setLayout (new BorderLayout))))

(doto main-frame
  (.add draw-panel BorderLayout/CENTER)
  .pack
  (.setPreferredSize (Dimension. 500 500))
  (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE))

(doto debug-frame
  (.setPreferredSize (Dimension. 500 500))
  (.setDefaultCloseOperation WindowConstants/HIDE_ON_CLOSE))


