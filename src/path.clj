(ns path
  (:use [clojure.contrib.generic.math-functions :only [abs]]))

(def ants (ref {})) ; key is [ant #] value is [moves] where each move is of the form {:x _ :y _}
(def pheromones (ref {}))
(def num-ants 5)
(def path-debug (ref nil))


(def directions [{:xd -1 :yd -1}  ; NW
                 {:xd 0  :yd -1}  ; N
                 {:xd 1  :yd -1}  ; NE
                 {:xd -1 :yd 0}   ; W
                 {:xd 1  :yd 0}   ; E
                 {:xd -1 :yd 1}   ; SW
                 {:xd 0  :yd 1}   ; S
                 {:xd 1  :yd 1}]) ; SE

(defn debug
  "Given a list, apply str to the list and append to path-debug"
  [& strs]
  (dosync
    (ref-set path-debug (apply str (interpose " " strs)))))

(defn distance
  "calculate travel distance if only the eight cardinal directions are allowed"
  [src dest]
  ;the shorter of the x or y distance is cancelled out by travelling diagonally
  (let [sx (:x src) sy (:y src) dx (:x dest) dy (:y dest)]
    (max (abs (- sx dx)) (abs (- sy dy)))))

(defn move-coord
  "Given an xy co-ord and an xy direction return the modified co-ords, e.g.
  (move-coord {:x 5 :y 5} {:xd -1 :yd 1}) -> {:x 4 :y 6}"
  [{:keys [x y]} {:keys [xd yd]}]
  {:x (+ x xd) :y (+ y yd)})

(defn mult-by-grid-size
  "Given a 'direction' co-ordinate (e.g. -1,1) multiple
  by the grid-size to get an actual co-ordinate"
  [grid-size {:keys [xd yd]}]
  {:xd (* xd grid-size) :yd (* yd grid-size)})

(defn rand-dir
  "Given a function to determine if a point is occupied, a size of grid square
  (to translate directions to coordinates) and a src, pick a random direction
  and return the coordinates of the next point from the src in that direction"
  ([occupied? grid-size src] (rand-dir occupied? grid-size src []))

  ([occupied? grid-size src prev]
    (if (>= 8 (count prev))
      nil)
    (let [coord (move-coord src (mult-by-grid-size grid-size (directions (int (* 7.999 (rand))))))]
      ;(debug "coord:" coord "prev:" prev)
      (cond
        (seq (filter (fn [x] (= x coord)) prev))
          (do
            ;(debug "already have " coord)
            (recur occupied? grid-size src prev))

        (or (neg? (:x coord)) (neg? (:y coord)))
          (do
            ;(debug "invalid coord; negative: " coord)
            (recur occupied? grid-size src (conj prev coord)))

        (occupied? coord)
          (do
            ;(debug "dir was occupied: " coord)
            (recur occupied? grid-size src (conj prev coord)))

        :else coord))))

; (rand-dir (fn [x] false) 5 {:x 5 :y 5})
; (rand-dir (fn [{:keys [x y]}] (odd? x)) 5 {:x 5 :y 5})
; (rand-dir (fn [x] false) 5 {:x 0 :y 0})

(defn create-ants
  "create some ants to go exploring for us"
  [{:keys [shape] :as unit} num-ants occupied?]
  (let [src {:x (.getX shape) :y (.getY shape)}]
    (zipmap (take 5 (iterate (fn [[unit ant-id]] [unit (inc ant-id)]) [unit 1])) (take 5 (repeatedly (rand-dir occupied? src))))))

(defn trunc-conj
  "Given a list of points and a point, return the list of points up to and including
  the first instance of point or the entire list with point on the end if the list
  does not include point"
  ([path point]
  (if (seq (filter #(= % point) path))
    (conj (vec (take-while #(not (= % point)) path)) point)
    (conj path point))))
; (trunc-conj [{:x 0 :y 0}] {:x 1 :y 1}) => [{:x 0, :y 0} {:x 1, :y 1}]
; (trunc-conj [{:x 1 :y 1} {:x 0 :y 0}] {:x 1 :y 1}) => ({:x 1, :y 1})
; (trunc-conj [{:x 0 :y 0} {:x 1 :y 1}] {:x 1 :y 1}) => [{:x 0, :y 0} {:x 1, :y 1}]

(defn update-phero
  "Given a unit and a point, update the pheromone count for
  that position"
  [unit p]
  (let [old-phero (get @pheromones [unit p])
        new-phero (if (nil? old-phero) 1 (inc old-phero))]
    (dosync
      (alter pheromones assoc-in [unit p] new-phero))))

(defn next-move
  "Figure out the next move given a source, target, a function to
  tell if a tile is occupied and a function to read
  pheromone levels"
  [src dest occupied? pheromones]
  ;for now I'm used rand-dir to pick a direction, but I'll need this eventually
  nil)

; an ant is a function that recursively does three things:
; 1) checks if it's alive
; 2) trys to get a move
; 3) moves
;
; returns a path
(defn explore
  "Given an path history, a function to get the next move, a target
  the current number of steps taken and the max allowable steps,
  return a non-cyclical path built with a direction function and a
  target.
  We can't use (count path) for number of steps because loops are
  eliminated (once we loop back to a point we erase the loop from
  memory"
  [path next-move-fn target steps max-steps unit]
  (cond
    (= steps max-steps) (do (debug "max steps") path)
    (= target (last path)) path
    :default (if-let [next (next-move-fn (last path))]
      (do
        (update-phero unit next)
        (debug "explore: " (inc steps) max-steps " target " target " path " path " next: " next " new path: " (trunc-conj path next))
        ;path)
        (recur (trunc-conj path next) next-move-fn target (inc steps) max-steps unit))
      path)))

;(explore [{:x 0 :y 0}] (fn [& _] {:x 0 :y 0}) {:x 1 :y 1} 5 5 :unit)
;(explore [{:x 0 :y 0}] (partial rand-dir (fn [x] false) 5) {:x 5 :y 5} 0 5 :unit)

(defn shortest-path
  "Given a list of paths and a target, return the shortest
  path that ends at the target"
  [paths target]
  (let [complete (filter #(= target (last %)) paths)]
    (if (seq complete)
      (first (sort #(compare (count %1) (count %2)) complete))
      nil)))

(defn trunc-at-closest-point
  "Given a path, return a map containing the path and the closest distace
  that that path comes to the target (in steps)"
  ([path target]
    (let [path (reverse path)
          point-dists (zipmap (map distance path (repeat target)) path)
          shortest-dist (first (sort (keys point-dists)))]
      {:path (trunc-conj path (get point-dists (first (sort (keys point-dists)))))
       :distance shortest-dist})))

(defn closest-path
  "Given a list of paths, return a list of maps, each
  containing an original list, the closest point to the
  target in the list and the distance between the closest
  point and the target in steps."
  [paths target]
  (:path (first (sort #(compare (:distance %1) (:distance %2)) (map trunc-at-closest-point paths (repeat target))))))

(defn best-path
  "Given a unit, a target and a function to tell if a tile is occupied, get
  the best path to target using several competing 'ants'"
  [unit src target occupied? square-size]

  (debug "best path - target: " target)

  (dosync (alter dissoc unit))

  (let [fns (repeat num-ants (fn [] (explore [src] (partial rand-dir occupied? square-size) target 0 5 unit)))
        paths (apply pcalls fns)
        shortest (shortest-path paths target)]
    (if (nil? shortest) (closest-path paths) shortest)))