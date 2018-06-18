;; The main/core module handles the game loop.
(ns tetris.core
  (:require [clojure.string :as s]
            [tetris.matrix :as m]
            [tetris.rotation :as r]
            [tetris.move :as mv]
            [tetris.score :as score]
            [tetris.gui :as gui])
  (:gen-class))

;; -------------------------------------------------------------------------------------------
;; ----------------------------------------- GLOBALS -----------------------------------------
;; -------------------------------------------------------------------------------------------

;; The player's score.
(def SCORE (atom 0))
(def CLEARED-LINES (atom 0))
(def HIGH-SCORE-FILE "./score.dat")

;; Timers.
(def LAST-MOVE-TIME (atom (System/currentTimeMillis))) ;; The exact time of when the game last moved down.

;; Our playfield.
(def MATRIX (atom []))

;; The active tetris piece that the player moves.
;; Possible rotations: CENTER, ROTATE1, ROTATE2, ROTATE3
(def ACTIVE-PIECE (atom {:id "" :rotation :CENTER :row 0 :col 0 :anchored false :graphics []}))

;; The number of pieces that the player has received so far in the game.
(def PIECE-COUNT (atom 0))

;; A lazy seq of all pieces that will flow one after the other during the game.
(def NEXT-PIECE
  (repeatedly
    #(first (shuffle ["I" "O" "Z" "S" "J" "L" "T"]))))

;; -------------------------------------------------------------------------------------------
;; ------------------------------------- HELPER FUNCTIONS ------------------------------------
;; -------------------------------------------------------------------------------------------

(defn clear-playfield!
  "Contract: nil -> nil"
  []
  (swap! MATRIX (fn [m] (m/get-empty-matrix))))

(defn choose-new-piece!
  "Contract: nil -> string"
  []
  (mv/set-active-piece! ACTIVE-PIECE (nth NEXT-PIECE @PIECE-COUNT))
  (swap! PIECE-COUNT #(inc %)))

(defn game-over?
  "Contract: nil -> bool
  Returns true if the player has reached the top level of the matrix, thus losing the game."
  []
  (some #(not= "." %) (first @MATRIX)))

(defn quit-game!
  "Contract: nil -> nil"
  []
  (System/exit 0))

(defn restart-game!
  "Contract: nil -> nil
  Allows the player to start playing the game again after game-over."
  []
  (reset! SCORE 0)
  (reset! CLEARED-LINES 0)
  (clear-playfield!))

;; game-over! needs to call game-loop early, in order to restart the game.
(declare game-loop)

(defn game-over!
  "Contract: nil -> nil
  Shows the game over message and exits when the player presses ESC key."
  []
  (score/overwrite-high-score! HIGH-SCORE-FILE @SCORE @CLEARED-LINES)
  (gui/print-line! "**** GAME OVER ****" 0 false)
  (gui/print-line! (gui/right-pad (str "YOUR SCORE - " @SCORE) 19) 1 false)
  (gui/print-line! (gui/right-pad (str "YOUR LINES - " @CLEARED-LINES) 19) 2 false)
  (gui/print-line! (gui/right-pad (str "HIGH SCORE - " (first (score/read-high-score HIGH-SCORE-FILE))) 19) 3 false)
  (gui/print-line! (gui/right-pad (str "HIGH LINES - " (last (score/read-high-score HIGH-SCORE-FILE))) 19) 4 false)
  (gui/print-line! (gui/right-pad "ENTER: RESTART" 19) 5 false)
  (gui/print-line! (gui/right-pad "ESC: QUIT" 19) 6 false)
  (gui/clear-screen!)
  (let [input-key (gui/get-key-blocking)]
    (cond
      (= :escape input-key) (quit-game!)
      (= :enter input-key) (do (restart-game!) (game-loop))
      :else (recur))))

(defn get-game-speed "Contract: nil -> int" []
  (cond
    (> @CLEARED-LINES 100) 60
    (> @CLEARED-LINES 75) 80
    (> @CLEARED-LINES 60) 120
    (> @CLEARED-LINES 40) 250
    (> @CLEARED-LINES 30) 330
    (> @CLEARED-LINES 20) 400
    (> @CLEARED-LINES 10) 500
    :else 600))

(defn force-down!
  "Contract: nil -> nil
  Force the current active piece to move down on its own."
  []
  (when (>
         (- (System/currentTimeMillis) @LAST-MOVE-TIME)
         (get-game-speed))
    (swap! LAST-MOVE-TIME (fn [x] (System/currentTimeMillis)))
    (mv/move-down! MATRIX ACTIVE-PIECE)))

;; -------------------------------------------------------------------------------------------
;; ---------------------------------------- GAME LOOP ----------------------------------------
;; -------------------------------------------------------------------------------------------

(defn read-input
  "Contract: nil -> nil"
  []
  (let [user-input (gui/get-key)]
    (cond
      (= :left user-input)   (mv/move-left! MATRIX ACTIVE-PIECE)
      (= :right user-input)  (mv/move-right! MATRIX ACTIVE-PIECE)
      (= :down user-input)   (mv/move-down! MATRIX ACTIVE-PIECE)
      (= :up user-input)     (mv/hard-drop! MATRIX ACTIVE-PIECE)
      (= :escape user-input) (quit-game!)
      (= :enter user-input)  (gui/show-pause-screen! @SCORE @CLEARED-LINES)
      (= \p user-input)      (gui/show-pause-screen!)
      (= \z user-input)      (r/rotate-left! MATRIX ACTIVE-PIECE)
      (= \x user-input)      (r/rotate-right! MATRIX ACTIVE-PIECE))))

(defn step!
  "Contract: nil -> nil
  Perform the next step in the game (if the player cleared a line, count the score and stuff)"
  []
  (let [num-cleared-lines (count (m/get-filled-lines @MATRIX))]
    (when (> num-cleared-lines 0)
      (swap! MATRIX #(m/clear-filled-lines %))
      (swap! CLEARED-LINES #(+ num-cleared-lines %))
      (swap! SCORE #(+ (* 100 num-cleared-lines) %)))))

(defn game-loop
  "Contract: nil -> nil"
  []
  (when (or (= "" (:id @ACTIVE-PIECE))
            (= true (:anchored @ACTIVE-PIECE)))
    (choose-new-piece!))
  (step!)
  (gui/clear-screen!)
  (gui/show-next-piece! (nth NEXT-PIECE @PIECE-COUNT))
  (gui/show-playfield! @MATRIX @ACTIVE-PIECE)
  (read-input)
  (force-down!)
  (if (game-over?)
    (game-over!)
    (recur)))

(defn -main []
  (println "Done!") ;; Signal that we have loaded the program.
  (gui/start-gui)
  ;; Center the main window before showing the title screen.
  (gui/center-gui!)
  (gui/set-title! "TETRIS")
  (gui/show-title-screen!)
  (clear-playfield!)
  (game-loop))
