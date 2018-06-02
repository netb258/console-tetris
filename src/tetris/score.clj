;; Keeps the game's score.
(ns tetris.score)

(defn read-high-score
  "Contract: string -> vector
  Reads the players best score from a file"
  [fname]
  (read-string (slurp fname)))

(defn save-high-score
  "Contract: string int int -> nil
  Saves the players score to a file."
  [fname score lines]
  (spit fname (with-out-str (pr [score lines]))))

(defn overwrite-high-score!
  "Contract: string string int int -> nil"
  [fname score lines]
  (let [high-score (read-high-score fname)]
    (when (> score (first high-score))
      (save-high-score fname score lines))))
