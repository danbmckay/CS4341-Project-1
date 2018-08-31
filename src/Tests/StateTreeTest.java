package Tests;

import Utilities.MGDMStateTree;
import Utilities.Move;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StateTreeTest {
    //State trees for players 1 and 2
    MGDMStateTree tree1;
    MGDMStateTree tree2;

    @Before
    public void makeBasicTrees(){
        int rows = 6;
        int cols = 7;
        int winNum = 4;
        boolean pop = false;
        tree1 = new MGDMStateTree(rows,cols,winNum,1,pop,pop,null,1);
        tree2 = new MGDMStateTree(rows,cols,winNum,2,pop,pop,null,2);
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
}
