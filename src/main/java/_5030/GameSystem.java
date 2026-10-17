package _5030;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class GameSystem {

    private boolean[] availableCards;
    private Player[] turnOrder;
    private int turnStart, turn, playerCount, lowestRank;
    private PlayingCard deckCard;
    public PlayingCard traded;
    private PlayingCard prevCard;
    private int[] lifeCount = {3, 3, 3, 3}; // for animations
    private BufferedImage table, b1, b2, b3, a11, a12, a21, a22, a31, a32, lives, c1, c2, c3;
    private BufferedImage h11, h12, h13, h21, h22, h23, h31, h32, h33, outline;

    // just a ton of flags
    private int gameOver; // 0 for not over, 1 for loss, 2 for win
    private int waiting;
    private int iterator, itMove, itShow;
    private String message;


    /* INITIALIZES VARIABLES */
    public GameSystem() {
        availableCards = new boolean[52];
        playerCount = 4;
        turnOrder = new Player[playerCount];
        deckCard = new PlayingCard();
        gameOver = 3;
        iterator = -1;
        itMove = -1;
        itShow = -1;

        try {
            table = ImageIO.read(getClass().getResourceAsStream("/images/table.png"));
            b1 = ImageIO.read(getClass().getResourceAsStream("/images/chars/body1.png"));
            b2 = ImageIO.read(getClass().getResourceAsStream("/images/chars/body2.png"));
            b3 = ImageIO.read(getClass().getResourceAsStream("/images/chars/body3.png"));
            a11 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms1-1.png"));
            a12 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms1-2.png"));
            a21 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms2-1.png"));
            a22 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms2-2.png"));
            a31 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms3-1.png"));
            a32 = ImageIO.read(getClass().getResourceAsStream("/images/chars/arms3-2.png"));
            h11 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head1-1.png"));
            h12 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head1-2.png"));
            h13 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head1-3.png"));
            h21 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head2-1.png"));
            h22 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head2-2.png"));
            h23 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head2-3.png"));
            h31 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head3-1.png"));
            h32 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head3-2.png"));
            h33 = ImageIO.read(getClass().getResourceAsStream("/images/chars/head3-3.png"));
            c1 = ImageIO.read(getClass().getResourceAsStream("/images/chips1.png"));
            c2 = ImageIO.read(getClass().getResourceAsStream("/images/chips2.png"));
            c3 = ImageIO.read(getClass().getResourceAsStream("/images/chips3.png"));
            outline = ImageIO.read(getClass().getResourceAsStream("/images/cards/card_outline.png"));
            lives = ImageIO.read(getClass().getResourceAsStream("/images/lives.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < 52; i++) { availableCards[i] = true; }
        for (int i = 0; i < playerCount; i++) { turnOrder[i] = new Player(i); }
        setWaiting(0);
    }


    /* INITIALIZES VARIABLES */
    public GameSystem(int count) {
        availableCards = new boolean[52];
        playerCount = count;
        turnOrder = new Player[playerCount];
        for(int i = 0; i < 52; i++) { availableCards[i] = true; }
        for (int i = 0; i < playerCount; i++) { turnOrder[i] = new Player(i); }
    }


    /* STARTS A ROUND */
    public void start() {
        iterator = -10;
        Thread thread = new UserInput(this);
        thread.start();
    }


    /* SHUFFLES THE HANDS */
    public void shuffle() {
        for (int i = 0; i < 52; i++) { availableCards[i] = true; }

        for (int i = 0; i < playerCount; i++) {
            getPlayer(i).getCard().randomize();
            if (!availableCards[getPlayer(i).getCard().getValue()])
                i--;
            else
                availableCards[getPlayer(i).getCard().getValue()] = false;
        }
        deckCard.randomize();
        while (!availableCards[deckCard.getValue()]) {
            deckCard.randomize();
        }
        availableCards[deckCard.getValue()] = false;
    }


    /* ELIMINATIONS */
    public void endRound() {
        //Determine what the lowest card is
        System.out.println("Ending Round...");
        //setPrevCard(turnOrder[0].getCard());
        lowestRank = 13;
        for (int i = 0; i < playerCount; i++) {
            if (turnOrder[i].getCard().getRank() < lowestRank) {
                lowestRank = turnOrder[i].getCard().getRank();
            }
        }

        //itMove = 0;

        //Remove lives from the player(s) with the lowest card(s)
        for (int i = 0; i < playerCount; i++) {
            if (turnOrder[i].getCard().getRank() == lowestRank) {
                turnOrder[i].loseLife();
                if (turnOrder[i].getLives() <= 0) {


                    //End the game if the main player is eliminated
                    if (i == 0) {
                        //Game Over
                        gameOver = 1;
                        setWaiting(0);
                        return;

                        //Eliminate a player by shifting all players down
                    } else {
                        Player[] copy = new Player[playerCount - 1];
                        System.arraycopy(turnOrder, 0, copy, 0, i);
                        System.arraycopy(turnOrder, i + 1, copy, i, playerCount - i - 1);
                        turnOrder = copy;
                        playerCount--;
                    }
                }
            }

        }
        for (int i = 0; i < 4; i++) lifeCount[i] = 0;
        for (int i = 0; i < playerCount; i++) lifeCount[turnOrder[i].getId()] = turnOrder[i].getLives();
        if(playerCount == 1){
            gameOver = 2;
            setWaiting(0);
        } else{
            start();
        }
    }


    /* GET / SET FUNCTIONS */
    public int getPlayerCount() { return playerCount; }
    public Player getPlayer(int index) { return turnOrder[index]; }
    public int getWaiting() { return waiting; }
    public void setWaiting(int var) { waiting = var; }
    public Player[] getTurnOrder() { return turnOrder; }
    public int getTurn() { return turn; }
    public void setTurn(int turn) { this.turn = turn; }
    public int getTurnStart() { return turnStart; }
    public PlayingCard getTraded() { return traded; }
    public PlayingCard getDeckCard() { return deckCard; }
    public void setPrevCard(PlayingCard prevCard) { this.prevCard = prevCard; }
    public void setGameOver(int gameOver) { this.gameOver = gameOver; }
    public void setIterator(int iterator) { this.iterator = iterator; }
    public void setItMove(int itMove) { this.itMove = itMove; }
    public void setItShow(int itShow) { this.itShow = itShow; }
    public void setMessage (String message) { this.message = message; }
    public void setLowestRank (int lowestRank) { this.lowestRank = lowestRank; }

    /* FONT CREATION */
    public class FontLoaderExample extends JPanel {
        private Font customFont;

        public FontLoaderExample() {
            try (InputStream is = getClass().getResourceAsStream("/fonts/Minecraft.ttf")) {
                // load font from classpath resource `\`/fonts/MyFont.ttf\``
                Font f = Font.createFont(Font.TRUETYPE_FONT, is);
                // register so it's available to the JVM
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                // derive a size (float) and style if needed
                customFont = f.deriveFont(Font.PLAIN, 24f);
                // optionally set as default for this component
                setFont(customFont);
            } catch (Exception ex) {
                ex.printStackTrace();
                // fallback to a logical font
                customFont = new Font("Serif", Font.PLAIN, 24);
            }
        }

        public Font getCustomFont() {
            return customFont;
        }
    }

    /* DRAWING FUNCTION */
    public void draw(Graphics g, double scale, int offsetX, int offsetY, ImageObserver obs) throws IOException {
        if (gameOver == 0) {

            FontLoaderExample f = new FontLoaderExample();
            g.setFont(f.getCustomFont());

            for (int i = 0; i < getPlayer(0).getLives(); i++) {
                g.drawImage(lives, offsetX + (int)((1019-73*i)*scale), offsetY + (int)(25*scale), (int) (66 * scale), (int) (66 * scale), obs);
            }

            g.drawImage(b1, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
            g.drawImage(b2, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
            g.drawImage(b3, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            g.drawImage(table, offsetX + (int)(120*scale), offsetY + (int)(388*scale), (int)(880 * scale), (int)(242 * scale), obs);

            if (lifeCount[1] == 0) // first bot's arms
                g.drawImage(a11, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
            else g.drawImage(a12, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
            if (lifeCount[2] == 0) // second bot's arms
                g.drawImage(a21, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
            else g.drawImage(a22, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
            if (lifeCount[3] == 0) // third bot's arms
                g.drawImage(a31, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            else g.drawImage(a32, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);

            if (turn == 0) { // user's turn
                if (waiting == 0) {
                    g.drawString("Your Turn!", offsetX + (int) (500 * scale), offsetY + (int) (45 * scale));
                    g.drawString("[SPACE] to Stay", offsetX + (int) (890 * scale), offsetY + (int) (590 * scale));
                    g.drawString("[ENTER] to Trade", offsetX + (int) (890 * scale), offsetY + (int) (620 * scale));
                }
                g.drawImage(h11, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
                g.drawImage(h22, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
                g.drawImage(h33, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            } else if (turn == 1 && lifeCount[1] > 0) { // first bot's turn
                g.drawImage(h12, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
                g.drawImage(h21, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
                g.drawImage(h32, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            } else if ((turn == 2 && lifeCount[1] > 0 && lifeCount[2] > 0) || (turn == 1 && lifeCount[1] == 0 && lifeCount[2] > 0)) { // second bot's turn
                g.drawImage(h13, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
                g.drawImage(h22, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
                g.drawImage(h31, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            } else { // third bot's turn
                g.drawImage(h12, offsetX + (int)(740*scale), offsetY + (int)(140*scale), (int)(280 * scale), (int)(490 * scale), obs);
                g.drawImage(h23, offsetX + (int)(425*scale), offsetY + (int)(80*scale), (int)(290 * scale), (int)(540 * scale), obs);
                g.drawImage(h32, offsetX + (int)(80*scale), offsetY + (int)(140*scale), (int) (340 * scale), (int) (490 * scale), obs);
            }

            // draw chips
            if (lifeCount[1] > 2) g.drawImage(c3, offsetX + (int)(800*scale), offsetY + (int)(480*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[1] > 1) g.drawImage(c2, offsetX + (int)(830*scale), offsetY + (int)(500*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[1] > 0) g.drawImage(c1, offsetX + (int)(790*scale), offsetY + (int)(520*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[2] > 2) g.drawImage(c3, offsetX + (int)(660*scale), offsetY + (int)(370*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[2] > 1) g.drawImage(c2, offsetX + (int)(700*scale), offsetY + (int)(375*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[2] > 0) g.drawImage(c1, offsetX + (int)(680*scale), offsetY + (int)(390*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[3] > 2) g.drawImage(c3, offsetX + (int)(220*scale), offsetY + (int)(430*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[3] > 1) g.drawImage(c2, offsetX + (int)(190*scale), offsetY + (int)(440*scale), (int) (27 * scale), (int) (63 * scale), obs);
            if (lifeCount[3] > 0) g.drawImage(c1, offsetX + (int)(170*scale), offsetY + (int)(470*scale), (int) (27 * scale), (int) (63 * scale), obs);

            // DRAWS THE OPPONENTS' MESSAGES
            if (itShow > -1) {
                //g.drawString(message, offsetX + (int) (300 * scale), offsetY + (int) ((300-itShow) * scale));

                if (getPlayer(turn).getId() == 1)
                    g.drawString(message, offsetX + (int) (800 * scale), offsetY + (int) ((300-itShow) * scale));
                else if (getPlayer(turn).getId() == 2)
                    g.drawString(message, offsetX + (int) (450 * scale), offsetY + (int) ((250-itShow) * scale));
                else if (getPlayer(turn).getId() == 3)
                    g.drawString(message, offsetX + (int) (300 * scale), offsetY + (int) ((300-itShow) * scale));
                else if (turn == 0) //(getPlayer(turn).getId() == 3)
                    g.drawString(message, offsetX + (int) (600 * scale), offsetY + (int) ((400-itShow) * scale));

                if (itShow > 20)
                    itShow = -2;
                itShow++;
            }


            // DRAWS THE USER'S CARD & ANIMATING IT
            if (itMove > -1) {
                if (itMove < 14)
                    prevCard.draw(475, 775 - (int)(400 *Math.cos(itMove/10.0)), g, scale, offsetX, offsetY, obs);
                else
                    getPlayer(0).getCard().draw(475, 775 - (int)(400 *Math.cos(itMove/10.0 + 3.14)), g, scale, offsetX, offsetY, obs);
                if (itMove < 32)
                    itMove++;
                else
                    itMove = -1;
            } else {
                getPlayer(0).getCard().draw(475, 375, g, scale, offsetX, offsetY, obs);
            }

            /*for (int i = 1; i < playerCount; i++) {
                if (i == turn) getPlayer(i).draw(100 + 150 * i, 100, g, scale, offsetX, offsetY, Color.RED, obs);
                else getPlayer(i).draw(100 + 150 * i, 100, g, scale, offsetX, offsetY, Color.BLUE, obs);
            } */

            // DRAWS CARD FLIP
            if (iterator > -1) {
                g.drawString("Revealing Cards!", offsetX + (int) (470 * scale), offsetY + (int) (45 * scale));
                for (int i = 1; i < playerCount; i++) {
                    if (getPlayer(i).getId() == 1)
                        getPlayer(i).getCard().drawFlip(900, 400, g, scale, offsetX, offsetY, 93 * Math.cos(Math.min(iterator,22) / 7.0), obs);
                    else if (getPlayer(i).getId() == 2)
                        getPlayer(i).getCard().drawFlip(750, 300, g, scale, offsetX, offsetY, 93 * Math.cos(Math.min(iterator,22) / 7.0), obs);
                    else if (getPlayer(i).getId() == 3)
                        getPlayer(i).getCard().drawFlip(150, 350, g, scale, offsetX, offsetY, 93*Math.cos(Math.min(iterator,22)/7.0), obs);
                }
                iterator++;
                if (iterator > 27) {
                    for (int i = 0; i < playerCount; i++) {
                        if (getPlayer(i).getCard().getRank() == lowestRank) {
                            if (getPlayer(i).getId() == 0) g.drawImage(outline, offsetX + (int)(470*scale), offsetY + (int)(369.5*scale), (int) (181.5 * scale), (int) (258.5 * scale), obs);
                            else if (getPlayer(i).getId() == 1) g.drawImage(outline, offsetX + (int)((900.5-49.5)*scale), offsetY + (int)(397*scale), (int) (99 * scale), (int) (141 * scale), obs);
                            else if (getPlayer(i).getId() == 2) g.drawImage(outline, offsetX + (int)((750.5-49.5)*scale), offsetY + (int)(297*scale), (int) (99 * scale), (int) (141 * scale), obs);
                            else g.drawImage(outline, offsetX + (int)((151-49.5)*scale), offsetY + (int)(347*scale), (int) (99 * scale), (int) (141 * scale), obs);
                        }
                    }
                }
            }


        }
        else { // game over screens
            g.setColor(Color.BLACK);
            g.fillRect(offsetX, offsetY, (int) (1120 * scale), (int) (630 * scale));
            g.setColor(Color.WHITE);
            FontLoaderExample f = new FontLoaderExample();
            g.setFont(f.getCustomFont());
            if (gameOver == 1) {
                g.drawString("You Lost!", offsetX + (int) (500 * scale), offsetY + (int) (300 * scale));
                g.drawString("Press ENTER to play again", offsetX + (int)(410*scale), offsetY + (int)(350*scale));
                if (waiting == 1) {
                    //reset game
                    playerCount = 4;
                    turnOrder = new Player[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        turnOrder[i] = new Player(i);
                    }
                    gameOver = 0;
                    for (int i = 0; i < playerCount; i++) lifeCount[i] = 3;
                    start();
                }
            }else if (gameOver == 2) {
                g.drawString("You Won!", offsetX + (int) (500 * scale), offsetY + (int) (300 * scale));
                g.drawString("Press ENTER to play again", offsetX + (int)(410*scale), offsetY + (int)(350*scale));
                if (waiting == 1) {
                    //reset game
                    playerCount = 4;
                    turnOrder = new Player[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        turnOrder[i] = new Player(i);
                    }
                    gameOver = 0;
                    for (int i = 0; i < playerCount; i++) lifeCount[i] = 3;
                    start();
                }
            }else if (gameOver == 3) {
                g.drawString("Welcome to Chase the Ace", offsetX + (int) (430 * scale), offsetY + (int) (300 * scale));
                g.drawString("Press ENTER to begin", offsetX + (int) (450 * scale), offsetY + (int) (350 * scale));
                if(waiting == 1) {
                    //start game
                    playerCount = 4;
                    turnOrder = new Player[playerCount];
                    for (int i = 0; i < playerCount; i++) {
                        turnOrder[i] = new Player(i);
                    }
                    gameOver = 0;
                    for (int i = 0; i < playerCount; i++) lifeCount[i] = 3;
                    start();
                }
            }
        }
    }
}
