;; This module controls motion in the game.
;; The very act of moving inplies changing state, because of this the functions here all deal with atoms.
;; The atoms we are dealing with here are the playfield and active-piece.
;; The active-piece looks like this: (atom {:id "" :rotation :CENTER :row 0 :col 0 :anchored false :graphics []})
;; The playfield looks like this:
;; (atom
;;   [["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]
;;    ["." "." "." "." "." "." "." "." "." "."]])

(ns tetris.move
  (:require [tetris.graphics :as g]
            [tetris.matrix :as m]
            [tetris.collision :as c]))

;; All possible tetris pieces and their spawning locations.
(def START-POSITIONS
  {"I" [0 3]
   "O" [0 4]
   "Z" [0 3]
   "S" [0 3]
   "J" [0 3]
   "L" [0 3]
   "T" [0 3]})

(defn set-active-piece!
  ([active-piece id] (set-active-piece! active-piece id :CENTER (START-POSITIONS id)))
  ([active-piece id [row col]] (set-active-piece! active-piece id (:rotation @active-piece) [row col]))
  ([active-piece id rotation [row col]] ;; Contract: atom string keyword [int int] -> nil
   (swap!
     active-piece
     (fn [p] {:id id
              :rotation rotation
              :row row
              :col col
              :anchored false
              :graphics (g/get-graphics id rotation)}))))

(defn update-playfield!
  "Contract: atom atom -> nil
  Note: Both arguments are atoms that will be swapped."
  [playfield active-piece]
  (swap! playfield
         #(m/downcase-matrix
            (m/insert-piece (:graphics @active-piece) % (:row @active-piece) (:col @active-piece))))
  (swap! active-piece #(assoc % :anchored true)))

(defn move-active-piece!
  "Contract: atom atom int int -> nil or error keyword"
  [playfield active-piece & {:keys [x y]
                             :or {x (:row @active-piece)
                                  y (:col @active-piece)}}]
  (if (= :in-bounds (c/check-bounds x y (:graphics @active-piece) @playfield))
    (set-active-piece! active-piece (:id @active-piece) [x y])
    :out-of-bounds))

(defn move-left!
  "Contract: atom atom -> nil or error keyword
  Allows the player to move the current active piece to the left."
  [playfield active-piece]
  (let [new-x (:row @active-piece)
        new-y (dec (:col @active-piece))]
    (cond
      (not= :in-bounds (c/check-bounds new-x new-y (:graphics @active-piece) @playfield)) :out-of-bouns
      (= :collision (c/detect-collision new-x new-y (:graphics @active-piece) @playfield)) :cant-move-there
      :else (move-active-piece! playfield active-piece :x new-x :y new-y))))

(defn move-right!
  "Contract: atom atom -> nil or error keyword
  Allows the player to move the current active piece to the right."
  [playfield active-piece]
  (let [new-x (:row @active-piece)
        new-y (inc (:col @active-piece))]
    (cond
      (not= :in-bounds (c/check-bounds new-x new-y (:graphics @active-piece) @playfield)) :out-of-bouns
      (= :collision (c/detect-collision new-x new-y (:graphics @active-piece) @playfield)) :cant-move-there
      :else (move-active-piece! playfield active-piece :x new-x :y new-y))))

(defn move-down!
  "Contract: atom atom -> nil or error keyword
  Moves to move the current active tetris piece one step down."
  [playfield active-piece]
  (let [new-x (inc (:row @active-piece))
        new-y (:col @active-piece)]
    (cond
      (not= :in-bounds (c/check-bounds new-x new-y (:graphics @active-piece) @playfield)) (update-playfield! playfield active-piece)
      (= :collision (c/detect-collision new-x new-y (:graphics @active-piece) @playfield)) (update-playfield! playfield active-piece)
      :else (move-active-piece! playfield active-piece :x new-x :y new-y))))

(defn hard-drop!
  "Contract: atom atom -> nil or error keyword
  Drop the player to the bottom of the matrix instantly."
  [playfield active-piece]
  (dotimes [i (count @playfield)]
    (move-down! playfield active-piece)))
