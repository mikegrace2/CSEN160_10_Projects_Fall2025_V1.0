package _5030;

public class UserInput extends Thread {

    private GameSystem system;

    public UserInput(GameSystem system) {
        this.system = system;
    }

    @Override
    public void run() {
        Player turnOrder[] = system.getTurnOrder();
        int traded = -1;
        int playerCount = system.getPlayerCount();
        int turnStart = (int) (Math.random() * playerCount);
        int turn = turnStart;


        system.setPrevCard(turnOrder[0].getCard());
        system.setItMove(0);

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        system.shuffle();

        for (int i = 0; i < playerCount; i++) {

            if(turn % playerCount == 0){
                //Generate the buttons
                //Wait for the player to click
                //if trade, call trade
                //if stay, increment turn

                system.setTurn(turn);
                system.setWaiting(0);
                while (system.getWaiting() == 0) {
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                system.setPrevCard(turnOrder[0].getCard());
                if (system.getWaiting() == 1) {
                    traded = turnOrder[turn].play(traded, turnOrder, turn, turnStart, 1, system.getPlayerCount(), system);
                    if (turnOrder[(turn+1)%playerCount].getCard().getRank() != 12)
                        system.setItMove(0);
                }
                else if (system.getWaiting() == 2) {
                    traded = -1;
                }
                turn = (turn + 1) % playerCount;


            }else {
                system.setTurn(turn);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                system.setPrevCard(turnOrder[0].getCard());
                traded = turnOrder[turn].play(traded, turnOrder, turn, turnStart, 0, system.getPlayerCount(), system);
                turn = (turn + 1) % playerCount;
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        int min = 13;
        for (int i = 0; i < playerCount; i++) {
            if (turnOrder[i].getCard().getRank() < min)  {
                min = turnOrder[i].getCard().getRank();
            }
        }
        system.setLowestRank(min);
        try {
            sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        system.setIterator(0);

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //system.setItMove(0);
        //system.setPrevCard(turnOrder[0].getCard());
        system.endRound();
    }
}
