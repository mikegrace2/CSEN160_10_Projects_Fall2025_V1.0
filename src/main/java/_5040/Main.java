package _5040;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

/**
 * The Main class represents the primary drawing canvas of the Spectral Layers application.
 * It handles user interaction with layers, including rendering all layers and resizing layers/
 * Main also works interacts with ToolbarPanel and LayerManager to allow users to interact with the app.
 */
public class Main extends JPanel {
    private LayerManager manager;
    private Point lastMouse;
    private Layer.HandlePosition activeHandle = Layer.HandlePosition.NONE;
    private Layer copiedLayer = null;
    // Simple undo stack (stores deleted layers)
    private final Stack<Layer> undoStack = new Stack<>();

    /**
     * Main() constructs the main drawing canvas, initializes mouse and keyboard listeners,
     * and configures interaction logic for layer actions like selecting, dragging, resizing, deleting,
     * and undoing.
     */
    public Main() {
        manager = new LayerManager();

        // Initial layers for testing
//        manager.addLayer(new Layer(new Color(0, 255, 255, 180), 0.8f, BlendMode.MULTIPLY, new Rectangle(150, 150, 250, 250))); // Cyan
//        manager.addLayer(new Layer(new Color(255, 0, 0, 180), 0.8f, BlendMode.MULTIPLY, new Rectangle(100, 100, 250, 250))); // Red

        // Make sure we can capture keyboard input
        setFocusable(true);
        requestFocusInWindow();

        // Mouse logic
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow(); // regain focus after clicking toolbar
                lastMouse = e.getPoint();

                // Step 1: Check for resize handle on selected layer
                activeHandle = manager.getHandleAt(e.getPoint());

                // Step 2: If not resizing, check if clicking on a layer body
                if (activeHandle == Layer.HandlePosition.NONE) {
                    manager.selectLayerByClick(e.getPoint());
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeHandle = Layer.HandlePosition.NONE;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (manager.getSelectedLayer() != null) {
                    int dx = e.getX() - lastMouse.x;
                    int dy = e.getY() - lastMouse.y;

                    if (activeHandle != Layer.HandlePosition.NONE) {
                        // resize
                        manager.getSelectedLayer().resize(activeHandle, dx, dy);
                    } else {
                        // move
                        manager.moveSelectedLayer(dx, dy);
                    }

                    lastMouse = e.getPoint();
                    repaint();
                }
            }
        });

        // Keyboard shortcuts
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Backspace to delete layer
                if (key == KeyEvent.VK_BACK_SPACE) {
                    Layer selected = manager.getSelectedLayer();
                    if (selected != null) {
                        undoStack.push(selected); // store for undo
                        manager.deleteSelectedLayer();
                        repaint();
                    }
                }

                // Ctrl+Z to undo actions
                if (e.isControlDown() && key == KeyEvent.VK_Z) {
                    if (!undoStack.isEmpty()) {
                        Layer restored = undoStack.pop();
                        manager.addLayer(restored);
                        manager.selectLayer(manager.getLayers().size() - 1); // select it again
                        repaint();
                    }
                }


                // Add this to your existing key listener:
                if (e.isControlDown() && key == KeyEvent.VK_C) {
                    Layer selected = manager.getSelectedLayer();
                    if (selected != null) {
                        copiedLayer = selected; // Just store reference
                    }
                }

                if (e.isControlDown() && key == KeyEvent.VK_V) {
                    if (copiedLayer != null) {
                        // Create a copy with offset
                        Layer pastedLayer = new Layer(
                                copiedLayer.getColor(),
                                copiedLayer.getOpacity(),
                                copiedLayer.getBlendMode(),
                                new Rectangle(
                                        copiedLayer.getShape().x + 20,
                                        copiedLayer.getShape().y + 20,
                                        copiedLayer.getShape().width,
                                        copiedLayer.getShape().height
                                )
                        );
                        manager.addLayer(pastedLayer);
                        repaint();
                    }
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.isControlDown() || e.isMetaDown()) && e.getKeyCode() == KeyEvent.VK_Z) {
                    manager.undo();
                    repaint();
                }
            }
        });

    }

    /**
     * The paintComponent(Graphics g) function renders all layers using the Renderer
     * and draws resize handles on the currently selected layer. This method is automatically run
     * when the canvas is edited.
     *
     * @param g the Graphics context used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Renderer renderer = new Renderer((Graphics2D) g);
        renderer.draw(manager.getLayers());

        // draw resize handles for selected layer. White corner thingys
        Layer selected = manager.getSelectedLayer();
        if (selected != null) {
            selected.drawHandles((Graphics2D) g);
        }
    }

    /**
     * getManager() returns the LayerManager used by this canvas
     *
     * @return the LayerManager instance
     */
    public LayerManager getManager() {
        return manager;
    }

    /**
     * Launches the Spectral Layers app. This initializes the main window,
     * displays the welcome page, and sets up the canvas and toolbar.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Spectral Layers");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);

            WelcomePage welcome = new WelcomePage(frame); // null parent since no main window yet
            welcome.setVisible(true);

            // Create canvas and toolbar
            Main canvas = new Main();
            canvas.setPreferredSize(new Dimension(780, 600)); // sets the size of the canvas, essential
            ToolbarPanel toolbar = new ToolbarPanel(canvas.getManager(), canvas::repaint);

            // Wrap them in a parent panel using BorderLayout
            JPanel rootPanel = new JPanel(new BorderLayout());
            rootPanel.add(canvas, BorderLayout.CENTER);
            rootPanel.add(toolbar, BorderLayout.EAST);

            frame.setContentPane(rootPanel);
            frame.pack(); // respects preferred sizes
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Ensure focus returns to canvas so Backspace works
            SwingUtilities.invokeLater(canvas::requestFocusInWindow);
        });
    }
}
