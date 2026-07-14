package com.musicplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class mp3player extends Application {

    private MediaPlayer mediaPlayer;
    private final ObservableList<Track> playlist = FXCollections.observableArrayList();
    private final ListView<Track> playlistView = new ListView<>(playlist);

    private final Slider seekSlider = new Slider(0, 100, 0);
    private final Slider volumeSlider = new Slider(0, 100, 70);
    private final Label timeLabel = new Label("00:00 / 00:00");
    private final Label nowPlayingLabel = new Label("No track loaded");
    private final Button playPauseButton = new Button("▶ Play");

    private boolean seeking = false; // prevents feedback loop
    private int currentIndex = -1;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ----- now playing + import button -----
        Button importButton = new Button("Import MP3(s)");
        importButton.setOnAction(e -> importFiles(stage));

        HBox topBar = new HBox(10, importButton, nowPlayingLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(topBar);

        // ----- Center -----
        playlistView.setPlaceholder(new Label("Import MP3 files or drag & drop them here"));
        playlistView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx != currentIndex) {
                playTrackAt(idx);
            }
        });
        root.setCenter(playlistView);
        root.setBottom(buildControls());
        root.setOnDragOver(this::handleDragOver);
        root.setOnDragDropped(this::handleDragDropped);

        Scene scene = new Scene(root, 560, 480);
        stage.setTitle("MP3 Player");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
        });
    }

    private VBox buildControls() {
        Button prevButton = new Button("⏮ Prev");
        Button stopButton = new Button("⏹ Stop");
        Button nextButton = new Button("⏭ Next");

        prevButton.setOnAction(e -> playPrevious());
        nextButton.setOnAction(e -> playNext());
        stopButton.setOnAction(e -> stopPlayback());
        playPauseButton.setOnAction(e -> togglePlayPause());

        HBox transportBar = new HBox(10, prevButton, playPauseButton, stopButton, nextButton);
        transportBar.setAlignment(Pos.CENTER);

        // Seek slider
        seekSlider.setMin(0);
        seekSlider.setMax(100);
        seekSlider.setValue(0);
        HBox.setHgrow(seekSlider, Priority.ALWAYS);

        seekSlider.setOnMousePressed(e -> seeking = true);
        seekSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null && !total.isUnknown()) {
                    mediaPlayer.seek(total.multiply(seekSlider.getValue() / 100.0));
                }
            }
            seeking = false;
        });

        HBox seekBar = new HBox(10, seekSlider, timeLabel);
        seekBar.setAlignment(Pos.CENTER);

        // Volume slider
        volumeSlider.setPrefWidth(120);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });

        HBox volumeBar = new HBox(8, new Label("Volume"), volumeSlider);
        volumeBar.setAlignment(Pos.CENTER_RIGHT);

        HBox bottomRow = new HBox(20, seekBar, volumeBar);
        HBox.setHgrow(seekBar, Priority.ALWAYS);
        bottomRow.setAlignment(Pos.CENTER);

        VBox controls = new VBox(10, transportBar, bottomRow);
        controls.setPadding(new Insets(10, 0, 0, 0));
        return controls;
    }

    // ---------- import ----------

    private void importFiles(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select MP3 file(s)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("MP3 Audio", "*.mp3"));
        List<File> files = chooser.showOpenMultipleDialog(stage);
        addFiles(files);
    }

    private void addFiles(List<File> files) {
        if (files == null || files.isEmpty()) return;
        boolean wasEmpty = playlist.isEmpty();
        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(".mp3")) {
                playlist.add(new Track(f));
            }
        }
        if (wasEmpty && !playlist.isEmpty()) {
            playlistView.getSelectionModel().select(0);
        }
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        var db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            addFiles(db.getFiles());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    // ---------- Playback ----------

    private void playTrackAt(int index) {
        if (index < 0 || index >= playlist.size()) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        currentIndex = index;
        Track track = playlist.get(index);
        Media media = new Media(track.getFile().toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> updateProgress());
        mediaPlayer.setOnReady(() -> updateProgress());
        mediaPlayer.setOnEndOfMedia(this::playNext);
        mediaPlayer.setOnError(() -> {
            nowPlayingLabel.setText("Error playing: " + track.getFile().getName());
        });

        mediaPlayer.play();
        playPauseButton.setText("⏸ Pause");
        nowPlayingLabel.setText("Now playing: " + track.getFile().getName());
        playlistView.getSelectionModel().select(index);
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) {
            if (!playlist.isEmpty()) {
                playTrackAt(0);
            }
            return;
        }
        if (mediaPlayer.getStatus() == Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("▶ Play");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸ Pause");
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playPauseButton.setText("▶ Play");
        }
    }

    private void playNext() {
        if (playlist.isEmpty()) return;
        int next = currentIndex + 1;
        if (next >= playlist.size()) {
            next = 0; // loop back to start
        }
        playTrackAt(next);
    }

    private void playPrevious() {
        if (playlist.isEmpty()) return;
        int prev = currentIndex - 1;
        if (prev < 0) {
            prev = playlist.size() - 1;
        }
        playTrackAt(prev);
    }

    private void updateProgress() {
        if (mediaPlayer == null || seeking) return;
        Duration current = mediaPlayer.getCurrentTime();
        Duration total = mediaPlayer.getTotalDuration();

        if (total != null && !total.isUnknown() && total.toMillis() > 0) {
            double pct = (current.toMillis() / total.toMillis()) * 100.0;
            Platform.runLater(() -> {
                if (!seeking) {
                    seekSlider.setValue(pct);
                }
                timeLabel.setText(formatDuration(current) + " / " + formatDuration(total));
            });
        }
    }

    private static String formatDuration(Duration d) {
        int totalSeconds = (int) d.toSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static class Track {
        private final File file;

        Track(File file) {
            this.file = file;
        }

        File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
