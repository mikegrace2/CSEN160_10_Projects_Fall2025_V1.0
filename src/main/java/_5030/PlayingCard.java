package _5030;

import javax.imageio.ImageIO;
import javax.smartcardio.Card;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

// Note: PlayingCard is used primarily as a struct.

public class PlayingCard {
    private int suit; // 0-3
    private int rank; // 0-12
    private BufferedImage image, back;
    private String[] suits = {"H", "D", "C", "S"};
    private String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private String name;

    /* CONSTRUCTORS */
    public PlayingCard() {
        suit = 0;
        rank = 0;
        name = "/images/cards/" + suits[suit] + ranks[rank] + ".png";
        try {
            image = ImageIO.read(getClass().getResourceAsStream(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            back = ImageIO.read(getClass().getResourceAsStream("/images/cards/card_back.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* SHUFFLES CARD */
    public void randomize() {
        suit = (int) (Math.random() * 4);
        rank = (int) (Math.random() * 13);
        name = "/images/cards/" + suits[suit] + ranks[rank] + ".png";
        try {
            image = ImageIO.read(getClass().getResourceAsStream(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* GET / SET FUNCTIONS */
    public int getSuit() { return suit; }
    public int getRank() { return rank; }
    public int getValue() { return 4*suit+rank; }
    public BufferedImage getImage() { return image; }


    /* DRAW FUNCTION */
    public void draw(int x, int y, Graphics g, double scale, int offsetX, int offsetY, ImageObserver obs) {
        g.drawImage(image, offsetX + (int)(x*scale), offsetY + (int)(y*scale), (int) (170.5 * scale), (int) (247.5 * scale), obs);
    }

    public void drawFlip(int x, int y, Graphics g, double scale, int offsetX, int offsetY, double width, ImageObserver obs) {
        if (width > 0)
            g.drawImage(back, offsetX + (int)(x*scale) - (int)(width * scale)/2, offsetY + (int)(y*scale), (int) (width * scale), (int) (135 * scale), obs);
        else if (width > -93)
            g.drawImage(image, offsetX + (int)(x*scale) - (int)(width * scale)/-2, offsetY + (int)(y*scale), (int) (-1 * width * scale), (int) (135 * scale), obs);
        else
            g.drawImage(image, offsetX + (int)(x*scale), offsetY + (int)(y*scale), (int) (93 * scale), (int) (135 * scale), obs);
    }
}
