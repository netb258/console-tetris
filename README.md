# console-tetris
A command line implementation of Tetris, written in Clojure.

Uses clojure-lanterna for rendering.

![Alt text](./screenshot.png?raw=true "Title")

# Usage


    $ lein run
    
# Running in system shell

By default the program is set to run in a swing based console (for portability).

If you want to run it in the OS shell, then you will need to change src/tetris/gui.clj slightly.

Notice that line 12 of gui.clj sets the WINDOW var with the :swing keyword.

Just change that keyword to :unix if you are on a Unix OS.

Under MS Windows only :cygwin is supported, so you will need to install Cygwin.

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
