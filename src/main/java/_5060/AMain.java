package _5060;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Launches the primary Swing Crosswordle game from the copied submission. */
public final class AMain {
    private AMain() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path gameDirectory = findGameDirectory(Paths.get(System.getProperty("user.dir")));
        Path classes = gameDirectory.resolve("2PlayerCrossWordle/out/production/2PlayerCrossWordle");
        Path resources = gameDirectory.resolve("2PlayerCrossWordle/src/main/resources");

        if (!Files.isRegularFile(classes.resolve("com/crosswordle/Main.class"))) {
            throw new IllegalStateException("The Crosswordle classes are missing from " + classes);
        }

        String classPath = classes + System.getProperty("path.separator") + resources;
        Path java = Paths.get(System.getProperty("java.home"), "bin", "java.exe");
        if (!Files.isRegularFile(java)) {
            java = Paths.get(System.getProperty("java.home"), "bin", "java");
        }

        int exitCode = new ProcessBuilder(java.toString(), "-cp", classPath, "com.crosswordle.Main")
                .directory(gameDirectory.resolve("2PlayerCrossWordle").toFile())
                .inheritIO()
                .start()
                .waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException("Crosswordle exited with code " + exitCode);
        }
    }

    private static Path findGameDirectory(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            Path nested = current.resolve("src/main/java/_5060");
            if (Files.isRegularFile(nested.resolve("2PlayerCrossWordle/out/production/2PlayerCrossWordle/com/crosswordle/Main.class"))) {
                return nested;
            }
            if (Files.isRegularFile(current.resolve("2PlayerCrossWordle/out/production/2PlayerCrossWordle/com/crosswordle/Main.class"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find the copied _5060 Crosswordle project.");
    }
}
