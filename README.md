# Simple MP3 Player (Java + JavaFX)

A minimal desktop MP3 player: import files, see them in a playlist, and play them
with standard transport controls (play/pause/stop/next/prev), a seek bar, and a
volume slider.

## Requirements

- **Java 17+** (JDK, not just JRE)
- **Maven 3.6+**

JavaFX itself is *not* something you need to install separately — it's pulled in
automatically as a Maven dependency (see `pom.xml`), so you don't need to worry
about setting up a JavaFX SDK by hand.

## How to run

From the project root (the folder containing `pom.xml`):

```bash
mvn javafx:run
```

The first run will download the JavaFX dependencies from Maven Central, so it
needs an internet connection the first time. After that, they're cached
locally.

## How to use it

1. Click **Import MP3(s)** and pick one or more `.mp3` files, or just drag and
   drop MP3 files onto the window.
2. Click a track in the playlist to play it (or hit **Play** to start the
   first track).
3. Use **Prev / Play-Pause / Stop / Next** to control playback.
4. Drag the seek bar to jump to a position in the track.
5. Adjust the volume slider on the right.
6. When a track ends, the player automatically advances to the next one
   (looping back to the top of the playlist after the last track).

## Project structure

```
musicplayer/
├── pom.xml                                     # Maven build config (JavaFX deps + run plugin)
├── README.md
└── src/main/java/com/musicplayer/
    └── MusicPlayerApp.java                     # Entire application (UI + playback logic)
```

## Notes / possible extensions

- Only `.mp3` files are accepted right now; JavaFX's `Media`/`MediaPlayer`
  also supports `.wav` and `.aiff` if you want to widen the file filter.
- Playlist order is just import order — you could add drag-to-reorder in the
  `ListView`, or a "shuffle" toggle.
- There's no persistence yet (playlist resets each run) — could be added with
  a simple text file or `java.util.prefs.Preferences` storing file paths.
- If you'd rather not depend on Maven downloading JavaFX at build time, you
  can instead download the JavaFX SDK for your OS from
  https://openjfx.io and point `--module-path` at it manually.
