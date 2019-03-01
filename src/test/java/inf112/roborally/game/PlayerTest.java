package inf112.roborally.game;

import inf112.roborally.game.objects.Player;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import static org.junit.Assert.assertEquals;

public class PlayerTest {
    private Player player;
    private Stack<ProgramCard> stack;

    @Before
    public void setup(){

        player = new Player(0,0);
        stack = ProgramCard.makeProgramCardDeck();
    }

    @Test
    public void takeDamageTest(){
        player.takeDamage();
        assertEquals(1, player.getDamage());
    }

    @Test
    public void takeDamageTest2(){
        int expected = 1;
        for(int i = 0; i < 9; i++){
            player.takeDamage();
            assertEquals(expected++, player.getDamage());
        }
    }

    @Test
    public void oneDamageOneLessCard(){
        for(int i = 0; i < 10; i++) {
            assertEquals(9 - i , player.getRegisters().getCardLimit(player));
            player.takeDamage();
        }
    }

    @Test
    public void zeroDmgDoesNotDestroy(){
        assert(!player.isDestroyed());
    }

    @Test
    public void nineDmgDoesNotDestroy(){
        for(int i = 0; i < 9; i++){
            player.takeDamage();
        }
        assert(!player.isDestroyed());
    }

    @Test
    public void tenDmgDestroys(){
        for(int i = 0; i < 10; i++){
            player.takeDamage();
        }
        assert(player.isDestroyed());
    }

    @Test
    public void fiveDmgLocksOneRegister(){
        for(int i = 0; i < 5; i++)
            player.takeDamage();
        assertEquals(true, player.getRegisters().isLocked(4));

        //the other registers should not be locked:
        for(int i = 0; i < 4; i++)
            assertEquals(false, player.getRegisters().isLocked(i));
    }

    @Test
    public void fourDmgDoesNotLocksOneRegister(){
        for(int i = 0; i < 4; i++)
            player.takeDamage();

        //the other registers should not be locked:
        for(int i = 0; i < 5; i++)
            assert(!player.getRegisters().isLocked(i));
    }

    @Test
    public void nineDmgLocksAllRegisters(){
        for(int i = 0; i < 9; i++)
            player.takeDamage();
        //all registers should be locked:
        for(int i = 0; i < 5; i++)
            assert(player.getRegisters().isLocked(i));
    }

    @Test
    public void repairResultsInZeroDmg(){
        for(int i = 0; i < 9; i++)
            player.takeDamage();
        player.repairAllDamage();
        assertEquals(0, player.getDamage());
        for(int i = 0; i < 5; i++)
            assert(!player.getRegisters().isLocked(i));
    }

    @Test
    public void noLockedRegistersReturnsAllCards(){
        for(int i = 0; i < 9; i++) {
            // Player is given nine cards:
            player.receiveCard(stack.pop());
            if (i < 5)
                // Player puts the five first in registers:
                player.getRegisters().pickCard(0);
        }

        ArrayList<ProgramCard> cardsReturned = player.returnCards();
        assertEquals(9, cardsReturned.size());

        // All registers are empty:
        for(ProgramCard register : player.getRegisters().getCardsInRegisters())
            assert(register == null);
    }

    @Test
    public void lockedCardsAreNotReturned(){
        for(int i = 0; i < 9; i++) {
            // Player is given nine cards:
            player.receiveCard(stack.pop());
            if (i < 5)
                // Player puts the five first in registers:
                player.getRegisters().pickCard(0);
        }
        // Player takes nine damage, locking all registers:
        for(int i = 0; i < 9; i++)
            player.takeDamage();

        // Only cards in hand are returned:

        ArrayList<ProgramCard> cardsReturned = player.returnCards();
        assertEquals(4, cardsReturned.size());

        // All registers contains program cards:
        for(ProgramCard register : player.getRegisters().getCardsInRegisters())
            assert(register != null);
    }

    @Test
    public void lockedCardsAreNotReturned2(){
        for(int i = 0; i < 9; i++) {
            // Player is given nine cards:
            player.receiveCard(stack.pop());
            if (i < 5)
                // Player puts the five first in registers:
                player.getRegisters().pickCard(0);
        }
        // Player takes 5 damage, locking one register:
        for(int i = 0; i < 5; i++)
            player.takeDamage();

        // Cards in hand are returned + 4 from registers:
        ArrayList<ProgramCard> cardsReturned = player.returnCards();
        assertEquals(8, cardsReturned.size());

        // The first 4 registers do not contain program cards:
        for(int i = 0; i < 4; i++)
            assert(player.getRegisters().getCardsInRegisters().get(i) == null);
        // The last register does:
        assert(player.getRegisters().getCardsInRegisters().get(4) != null);
    }

    @Test
    public void priorityTest(){
        PriorityQueue<ProgramCard> q = new PriorityQueue<>();
        // create 3 players:
        Player p1 = new Player(0,0);
        Player p2 = new Player(0,0);
        Player p3 = new Player(0,0);
        // give them five cards each:
        for(int i = 0; i < 5; i++){
            p1.receiveCard(stack.pop());
            p2.receiveCard(stack.pop());
            p3.receiveCard(stack.pop());
        }
        // have them all pick the first card and place it in a register:
        p1.getRegisters().pickCard(0);
        p2.getRegisters().pickCard(0);
        p3.getRegisters().pickCard(0);
        // retrieve cards from the first register:
        q.add(p1.getRegisters().getCardInRegister(0));
        q.add(p2.getRegisters().getCardInRegister(0));
        q.add(p3.getRegisters().getCardInRegister(0));
        // print in order of highest priority
        while(!q.isEmpty()) {
            System.out.println(q.poll());
        }
    }

    @Test
    public void repairUnlocksAllRegister(){
        for(int i = 0; i < 9; i++){
            player.takeDamage();
        }
        player.repairAllDamage();
        assertEquals(5, player.getRegisters().getUnlockedRegisters());
    }
}
