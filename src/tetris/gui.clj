;; This module renders the game. Our GUI is a command line interface with colors.
(ns tetris.gui
  (:require [lanterna.terminal :as t]
            [lanterna.screen :as console]
            [tetris.graphics :as g]
            [tetris.move :as mv]
            [tetris.collision :as c]
            [tetris.matrix :as m])
  (:import com.googlecode.lanterna.screen.Screen))

;; The window that will hold our game.
(def WINDOW (t/get-terminal :swing {:rows 26 :cols 19 :font-size 20}))
(def DISPLAY (new Screen WINDOW))
(def REDRAW-PAUSE 20)

(defn get-color
  [ch]
  (cond
    (or (= \b ch) (= \B ch)) {:fg :blue}
    (or (= \r ch) (= \R ch)) {:fg :red}
    (or (= \y ch) (= \Y ch)) {:fg :yellow :styles #{:bold}}
    (or (= \g ch) (= \G ch)) {:fg :green}
    (or (= \m ch) (= \M ch)) {:fg :magenta}
    (or (= \c ch) (= \C ch)) {:fg :cyan}
    (or (= \o ch) (= \O ch)) {:fg :yellow}
    (= \= ch) {:fg :white :styles #{:underline}}
    :else {:fg :default :bg :default}))

(defn right-pad
  "Right pad string with spaces, making it at least len long."
  [mystr len]
  (format (str "%-" len "s") mystr))

(defn print-line!
  "Contract: string int bool -> nil
  A custom printing function for our swing console.
  NOTE: Obliously it returns something, since it's a call to map,
  but the result is useless so I'm contracting it as -> nil."
  [text lnum use-color]
  (doall
    (map-indexed
      (fn [idx ch]
        (console/put-string DISPLAY idx lnum (str ch) (when use-color (get-color ch))))
      text)))

(defn clear-screen!
  "Contract: nil -> nil
  Clear the console window."
  []
  (console/redraw DISPLAY)
  (Thread/sleep REDRAW-PAUSE)) ;; We need a slight delay when redrawing or it will consume too much CPU.

(defn print-matrix!
  "Contract: vector<vector> int -> nil"
  [matrix offset]
  (flush)
  (if (empty? matrix) (recur (m/get-empty-matrix) offset)
    (let [lines (map #(clojure.string/join " " %) matrix)]
      (doseq [[line i] (map list lines (range (count lines)))]
        (print-line! line (+ i offset) true)))))

(defn show-next-piece!
  "Contract: string -> nil
  Displays the next tetris piece that the player will receive."
  [next-piece-id]
  (print-line! "^^^ NEXT1 PIECE ^^^" 3 false)
  (let [next-piece-graphics (g/get-graphics next-piece-id :CENTER)
        start-position (mv/START-POSITIONS next-piece-id)
        padding (into [] (take 3 (repeat g/EMPTY-LINE)))
        x (first start-position)
        y (last start-position)
        offset 0]
    (print-matrix!
      (m/insert-piece
        next-piece-graphics padding x y)
      offset)))

(defn get-lowest-row
  "Contract: vector<vector> vector<vector> int int -> int
  Returns the lowest row that a piece can drop in the matrix."
  [matrix piece-graphics current-row current-col]
  (let [new-x (inc current-row)
        new-y current-col]
    (cond
      (not= :in-bounds (c/check-bounds new-x new-y piece-graphics matrix)) current-row
      (= :collision (c/detect-collision new-x new-y piece-graphics matrix)) current-row
      :else (recur matrix piece-graphics new-x new-y))))

(defn show-playfield!
  "Contract: vector<vector> vector<vector> -> nil
  Renders the playfield along with the current tetris piece and it's shadow.
  The shadow is the little preview at the bottom, that tells the player where the current tetris piece is going to land."
  [playfield active-piece]
  (let [shadow-graphics (map (fn [row] (map #(if (not= "." %) "=" %) row)) (:graphics active-piece))
        shadow-col (:col active-piece)
        shadow-row (get-lowest-row playfield shadow-graphics (:row active-piece) shadow-col)
        playfield-with-shadow (m/insert-piece shadow-graphics playfield shadow-row shadow-col)
        start-row 4]
    (print-matrix!
      (m/insert-piece
        (:graphics active-piece) playfield-with-shadow (:row active-piece) (:col active-piece))
      start-row)))

(defn get-key
  "Contract: nil -> char
  Does not block when listening for a keypress."
  []
  (console/get-key DISPLAY))

(defn get-key-blocking
  "Contract: nil -> char
  Blocks when listening for a keypress."
  []
  (console/get-key-blocking DISPLAY))

(defn start-gui
  "Contract: nil -> nil"
  []
  (console/start DISPLAY))

(defn center-gui!
  "Contract: nil -> nil"
  []
  (-> WINDOW (.getJFrame) (.setLocationRelativeTo nil)))

(defn set-title!
  "Contract: string -> nil"
  [title]
  (-> WINDOW (.getJFrame) (.setTitle title)))

(defn show-title-screen!
  "Contract: nil -> char"
  []
  (print-line! "***** TETRIS *****" 0 false)
  (print-line! "PRESS ANY KEY: PLAY" 1 false)
  (print-line! "PRESS ESC: QUIT" 2 false)
  (print-line! "AROW KEYS: MOVE" 3 false)
  (print-line! "PRESS Z: ROTATE L" 4 false)
  (print-line! "PRESS X: ROTATE R" 5 false)
  (print-line! "PRESS P: PAUSE" 6 false)
  (print-line! "PRESS ENTER: PAUSE" 7 false)
  (clear-screen!)
  (get-key-blocking))

(defn show-pause-screen!
  "Contract int int -> nil"
  [score cleared-lines]
  (print-line! "*** GAME PAUSED ***" 0 false)
  (print-line! "*ANY KEY: CONTINUE*" 1 false)
  (print-line! (right-pad (str "*SCORE: " score) 19) 2 false)
  (print-line! (right-pad (str "*LINES: " cleared-lines) 19) 3 false)
  (clear-screen!)
  (get-key-blocking))
