package _5020;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

// handles all game logic such as flipping cards, matching, winning, and losing
// calculates and keeps track of the scores

public class MemoryGame extends JFrame {
    private final int rows, cols, timeLimitSeconds, moveLimit;
    private final JPanel boardPanel = new JPanel();
    private final JLabel timeLabel = new JLabel(), movesLabel = new JLabel(),
            scoreLabel = new JLabel(), bestLabel = new JLabel();

    private final List<CardButton> cards = new ArrayList<>();
    private CardButton first = null, second = null;
    private boolean busy = false;
    private Timer flipBackTimer, gameTimer;
    private int timeLeft, moves, matchesFound;

    // uses Java Preferences API to store scores
    private final Preferences prefs = Preferences.userNodeForPackage(MemoryGame.class);

    private static final int MAX_IMAGE_PROBE = 50;
    private static final int IMAGE_W = 300;
    private static final int IMAGE_H = 250;
    private ImageIcon panIcon;

    // constructor to save the game settings and initialize the UI
    public MemoryGame(int rows, int cols, int timeLimitSeconds, int moveLimit) {
        this.rows = rows;
        this.cols = cols;
        this.timeLimitSeconds = timeLimitSeconds;
        this.moveLimit = moveLimit;

        initUI(); // building the screen

        pack();
        setTitle("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        panIcon = loadPanIcon(IMAGE_W, IMAGE_H); // loading the pan image

        setVisible(true);
        startNewGame(); // starts the game
    }

    // building the layout
    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));

        // button to go back to the home screen
        RoundButton backButton = new RoundButton("\u25C0", new Color(180, 40, 40), Color.WHITE); // ◀
        backButton.addActionListener(e -> {
            if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop(); // stop the game timer
            if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop(); // stop the flip back timer
            dispose();
            new HomeScreen();
        });

        // button to pause the game
        RoundButton pauseButton = new RoundButton("\u23F8", new Color(30, 140, 140), Color.WHITE); // ⏸
        pauseButton.addActionListener(e -> togglePause());

        top.add(backButton);
        top.add(pauseButton);

        // labels to show the time, moves, score, and best time at the top of the screen
        top.add(timeLabel); top.add(movesLabel); top.add(scoreLabel); top.add(bestLabel);
        add(top, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(rows, cols, 6, 6));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(boardPanel, BorderLayout.CENTER);
    }

    // pause button functionality
    private void togglePause() {
        if (busy) return;
        boolean gameWasRunning = gameTimer != null && gameTimer.isRunning(); // check if the game timer was running
        boolean flipWasRunning = flipBackTimer != null && flipBackTimer.isRunning(); // check if the flip back timer was running

        if (gameWasRunning) gameTimer.stop();
        if (flipWasRunning) flipBackTimer.stop();

        busy = true; // set busy to true to prevent interactions
        cards.forEach(b -> b.setEnabled(false)); // disable all cards

        // Pause dialog with resume button
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(233, 164, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel pauseLabel = new JLabel("Paused", SwingConstants.CENTER);
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 36));
        pauseLabel.setForeground(Color.WHITE);
        panel.add(pauseLabel, BorderLayout.CENTER);

        RoundedRectButton resumeButton = new RoundedRectButton("Resume", 20);
        resumeButton.setFont(new Font("Arial", Font.BOLD, 20));
        resumeButton.setBackground(new Color(118, 48, 8));
        resumeButton.setForeground(Color.WHITE);
        resumeButton.setPreferredSize(new Dimension(150, 50));

        // Clicking resume closes the dialog and resumes the game
        JDialog pauseDialog = new JDialog(this, "Pause", true);
        resumeButton.addActionListener(e -> pauseDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(resumeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        pauseDialog.setContentPane(panel);
        pauseDialog.setSize(350, 200);
        pauseDialog.setLocationRelativeTo(this);
        pauseDialog.setVisible(true);
        // End of Custom Pause Dialog

        // resuming the game
        cards.forEach(b -> { if (!b.isMatched()) b.setEnabled(true); }); // re-enable unmatched cards
        busy = false; // clear busy flag
        if (gameWasRunning && gameTimer != null) gameTimer.start();
        if (flipWasRunning && flipBackTimer != null) flipBackTimer.restart();
    }

    // starts a new game by initializing the cards and timers
    private void startNewGame() {
        // helper class to define a card with its image and id for the matching logic
        class CardDef {
            final ImageIcon icon;
            final Integer id;
            CardDef(ImageIcon icon, Integer id) { this.icon = icon; this.id = id; }
        }

        cards.clear();
        boardPanel.removeAll();
        first = null; second = null; busy = false;
        moves = 0; matchesFound = 0; timeLeft = timeLimitSeconds; // reset game state

        int pairs = (rows * cols) / 2; // calculate number of pairs of cards needed

        List<ImageIcon> images = new ArrayList<>(); // list to hold loaded images of the food
        // first try loading images named image1.jpg, image2.jpg, etc.
        for (int i = 1; i <= Math.min(pairs, MAX_IMAGE_PROBE); i++) {
            String candidate = "image" + i + ".jpg";
            if (candidate.toLowerCase().contains("pan")) continue;
            ImageIcon ic = loadImageIcon(candidate, IMAGE_W, IMAGE_H);
            if (ic != null) images.add(ic); // add loaded image to the list
        }

        // if not enough images are found, scan other directories for more images
        if (images.size() < pairs) {
            List<File> found = new ArrayList<>();
            String[] scanRoots = new String[] { "images", "resources", "." };
            for (String root : scanRoots) {
                collectImageFiles(new File(root), found, 3, Math.max(10, pairs));
                if (found.size() >= pairs) break;
            }
            for (File f : found) {
                if (images.size() >= pairs) break;
                String name = f.getName().toLowerCase();
                if (name.contains("pan")) continue;
                ImageIcon ic = loadImageIcon(f, IMAGE_W, IMAGE_H);
                if (ic != null) images.add(ic);
            }
            for (File f : found) {
                if (images.size() >= pairs) break;
                ImageIcon ic = loadImageIcon(f, IMAGE_W, IMAGE_H);
                if (ic != null && !images.contains(ic)) images.add(ic);
            }
        }

        // if still no images found, create a placeholder image
        if (images.isEmpty()) {
            images.add(makePlaceholderIcon(IMAGE_W, IMAGE_H, Color.GRAY, "IMG"));
        }

        // select images for the pairs needed
        List<ImageIcon> pairImages = new ArrayList<>();
        Random rnd = new Random();

        if (images.size() >= pairs) {
            Collections.shuffle(images, rnd); // shuffle the images randomly
            pairImages.addAll(images.subList(0, pairs)); // take only as many as needed
        } else {
            // not enough unique images, allow duplicates
            while (pairImages.size() < pairs) {
                pairImages.add(images.get(rnd.nextInt(images.size())));
            }
        }

        List<CardDef> defs = new ArrayList<>(); // create card definitions for each pair
        int nextId = 0;
        // each image gets two cards with the same id
        for (ImageIcon img : pairImages) {
            defs.add(new CardDef(img, nextId));
            defs.add(new CardDef(img, nextId));
            nextId++;
        }
        Collections.shuffle(defs, rnd);

        // create CardButton instances and add them to the board
        for (CardDef d : defs) {
            CardButton cb = new CardButton(d.icon, panIcon, d.id);
            cb.addActionListener(this::onCardClicked);
            cards.add(cb);
            boardPanel.add(cb);
        }

        updateLabels();
        revalidate();
        repaint();

        // setting up the timers for flipping back cards and the game timer countdown
        if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop();
        flipBackTimer = new Timer(700, e -> {
            if (first != null) first.showBack();
            if (second != null) second.showBack();
            // reset selection and busy flag
            first = null; second = null; busy = false;
        });
        // only run once per mismatch
        flipBackTimer.setRepeats(false);

        // game timer to count down the time left
        if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
        gameTimer = new Timer(1000, e -> {
            timeLeft--; updateLabels();
            if (timeLeft <= 0) {
                if (gameTimer != null) gameTimer.stop();
                // timer runs out - end the game
                if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop();
                busy = true; // set busy to prevent interactions
                cards.forEach(cb -> cb.setEnabled(false)); // disable all cards
                new TimeUpScreen(this, rows, cols, timeLimitSeconds, moveLimit); // show time-up screen
            }
        });
        gameTimer.start(); // start the game timer
    }

    // handles card click events
    private void onCardClicked(ActionEvent evt) {
        if (busy) return; // this ignores clicks when busy, like during the flip animation
        CardButton cb = (CardButton) evt.getSource(); // storing the clicked card
        if (cb.isMatched() || cb == first) return; // ignore if already matched or same card clicked

        cb.showFront();

        if (first == null) { first = cb; return; } // first card selected
        else {
            second = cb;
            moves++; updateLabels();
            if (moves > moveLimit) {
                if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
                if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop();
                busy = true;
                cards.forEach(c -> c.setEnabled(false));
                new MovesUpScreen(this, rows, cols, timeLimitSeconds, moveLimit);
                return;
            }

            // check for a match between the two clicked cards
            boolean match = false;
            Object id1 = first.getId();
            Object id2 = second.getId();
            if (id1 != null && id2 != null) match = id1.equals(id2); // compare ids
            else { // fallback to comparing images if no ids
                ImageIcon f1 = first.getFrontIcon();
                ImageIcon f2 = second.getFrontIcon();
                match = (f1 != null && f1.equals(f2));
            }

            // handling the match or mismatch
            if (match) {
                first.setMatched(true);
                second.setMatched(true);
                matchesFound++; // increment matches found
                first = null; second = null; // reset the card clicks
                updateLabels();
                int pairs = (rows * cols) / 2; // total pairs needed
                if (matchesFound >= pairs) {
                    // stop timers and show victory screen
                    if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
                    if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop();
                    endGame(true);
                }
            } else { // if it wasn't a match, flip back the cards after a delay
                busy = true;
                flipBackTimer.restart();
            }
        }
    }

    // ends the game and shows the respective end screen
    private void endGame(boolean won) {
        // disable further interactions once the game is over
        busy = true;
        if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
        if (flipBackTimer != null && flipBackTimer.isRunning()) flipBackTimer.stop();
        cards.forEach(cb -> { if (!cb.isMatched()) cb.setEnabled(false); }); // disable unmatched cards

        int elapsed = timeLimitSeconds - timeLeft;
        int score = Math.max(0, matchesFound * 100 - moves * 2 - elapsed);
        updateLabels();

        // Save score to history in the API
        String history = prefs.get("scoreHistory", "");
        if (history.isEmpty()) history = String.valueOf(score);
        else history = history + "," + score;
        prefs.put("scoreHistory", history);

        if (won) {
            // if the player won, check and update best time
            int best = prefs.getInt("bestTime", Integer.MAX_VALUE);
            if (elapsed < best) prefs.putInt("bestTime", elapsed);
            new VictoryScreen(this, rows, cols, timeLimitSeconds, moveLimit, elapsed, moves, score); // show victory screen
        } else {
            // fallback - treat as time-up if reason unknown
            new TimeUpScreen(this, rows, cols, timeLimitSeconds, moveLimit); // show time-up screen
        }
    }

    // ----------Helper functions----------

    // updates the labels in the top bar showing time, moves, score, and best time
    private void updateLabels() {
        timeLabel.setText("Time: " + timeLeft + "s");
        movesLabel.setText("Moves: " + moves + "/" + moveLimit);
        // each correct match increases score by a 100 * #of pairs and every attempted pair costs 2 points
        // every seconds that passes reduces 1 point
        int score = Math.max(0, matchesFound * 100 - moves * 2 - (timeLimitSeconds - timeLeft)); // score calculation
        scoreLabel.setText("Score: " + score);
        int best = prefs.getInt("bestTime", Integer.MAX_VALUE); // retrieve best time
        bestLabel.setText("Best time: " + (best == Integer.MAX_VALUE ? "-" : best + "s")); // display best time at the top
    }

    // loads the pan icon used for the back of all the cards
    private ImageIcon loadPanIcon(int w, int h) {
        String[] candidates = new String[] { "pan.png", "pan.jpg", "pan.jpeg" };
        for (String c : candidates) {
            ImageIcon ic = loadImageIcon(c, w, h);
            if (ic != null) return ic;
        }
        return makePlaceholderIcon(w, h, new Color(120, 80, 40), "PAN");
    }

    private ImageIcon loadImageIcon(String nameOrPath, int w, int h) {
        URL res = MemoryGame.class.getResource("/" + nameOrPath);
        if (res != null) {
            try {
                BufferedImage img = ImageIO.read(res);
                if (img != null) return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } catch (IOException ignored) {}
        }
        File f = new File(nameOrPath);
        if (f.exists()) return loadImageIcon(f, w, h);
        return null;
    }

    private ImageIcon loadImageIcon(File file, int w, int h) {
        if (file == null || !file.exists()) return null;
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return null;
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (IOException ignored) { return null; }
    }

    private void collectImageFiles(File dir, List<File> out, int maxDepth, int maxFiles) {
        if (dir == null || !dir.exists() || maxDepth < 0 || out.size() >= maxFiles) return;
        if (dir.isFile()) {
            String n = dir.getName().toLowerCase();
            if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif")) out.add(dir);
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (out.size() >= maxFiles) break;
            if (f.isDirectory()) collectImageFiles(f, out, maxDepth - 1, maxFiles);
            else {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif")) out.add(f);
            }
        }
    }

    private ImageIcon makePlaceholderIcon(int w, int h, Color bg, String text) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, Math.max(12, w / 8)));
        FontMetrics fm = g.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
        g.dispose();
        return new ImageIcon(img);
    }

    class RoundedRectButton extends JButton {
        private final int radius;

        public RoundedRectButton(String text, int radius) {
            super(text);
            this.radius = radius;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false); // prevent default background paint
            setBorderPainted(false); // no outline border
            setForeground(Color.WHITE); // text color
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill background with rounded shape
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        public void updateUI() {
            // prevents UI manager from overriding custom look
            super.updateUI();
            setContentAreaFilled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoryGame(2, 4, 60, 50));
    }

    // small RoundButton helper reused in this file
    private static class RoundButton extends JButton {
        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            setBackground(bg);
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = Math.min(40, Math.min(width, height) / 4);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }
}
