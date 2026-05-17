# Pikafish Android Shell

Minimal Android shell for the Pikafish xiangqi engine.

This project does not build Pikafish locally. The GitHub Actions workflow:

1. Clones `official-pikafish/Pikafish`.
2. Downloads `pikafish.nnue`.
3. Cross-compiles the engine for Android arm64 with NDK.
4. Packages the engine and NNUE file into a debug APK.

The app starts the bundled engine as a UCI process, sends `position ... moves ...`
and `go movetime ...`, then applies the returned `bestmove`.

The first version is intentionally small:

- Human plays red from the initial position.
- The engine replies as black.
- Move input is tap source, tap destination.
- The app does not yet enforce full xiangqi legality in the UI; invalid moves are not a supported workflow.
