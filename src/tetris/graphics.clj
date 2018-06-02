;; All the glorious graphics in our game, represented as data structures:
;; Needless to say, this module is pure.
(ns tetris.graphics)

(def EMPTY-LINE ["." "." "." "." "." "." "." "." "." "."])

(def PIECES-NORMAL
  {"I" [["." "." "." "."]
        ["c" "c" "c" "c"]
        ["." "." "." "."]
        ["." "." "." "."]]
   "O" [["y" "y"]
        ["y" "y"]]
   "Z" [["r" "r" "."]
        ["." "r" "r"]
        ["." "." "."]]
   "S" [["." "g" "g"]
        ["g" "g" "."]
        ["." "." "."]]
   "J" [["b" "." "."]
        ["b" "b" "b"]
        ["." "." "."]]
   "L" [["." "." "o"]
        ["o" "o" "o"]
        ["." "." "."]]
   "T" [["." "m" "."]
        ["m" "m" "m"]
        ["." "." "."]]})

(def PIECES-ROTATED1
  {"I" [["." "." "c" "."]
        ["." "." "c" "."]
        ["." "." "c" "."]
        ["." "." "c" "."]]
   "O" [["y" "y"]
        ["y" "y"]]
   "Z" [["." "." "r"]
        ["." "r" "r"]
        ["." "r" "."]]
   "S" [["." "g" "."]
        ["." "g" "g"]
        ["." "." "g"]]
   "J" [["." "b" "b"]
        ["." "b" "."]
        ["." "b" "."]]
   "L" [["." "o" "."]
        ["." "o" "."]
        ["." "o" "o"]]
   "T" [["." "m" "."]
        ["." "m" "m"]
        ["." "m" "."]]})

(def PIECES-ROTATED2
  {"I" [["." "." "." "."]
        ["." "." "." "."]
        ["c" "c" "c" "c"]
        ["." "." "." "."]]
   "O" [["y" "y"]
        ["y" "y"]]
   "Z" [["." "." "."]
        ["r" "r" "."]
        ["." "r" "r"]]
   "S" [["." "." "."]
        ["." "g" "g"]
        ["g" "g" "."]]
   "J" [["." "." "."]
        ["b" "b" "b"]
        ["." "." "b"]]
   "L" [["." "." "."]
        ["o" "o" "o"]
        ["o" "." "."]]
   "T" [["." "." "."]
        ["m" "m" "m"]
        ["." "m" "."]]})

(def PIECES-ROTATED3
  {"I" [["." "c" "." "."]
        ["." "c" "." "."]
        ["." "c" "." "."]
        ["." "c" "." "."]]
   "O" [["y" "y"]
         ["y" "y"]]
   "Z" [["." "r" "."]
        ["r" "r" "."]
        ["r" "." "."]]
   "S" [["g" "." "."]
        ["g" "g" "."]
        ["." "g" "."]]
   "J" [["." "b" "."]
        ["." "b" "."]
        ["b" "b" "."]]
   "L" [["o" "o" "."]
        ["." "o" "."]
        ["." "o" "."]]
   "T" [["." "m" "."]
        ["m" "m" "."]
        ["." "m" "."]]})

(defn get-graphics
  "Contract: string keyword -> vector<vector>"
  [piece-id rotation]
  (cond
    (= :CENTER rotation) (get PIECES-NORMAL piece-id)
    (= :ROTATE1 rotation) (get PIECES-ROTATED1 piece-id)
    (= :ROTATE2 rotation) (get PIECES-ROTATED2 piece-id)
    (= :ROTATE3 rotation) (get PIECES-ROTATED3 piece-id)))
