# Simple MP3 Player (Java + JavaFX)

A minimal desktop MP3 player: import files, see them in a playlist, and play them
with standard transport controls (play/pause/stop/next/prev), a seek bar, and a
volume slider.

## Requirements

- **Java 17+** (JDK, not just JRE)
- **Maven 3.6+**

JavaFX itself is *not* something you need to install separately btw

## How to run

From the project root (the folder containing `pom.xml`):

```bash
mvn javafx:run
```

The first run will download the JavaFX dependencies from Maven Central, so it
needs an internet connection the first time. After that, they're cached
locally.

## How to use it

1. import by clicking the "Import MP3s" and pick one or more `.mp3` files, or just drag and
   drop MP3 files onto the window.
2. click a track in the playlist to play it (or hit **Play** to start the
   first track).
3. use **Prev / Play-Pause / Stop / Next** to control playback.
4. drag the seek bar to jump to a position in the track.
5. adjust the volume slider to maximize or minimize volume.
6. the player automatically advances to the next one after the current track ends.
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

- Only `.mp3` files are used right now.
- Playlist order is just import order — you could add drag-to-reorder in the
  `ListView`, or a "shuffle" toggle.
- There's no persistence yet (playlist resets each run)
