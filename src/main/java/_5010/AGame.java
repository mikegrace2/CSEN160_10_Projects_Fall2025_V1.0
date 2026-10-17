package _5010;

import javax.swing.*;

/**
 * Main game class for Cube Collector 3D
 * A polished 3D game where you collect cubes floating in space
 */
public class AGame extends JFrame implements Runnable {
    // Window dimensions
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000 / TARGET_FPS;

    // Core systems
    private GameState gameState;
    private Renderer3D renderer;
    private InputManager inputManager;
    private GamePanel gamePanel;

    private Thread gameThread;
    private boolean running = false;

    public AGame() {
        // Initialize window
        setTitle("Tube Runner 3D");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Initialize systems
        gameState = new GameState();
        renderer = new Renderer3D(WIDTH, HEIGHT);

        // Create game panel with double buffering
        gamePanel = new GamePanel(WIDTH, HEIGHT, renderer, gameState);
        add(gamePanel);
        pack();
        
        // Initialize input manager
        inputManager = new InputManager(gameState);

        // Add input listeners
        addKeyListener(inputManager);
        gamePanel.addKeyListener(inputManager);

        setVisible(true);
        gamePanel.requestFocusInWindow();
        gamePanel.setFocusable(true);
        setFocusable(true);
    }

    public void start() {
        if (running)
            return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            // Cap delta time to prevent huge jumps
            if (deltaTime > 0.1)
                deltaTime = 0.1;

            update(deltaTime);
            render();
            frames++;

            // FPS counter (optional, for debugging)
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                // System.out.println("FPS: " + frames);
                frames = 0;
            }

            // Sleep to maintain target FPS
            try {
                Thread.sleep(FRAME_TIME / 4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update(double deltaTime) {
        // Poll input
        inputManager.poll(deltaTime);

        // Update game state
        if (gameState.getMode() == GameState.GameMode.PLAYING) {
            gameState.update(deltaTime);
        }
    }

    private void render() {
        // Render using double-buffered panel
        if (gamePanel != null) {
            gamePanel.renderFrame();
        }
    }

    public void stop() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (gamePanel != null) {
            gamePanel.cleanup();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AGame game = new AGame();
            game.start();
        });
    }
}

