package _5030;

import java.awt.*;
import java.awt.image.ImageObserver;

public class Player {
    private PlayingCard playingCard;
    private int lives, id;

    /* CONSTRUCTORS */
    public Player(int id) {
        playingCard = new PlayingCard();
        lives = 3;
        this.id = id;
    }

    public Player(PlayingCard playingCard) {
        this.playingCard = playingCard;
        this.lives = 3;
    }


    /* PLAYER TRADES CARDS */
    public int trade(PlayingCard mycard, int othercard, Player[] turnOrder, int nextPlayer, int initialP, GameSystem system, int pCount){
        PlayingCard temp;
        if(nextPlayer == initialP) {
            //Generate new card
            PlayingCard temp2 = new PlayingCard();
            int x=0;
            int y=0;
            while(y==0) {
                temp2.randomize();
                for (int i = 0; i < pCount; i++) {
                    if (temp2.equals(turnOrder[i].getCard())) {
                        x = 1;
                    }
                }
                if (x == 0) {
                    y = 1;
                }
            }
            this.setCard(temp2);
            if (nextPlayer != 1) {
                system.setMessage("Draw");
                system.setItShow(0);
            }
            return -1;
        }else if(turnOrder[nextPlayer].getCard().getRank() == 12){
            //If the next player has a king, the trade is blocked
            system.setMessage("King Blocks");
            system.setItShow(0);
            return -1;
        }else {
            //Get next player's card
            temp = turnOrder[nextPlayer].getCard();
            turnOrder[nextPlayer].setCard(mycard);
            othercard = temp.getRank();
            this.setCard(temp);
            if (nextPlayer == 0)
                system.setItMove(0);
            if (nextPlayer != 1) {
                system.setMessage("Steal");
                system.setItShow(0);
            }
            return othercard;
        }
    }


    /* PLAYER MAKES THEIR TURN */
    public int play(int othercard, Player[] turnOrder, int currentPlayer, int initialP, int isUser, int pCount, GameSystem system){
        //logic for their decision to stay or trade

        if(isUser == 1) {
            othercard = trade(playingCard, othercard, turnOrder, (currentPlayer+1)%pCount, initialP, system, pCount);
            return othercard;

            //If they weren't traded with, just base their decision on card rank
        }else if(othercard == -1){
            if(playingCard.getRank() < 6){
                //trade
                othercard = trade(playingCard, othercard, turnOrder, (currentPlayer+1)%pCount, initialP, system, pCount);
            }else{
                //stay
                if (currentPlayer != 0) {
                    system.setMessage("Stay");
                    system.setItShow(0);
                }
                othercard = -1;
            }

        //If they were traded with, consider their rank to the previous one
        }else if(othercard >= this.playingCard.getRank() && playingCard.getRank() < 6){
            //trade
            othercard = trade(playingCard, othercard, turnOrder, (currentPlayer+1)%pCount, initialP, system, pCount);
        } else {
            //stay
            if (currentPlayer != 0) {
                system.setMessage("Stay");
                system.setItShow(0);
            }
            othercard = -1;
        }
        return othercard;
    }


    /* GET / SET FUNCTIONS */
    public PlayingCard getCard() { return playingCard; }
    public void setCard(PlayingCard playingCard) { this.playingCard = playingCard; }
    public void loseLife() { this.lives--; }
    public int getLives() { return lives; }
    public int getId() { return id; }


    /* DRAW FUNCTION */
    public void draw(int x, int y, Graphics g, double scale, int offsetX, int offsetY, Color color, ImageObserver obs) {
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

        g.setColor(color);
        g.fillRect(offsetX + (int) (x * scale), offsetY + (int) (y * scale), (int) (80 * scale), (int) (80 * scale));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, (int)(16*scale)));
        g.drawString("id: " + id, offsetX + (int) ((x + 5) * scale), offsetY + (int) ((y + 30) * scale));
        g.drawString("" + lives, offsetX + (int) ((x + 5) * scale), offsetY + (int) ((y + 45) * scale));
        g.drawString(ranks[getCard().getRank()] + " of", offsetX + (int) ((x + 5) * scale), offsetY + (int) ((y + 60) * scale));
        g.drawString(suits[getCard().getSuit()], offsetX + (int) ((x + 5) * scale), offsetY + (int) ((y + 75) * scale));
    }
}
