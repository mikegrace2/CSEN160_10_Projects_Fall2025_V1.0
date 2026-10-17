package _5090;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.media.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

public class AMain extends Application {

    // notes arraylist for easy management, declared at top as class field so that all scopes have access
    private List<Note> notes = new ArrayList<>();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int LANE_COUNT = 4;

    // Need a data container for containing each note's accuracy
    static class Note {
        double hitTime;
        int lane;
        boolean hit = false;
        boolean correctHit = false; // true if hit correctly

        public Note(double hitTime, int lane) {
            this.hitTime = hitTime;
            this.lane = lane;
        }
    }

    // JavaFX usual setup
    @Override
    public void start(Stage stage) {

        Group root = new Group();
        Scene scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK); // temp color, will use a bg

        Image bgImage = new Image(getClass().getResource("/backg.jpg").toExternalForm());
        Rectangle bgRect = new Rectangle(WIDTH*2, HEIGHT*2); // big enough to cover the screen when extended
        bgRect.setFill(new ImagePattern(bgImage));
        bgRect.setTranslateZ(300);
        bgRect.setTranslateX(-400);
        root.getChildren().add(0, bgRect); // add at index 0 so it's behind everything

        // intro text and animations, uses the custom TitleText class
        TitleText title = new TitleText("Catch Stars Using ASDF", WIDTH, HEIGHT);
        root.getChildren().add(title.get());
        FadeTransition fadeIn = title.fadeIn(3);
        FadeTransition stay = title.stay(3);
        FadeTransition fadeOut = title.fadeOut(3);

        // same as above
        TitleText begin = new TitleText("Begin...", WIDTH, HEIGHT);
        root.getChildren().add(begin.get());
        FadeTransition fadeIn1 = begin.fadeIn(1);
        FadeTransition stay1 = begin.stay(1);
        FadeTransition fadeOut1 = begin.fadeOut(2);

        // link them together
        fadeIn.setOnFinished(e -> stay.play());
        stay.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> fadeIn1.play());
        fadeIn1.setOnFinished(e -> stay1.play());
        stay1.setOnFinished(e -> fadeOut1.play());
        fadeIn.play(); // start from beginning.

        // perspective camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateX(400);
        camera.setTranslateY(-100);
        camera.setTranslateZ(-1000);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-25);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        scene.setCamera(camera);

        // lighting needed to see the boxes
        PointLight light = new PointLight(Color.rgb(255, 255, 100, 0.5));
        light.setTranslateY(-200);
        light.setTranslateZ(-400);
        root.getChildren().add(light);


        AmbientLight ambient = new AmbientLight(Color.rgb(49, 49, 69));
        root.getChildren().add(ambient);

        // four lanes, well, boxes
        Box[] lanes = new Box[LANE_COUNT];
        for (int i = 0; i < LANE_COUNT; i++) {
            Box lane = new Box(100, 100, 100);
            lane.setTranslateX(i * 200 + 100);
            lane.setTranslateY(500);
            lane.setMaterial(new PhongMaterial(Color.rgb(50, 50, 220)));
            root.getChildren().add(lane);
            lanes[i] = lane;
        }

        // ground
        Box ground = new Box(1000, 500, 300);
        ground.setTranslateX(WIDTH / 2);
        ground.setTranslateY(550+500/2);
        ground.setMaterial(new PhongMaterial(Color.rgb(60, 60, 220)));
        root.getChildren().add(ground);


        // music
        Media media = new Media(getClass().getResource("/twinkle_short.wav").toExternalForm());
        MediaPlayer player = new MediaPlayer(media);

        // after media player is playing, then we start the notes
        player.setOnPlaying(() -> {


            // hardcoded, twinkle song has 72 bpm according to sheet, first note starts at 14.10s in this piece.
            double bpm = 72.0;
            double beatLength = 60.0 / bpm;
            double firstNote = 14.10;

            // 6 quarter notes and 1 half note repeated 6 times, very handy
            int[] notePattern = {1, 1, 1, 1, 1, 1, 2};

            Random random = new Random();

            double currentTime = firstNote;
            // load the obj file for the star, each star will be each note
            File starFile = null;
            try {
                starFile = new File(getClass().getResource("/star.obj").toURI());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 42; i++) {
                int lane = random.nextInt(LANE_COUNT); // randomly chooses a lane
                double hitTime = currentTime;
                notes.add(new Note(hitTime, lane)); // use the static class above to hold info about each note

                new Star(root, starFile, lane, hitTime); // star class
                currentTime += notePattern[i % notePattern.length] * beatLength; // the note lengths are repeated, so use %
            }

            // after all 42 notes, show accuracy
            // we need the accuracy inside this scope because this part of the code runs the last
            // it also needs the entire notes data
            double lastNoteTime = notes.get(41).hitTime; // index 41 is the final note, if more notes are in the list, they are all the extra ones
            double showAccuracyTime = lastNoteTime + 2.0; // used for a 2s pause below


            PauseTransition accuracyDelay = new PauseTransition(Duration.seconds(showAccuracyTime));
            accuracyDelay.setOnFinished(ev -> {
                // after pause, loop through the notes list and sum the correct hits
                int correctHits = 0;
                int totalAttempts = notes.size(); // include extra attempts
                for (Note n : notes) {
                    if (n.correctHit) correctHits++;
                }

                double percent = (correctHits * 100.0) / totalAttempts;
                String accText = String.format("Accuracy: %.1f%%", percent);

                // use the TitleText custom class
                TitleText acc = new TitleText(accText, WIDTH, HEIGHT);
                root.getChildren().add(acc.get());
                acc.fadeIn(2).play();
            });
            accuracyDelay.play();
        });
        player.play();

        double hitWindow = 0.5; // +-0.5 sec window to hit the correct block, change for diff difficulties

        // hit detection event handling for each key press
        scene.setOnKeyPressed(e -> {
            int laneHit = switch (e.getCode()) {
                case A -> 0;
                case S -> 1;
                case D -> 2;
                case F -> 3;
                default -> -1;
            };
            if (laneHit == -1) return;

            double time = player.getCurrentTime().toSeconds();
            boolean hitCorrectNote = false; // needed to identify extra hits

            // check for a valid note within +-0.5s that hasn't been hit
            for (Note note : notes) {
                if (!note.hit && note.lane == laneHit && Math.abs(time - note.hitTime) <= hitWindow) { // if the note isn't hit yet and the lane is correct and within the hitwindow, correct hit
                    // change the note data
                    note.hit = true;
                    note.correctHit = true;
                    hitCorrectNote = true;
                    break;
                }
            }

            // if no valid note found, this counts as an incorrect hit
            if (!hitCorrectNote) {
                Note extraNote = new Note(time, laneHit);
                extraNote.hit = true;
                extraNote.correctHit = false;
                notes.add(extraNote);
            }

            // red when incorrect, green when correct
            Box lane = lanes[laneHit];
            PhongMaterial mat = (PhongMaterial) lane.getMaterial();
            mat.setDiffuseColor(hitCorrectNote ? Color.GREEN : Color.RED);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.2));
            pause.setOnFinished(ev -> mat.setDiffuseColor(Color.rgb(50, 50, 220)));
            pause.play();
        });



        // JavaFX required setup
        stage.setScene(scene);
        stage.setTitle("3D Rhythm Game");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
