package Tests;

import Utilities.MGDMStateTree;
import Utilities.Move;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class StateTreeTest {
    //State trees for players 1 and 2
    MGDMStateTree tree1;
    MGDMStateTree tree2;
    Move m0,m1,m2,m3,m4,m5,m6;

    @Before
    public void makeBasicTrees(){
        int rows = 6;
        int cols = 7;
        int winNum = 4;
        boolean pop = false;
        tree1 = new MGDMStateTree(rows,cols,winNum,1,pop,pop,null,1);
        tree2 = new MGDMStateTree(rows,cols,winNum,2,pop,pop,null,2);
    }

    @Before
    public void makeMoves(){
        m0 = new Move(false,0);
        m1 = new Move(false,1);
        m2 = new Move(false,2);
        m3 = new Move(false,3);
        m4 = new Move(false,4);
        m5 = new Move(false,5);
        m6 = new Move(false,6);
    }


    @Test
    public void testSigmoid(){
        assertEquals(tree1.sigmoid(0),0.0, 0);
    }

    @Test
    public void test4Win(){
        Move move1 = new Move(false,0);
        Move move2 = new Move(false, 1);
        for(int i = 0; i<3; i++) {
            tree1.makeMove(move1);
            tree1.makeMove(move2);
        }
        tree1.makeMove(move1);

        assertEquals(tree1.evaluate(),1.0,0);
    }

    @Test
    public void test4Lose(){
        Move move1 = new Move(false,0);
        Move move2 = new Move(false, 1);
        for(int i = 0; i<3; i++) {
            tree1.makeMove(move1);
            tree1.makeMove(move2);
        }
        tree1.makeMove(new Move(false, 3));
        tree1.makeMove(move2);

        assertEquals(tree1.evaluate(),-1.0,0);
    }

    @Test
    public void testHeuristics(){
        Random rand = new Random();
        for(int i = 0;i<20;i++){
            int r = rand.nextInt(6);
            tree1.makeMove(new Move(false, r));
        }
        tree1.evaluate();

    }
}
