package _5010;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Double-buffered game panel for smooth rendering
 */
public class GamePanel extends JPanel {
    private int width;
    private int height;
    private Renderer3D renderer;
    private GameState gameState;
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;

    public GamePanel(int width, int height, Renderer3D renderer, GameState gameState) {
        this.width = width;
        this.height = height;
        this.renderer = renderer;
        this.gameState = gameState;
        
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setBackground(Color.BLACK);
        
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = buffer.createGraphics();
    }

    public void renderFrame() {
        if (bufferGraphics == null) return;
        
        renderer.setGraphics(bufferGraphics);
        renderer.render(gameState);
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer != null) {
            g.drawImage(buffer, 0, 0, null);
        }
    }

    public void cleanup() {
        if (bufferGraphics != null) {
            bufferGraphics.dispose();
            bufferGraphics = null;
        }
    }
}




