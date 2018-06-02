;; You could say that the game of tetris is all about matrix manipulations and that is exactly what this module does.
;; The functions here are pure, mostly they take a matrix and returns a transformed matrix.
(ns tetris.matrix
  (:require [clojure.string :as s]
            [tetris.graphics :as g]))

(defn get-empty-matrix
  "Contract: nil -> vector<vector>"
  []
  (into [] (take 22 (repeat g/EMPTY-LINE))))

(defn clean-rows
  "Contract: vector<vector> -> vector<vector>
  Removes from the piece any rows that contain only empty spaces."
  [piece-graphics]
  (let [top-empty-rows (take-while (fn [row] (every? #(= "." %) row)) piece-graphics)
        full-rows (filter (fn [row] (some #(not= "." %) row)) piece-graphics)]
    (into [] (concat top-empty-rows full-rows))))

(defn flip-row
  "Contract: vector<vector> int -> vector<string>
  Selects a single row from a piece graphics and transforms it from rows to cols representation."
  [piece-graphics col]
  (into [] (map #(nth % col) piece-graphics)))

(defn flip-all-rows
  "Contract: vector<vector> -> vector<vector>"
  [piece-graphics]
  (vec
    (for [i (range (count (first piece-graphics)))
          :let [col (flip-row piece-graphics i)]]
      col)))

;; NOTE: Calling flip-all-rows twice, basically flips the rows/cols representation back into it's original form.
(defn clean-cols
  "Contract: vector<vector> -> vector<vector>
  Removes from the piece any cols that contain only empty spaces."
  [piece-graphics]
  (flip-all-rows
    (filter
      (fn [row] (some #(not= "." %) row))
      (flip-all-rows piece-graphics))))

(defn clean-piece
  "Contract: vector<vector> -> vector<vector>
  Removes any empty rows and cols from a piece."
  [piece-graphics]
  (clean-cols (clean-rows piece-graphics)))

(defn insert-piece-row
  "Contract: vector<string> vector<string> int -> vector<string>"
  [piece-row matrix-row position]
  (let [row-size (count matrix-row)
        end-position (+ position (count piece-row))
        leading-space (take-while #(= "." %) piece-row)
        trailing-space (drop-while #(not= "." %) (drop-while #(= "." %) piece-row))
        before-piece (subvec matrix-row 0 (+ position (count leading-space)))
        piece (filter #(not= "." %) piece-row)
        after-piece (subvec matrix-row (- end-position (count trailing-space)))]
    (into []
          (concat
            before-piece piece after-piece))))

(defn upcase-matrix
  "Contract: vector<vector> -> vector<vector>"
  [matrix]
  (mapv #(mapv s/upper-case %) matrix))

(defn downcase-matrix
  "Contract: vector<vector> -> vector<vector>"
  [matrix]
  (mapv #(mapv s/lower-case %) matrix))

;; Throws an IndexOutOfBoundsException, if the row/col are outside the matrix.
;; NOTE: (map #(map s/upper-case %) piece) - Need the double map, since the piece is represented as a vector of vectors.
(defn insert-piece
  "Contract: vector<vector> vector<vector> int int -> vector<vector>"
  [piece matrix row col]
  (let [piece (clean-piece piece)
        num-piece-rows (count piece)
        rows-before-piece (subvec matrix 0 row)
        rows-for-piece (subvec matrix row (+ row num-piece-rows))
        rows-after-piece (subvec matrix (+ num-piece-rows row))
        piece-upcased (upcase-matrix piece)
        piece-inserted (map #(insert-piece-row %1 %2 col) piece-upcased rows-for-piece)]
    (into [] (concat rows-before-piece piece-inserted rows-after-piece))))

(defn get-filled-lines
  "Contract: vector<vector> -> vector<vector>
  Returns any lines with no empty spaces in them."
  [matrix]
  (filter #(not (some #{"."} %)) matrix))

(defn clear-filled-lines
  "Contract: vector<vector> -> vector<vector>
  Well, if the player has filled any lines, we have to unfill them."
  [matrix]
  (let [num-cleared-lines (count (get-filled-lines matrix))
        matrix-cleared (into [] (remove #(not (some #{"."} %)) matrix))]
    (into []
          (concat
            (take num-cleared-lines (repeat g/EMPTY-LINE))
            matrix-cleared))))
