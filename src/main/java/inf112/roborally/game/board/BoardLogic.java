package inf112.roborally.game.board;

import com.badlogic.gdx.Gdx;
import inf112.roborally.game.RoboRallyGame;
import inf112.roborally.game.enums.GameState;
import inf112.roborally.game.enums.PlayerState;
import inf112.roborally.game.tools.AiRobo;
import inf112.roborally.game.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static inf112.roborally.game.enums.GameState.*;
import static java.util.Collections.shuffle;

@SuppressWarnings("Duplicates")
public class BoardLogic {

    protected int phase;
    protected GameState state;

    protected List<Player> players;
    protected Stack<ProgramCard> returnedProgramCards;
    protected Stack<ProgramCard> stackOfProgramCards;
    private ArrayList<Player> aiBots;

    public BoardLogic(List<Player> players) {
        this.players = players;
        aiBots = new ArrayList<>();
        if (players.get(0).isDebuggingActive() || ((RoboRallyGame) Gdx.app.getApplicationListener()).AIvsAI)
            aiBots.add(players.get(0));
        for (int i = 1; i < players.size(); i++)
            aiBots.add(players.get(i));

        phase = 0;
        state = BETWEEN_ROUNDS;
        stackOfProgramCards = ProgramCard.makeProgramCardDeck();
        returnedProgramCards = new Stack<>();
    }

    public void executeLogic() {
        switch (state) {
            case BETWEEN_ROUNDS:
                doBeforeRound();
                break;
            case PICKING_CARDS:
                AiRobo.makeDecisionsForRobos(aiBots);
                checkIfReady();
                break;
            case ROUND:
                doPhase();
                break;
            case BOARD_MOVES:
                boardMoves();
                break;
            case GAME_OVER:
                endGame();
                break;
        }
    }

    protected void doBeforeRound() {
        System.out.println("Set up before round");
        cleanBoard();
        respawnRobots();
        powerUpRobots();
        powerDownRobots();

        for (Player player : players) {
            if (!player.isPoweredDown()) {
                retrieveCardsFromPlayer(player);
            }
            if (!player.outOfLives() && player.isOperational()) {
                giveCardsToPlayer(player);
            }
        }
        System.out.println("Players choosing cards. Players alive: " + players.size());
        state = PICKING_CARDS;
    }

    private void respawnRobots() {
        for (Player player : players) {
            if (player.isDestroyed()) {
                if (player.outOfLives()) player.setPlayerState(PlayerState.GAME_OVER);
                else player.respawn();
            }
        }
    }

    public void removeDeadRobots() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if(players.size() == 1){
                endGame();
                return;
            }
            else if (player.getPlayerState() == PlayerState.GAME_OVER) {
                System.out.println(player.getName() + " was removed.");
                players.remove(player);
                aiBots.remove(player);
            }
        }
    }

    private void powerUpRobots() {
        for (Player player : players) {
            player.powerUp();
        }
    }

    protected void powerDownRobots() {
        for (Player player : players) {
            player.powerDown();
        }
    }

    private boolean allPlayersReady() {
        for (Player player : players) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    public void checkIfReady() {
        if (allPlayersReady()) {
            for (Player player : players) {
                if (player.getPlayerState() == PlayerState.READY) //true if submit button is pressed
                    player.setPlayerState(PlayerState.OPERATIONAL);
            }
            state = ROUND;
        }
    }

    protected void doPhase() {
        if (phase >= 5) {
            phase = 0;
            state = BETWEEN_ROUNDS;
            System.out.println("Round over");
            return;
        }

        System.out.println("Executing phase " + phase);
        sortPlayersByPriority();
        executeCards();
        checkIfAPlayerHasWon();
        phase++;
        if(!players.isEmpty() && players.get(0).isDebuggingActive()) return;

        if (((RoboRallyGame) Gdx.app.getApplicationListener()).AIvsAI)
            sleepThread(100);
        else
            sleepThread(500);
    }

    private void sleepThread(int millis) {
        if (players.size() > 0 && !players.get(0).isDebuggingActive()) {
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sortPlayersByPriority() {
        for (Player player : players) {
            player.setPhase(phase);
        }
        try {
            Collections.sort(players);
        }
        catch (NullPointerException e) {
            throw new NullPointerException("AIRobots: " + aiBots.size() + "\n Players: " + players.size());
        }
    }

    private void executeCards() {
        for (Player player : players) {
            player.getRegisters().executeCard(phase);
            updatePlayers();
        }
    }

    protected void boardMoves() {
        state = ROUND;
    }


    protected void cleanBoard() {
        System.out.println("Cleaning board");
    }

    protected Player checkIfAPlayerHasWon() {
        if (players.size() == 1) {
            System.out.printf("%s just won the game by outliving their opponents!!%n", players.get(0).getName());
            state = GAME_OVER;
            return players.get(0);
        }

        for (Player player : players) {
            if (player.hasWon()) {
                System.out.printf("%s just won the game by collecting all the flags!!%n", player.getName());
                state = GAME_OVER;
                return player;
            }
        }
        state = BOARD_MOVES;
        return null;
    }

    protected void endGame() {
        System.out.println("Game Over! "
                + checkIfAPlayerHasWon().getName()
                + " won the game.");
    }


    protected void updatePlayers() {
        for (Player player : players) {
            player.updateSprite();
        }
    }

    /**
     * Gives the player as many cards as allowed from the stack of program cards.*
     *
     * @param player which player to give program cards to.
     */
    private void giveCardsToPlayer(Player player) {
        for (int i = 0; i < player.getCardLimit(); i++) {
            if (stackOfProgramCards.isEmpty()) { // in case the game drags on and we run out of cards - Morten
                reshuffleDeck();
            }
            player.getHand().receiveCard(stackOfProgramCards.pop());
        }
    }

    private void retrieveCardsFromPlayer(Player player) {
        ArrayList<ProgramCard> playerCards = player.returnCards();
        while (!playerCards.isEmpty()) {
            returnedProgramCards.push(playerCards.remove(0));
        }
    }

    /**
     * If the stack of cards run out of cards we need to reshuffle all the returned cards and deal them out instead
     * Move this to ProgramCard maybe..
     */
    private void reshuffleDeck() {
        while (!returnedProgramCards.empty())
            stackOfProgramCards.push(returnedProgramCards.pop());
        shuffle(stackOfProgramCards);
    }

    public GameState getState() {
        return state;
    }
}
