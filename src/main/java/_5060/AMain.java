package _5060;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Launches the 4D Crosswordle Gradle project from IntelliJ. */
public final class AMain {
    private AMain() {
        // Utility class; do not instantiate.
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Path gameDirectory = findGameDirectory(Paths.get(System.getProperty("user.dir")));
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder processBuilder = isWindows
                ? new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", "run")
                : new ProcessBuilder("./gradlew", "run");
        processBuilder.directory(gameDirectory.toFile()).inheritIO();

        int exitCode = processBuilder.start().waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException("4D Crosswordle exited with code " + exitCode);
        }
    }

    private static Path findGameDirectory(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            Path[] candidates = {
                    current.resolve("src/main/java/_5060/4DCROSSWORDLE"),
                    current.resolve("_5060/4DCROSSWORDLE"),
                    current.resolve("4DCROSSWORDLE")
            };
            for (Path candidate : candidates) {
                if (Files.isRegularFile(candidate.resolve("build.gradle"))
                        && Files.isRegularFile(candidate.resolve("gradlew.bat"))
                        && Files.isRegularFile(candidate.resolve("gradlew"))) {
                    return candidate;
                }
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find the _5060/4DCROSSWORDLE Gradle project.");
    }
}
