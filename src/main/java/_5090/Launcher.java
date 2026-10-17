package _5090;

/**
 * Separate launcher entry point for the JavaFX 3D Rhythm Game.
 * JavaFX requires the class passed to the JVM as the main class to NOT
 * directly extend javafx.application.Application when launched from a
 * plain classpath (non-modular) setup, otherwise it throws:
 * "Error: JavaFX runtime components are missing, and are required to run this application".
 * This class works around that by delegating to Main.main().
 */
public class Launcher {
    public static void main(String[] args) {
        AMain.main(args);
    }
}
