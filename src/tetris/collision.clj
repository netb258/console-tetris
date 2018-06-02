;; As the name suggests, this module handles collision detection.
;; The functions here are pure, they take matrixes along with x,y coordinates and return any detected collisions.
(ns tetris.collision
  (:require [tetris.matrix :as m]
            [tetris.graphics :as g]))

(defn check-bounds
  "Contract: int int vector<vector> vector<vector> -> keyword
  Takes the row and col of where the player is trying to move and checks if they are within the matrix."
  [move-x move-y piece-graphics matrix]
  (let [piece-cleaned (m/clean-piece piece-graphics)
        piece-width (count (last (sort-by count piece-cleaned)))
        piece-height (count piece-cleaned)
        y-limit (- (count g/EMPTY-LINE) piece-width)
        x-limit (- (count matrix) piece-height)]
    (cond
      (< move-y 0) :side-collison
      (> move-y y-limit) :side-collison
      (> move-x x-limit) :bottom-collison
      :else :in-bounds)))

(defn get-collisions
  "Contract: vector<string> vector<string> -> vector<bool>
  Detects collisions on a single piece row."
  [piece-row move-row]
  (map #(and (not= "." %1) (not= "." %2)) piece-row move-row))

(defn count-collisions
  "Contract: vector<bool> -> int"
  [collision-vector]
  (count (filter #(some #{true} %) collision-vector)))

(defn detect-collision
  "Contract: int int vector<vector> vector<vector> -> keyword
  Returns :collision if the active tetris piece will collide with anything in the matrix at the given coordinates."
  [move-row move-col rotation-graphics playfield]
  (let [piece-height (count (m/clean-piece rotation-graphics))
        portrait (m/insert-piece rotation-graphics (m/get-empty-matrix) move-row move-col)
        piece-slice (subvec portrait move-row (+ move-row piece-height))
        move-slice (subvec playfield move-row (+ move-row piece-height))
        collisions (map #(get-collisions %1 %2) piece-slice move-slice)]
    (if
      (> (count-collisions collisions) 0) :collision
      :no-collision)))
