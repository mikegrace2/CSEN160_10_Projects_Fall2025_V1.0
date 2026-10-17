package _5020;

import javax.swing.SwingUtilities;

// Main class to launch the Memory Game application
public class AMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomeScreen());
    }
}
