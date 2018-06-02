;; In the game of tetris, the player needs to be able to rotate the currently active tetris piece.
;; Unfortunately, this implies change, so the functions here all deal with atoms.
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

(ns tetris.rotation
  (:require [tetris.collision :as c]
            [tetris.move :as mv]
            [tetris.graphics :as g]))

(defn rotate-piece!
  "Contract: atom atom keyword -> nil
  NOTE: There is only one situation when the rotation would put us out of bounds:
  When we have moved too far to the right and we are trying to rotate out of the matrix (can't happen for left).
  We correct this situation by moving ot the left, until (not= :in-bounds) becomes false (or we :cant-move-there)."
  [playfield active-piece rotation]
  (let [current-id (:id @active-piece)
        current-row (:row @active-piece)
        current-col (:col @active-piece)
        new-rotation (g/get-graphics current-id rotation)]
    (cond
      (not= :in-bounds (c/check-bounds current-row current-col new-rotation @playfield))
      (when (not= :cant-move-there (mv/move-left! playfield active-piece)) (recur playfield active-piece rotation))
      (= :no-collision (c/detect-collision current-row current-col new-rotation @playfield))
      (swap!
        active-piece
        (fn [p]
          {:id current-id
           :rotation rotation
           :row current-row
           :col current-col
           :anchored false
           :graphics new-rotation}))
      :else :cant-rotate)))

(defn rotate-left!
  "Contract: atom atom -> nil"
  [playfield active-piece]
  (let [current-rotation (:rotation @active-piece)]
   (cond
     (= :CENTER current-rotation) (rotate-piece! playfield active-piece :ROTATE3)
     (= :ROTATE3 current-rotation) (rotate-piece! playfield active-piece :ROTATE2)
     (= :ROTATE2 current-rotation) (rotate-piece! playfield active-piece :ROTATE1)
     :else (rotate-piece! playfield active-piece :CENTER))))

(defn rotate-right!
  "Contract: atom atom -> nil"
  [playfield active-piece]
  (let [current-rotation (:rotation @active-piece)]
    (cond
      (= :CENTER current-rotation) (rotate-piece! playfield active-piece :ROTATE1)
      (= :ROTATE1 current-rotation) (rotate-piece! playfield active-piece :ROTATE2)
      (= :ROTATE2 current-rotation) (rotate-piece! playfield active-piece :ROTATE3)
      :else (rotate-piece! playfield active-piece :CENTER))))
