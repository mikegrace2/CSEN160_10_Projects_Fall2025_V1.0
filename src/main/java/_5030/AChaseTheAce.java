package _5030;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.lang.Thread.sleep;

public class AChaseTheAce extends JFrame implements KeyListener{

    private GameSystem system;
    private int initWidth = 1120, initHeight = 630;

    private BufferedImage image;


    /* CREATES THE PANEL */
    public AChaseTheAce() {
        setTitle("Chase the Ace");
        setSize(initWidth, initHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        system = new GameSystem();

        DrawingPanel panel = new DrawingPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;


        add(panel);
        setVisible(true);
        panel.requestFocusInWindow();
        panel.addKeyListener(this);

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/images/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* DRAWING COMPONENT */
    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.BLACK);

            double scale;
            int offsetX, offsetY;
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            if (panelWidth / 16 <= panelHeight / 9) {
                scale = (double) panelWidth / (double) initWidth;
                offsetX = 0;
                offsetY = (panelHeight - (int)(initHeight * scale)) / 2;
            }
            else {
                scale = (double) panelHeight / (double) initHeight;
                offsetX = (panelWidth - (int)(initWidth * scale)) / 2;
                offsetY = 0;
            }

            g.setColor(Color.WHITE);
            g.fillRect(offsetX, offsetY, (int) (initWidth * scale), (int) (initHeight * scale));
            g.drawImage(image, offsetX, offsetY, (int) (initWidth * scale), (int) (initHeight * scale), this);
            PlayingCard card = new PlayingCard();
            try {
                system.draw(g, scale, offsetX, offsetY, this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            g.setColor(Color.BLACK);
            g.fillRect(offsetX, offsetY+(int)(initHeight*scale), (int) (initWidth * scale), (int) (initHeight * scale));
            try {
                sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            repaint();
        }
    }


    /* DETECTS KEY INPUTS */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (system.getWaiting() == 0 && keyCode == KeyEvent.VK_ENTER) {
        //if (keyCode == KeyEvent.VK_ENTER) {
            System.out.println("Enter pressed!");
            system.setWaiting(1);
            repaint();
        } else if (system.getWaiting() == 0 && keyCode == KeyEvent.VK_SPACE) {

        //else if (keyCode == KeyEvent.VK_SPACE) {
            System.out.println("Space pressed!");
            //system.shuffle();
            system.setWaiting(2);
            repaint();
        }
    }

    /* I HAVE TO INCLUDE THESE OR THE CODE BREAKS */
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}


    /* MAIN FUNCTION */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AChaseTheAce();

        });
    }
}