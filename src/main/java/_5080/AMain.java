package _5080;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IntelliJ launcher for the HeartAttack game in the Virus-Defense Gradle project.
 */
public class AMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path gameDirectory = findGameDirectory(Paths.get(System.getProperty("user.dir")));
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder processBuilder = isWindows
                ? new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", "run")
                : new ProcessBuilder("./gradlew", "run");
        processBuilder.directory(gameDirectory.toFile()).inheritIO();

        int exitCode = processBuilder.start().waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Game process exited with code " + exitCode);
        }
    }

    private static Path findGameDirectory(Path start) {
        Path current = start.toAbsolutePath().normalize();
        while (current != null) {
            Path[] candidates = {
                    current.resolve("_5080").resolve("Virus-Defense"),
                    current.resolve("src").resolve("main").resolve("java")
                            .resolve("_5080").resolve("Virus-Defense"),
                    current.resolve("Virus-Defense")
            };
            for (Path candidate : candidates) {
                if (Files.isRegularFile(candidate.resolve("build.gradle"))) {
                    return candidate;
                }
            }

            // Also support running with the game directory as IntelliJ's working directory.
            if (Files.isRegularFile(current.resolve("build.gradle"))
                    && current.getFileName() != null
                    && current.getFileName().toString().equals("Virus-Defense")) {
                return current;
            }
            current = current.getParent();
        }

        throw new IllegalStateException("Could not find _5080/Virus-Defense/build.gradle. "
                + "Set IntelliJ's working directory to the project root.");
    }
}
