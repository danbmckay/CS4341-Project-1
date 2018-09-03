package Players;

import Utilities.Move;
import Utilities.StateTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.System.out;


/**
 * This is an example of how to make a player.
 * This player is extremely simple and does no tree building
 * but its good to test against at first.
 *
 * @author Ethan Prihar
 *
 */
public class SimplePlayer1 extends Player {
    private final ExecutorService myService = Executors.newSingleThreadExecutor();
    private StateTree initState; //the state of the board when its the players turn
    private int maxDepth = 1; //the number of layers that the algorithm is going to dive
    private int currentDepth = 1;
    private boolean myPop = false; // does the player still have a pop
    private boolean theirPop = false; // has the opponent used there pop yet
    private StateTree lastBoard;
    private List<Move> gKids;
    private List<Double> gScores;
    private boolean justPopped = false;
    private boolean firstTurn = true;
    private Move myFinalMove;

    Callable<Object> getNextDepth;


    public SimplePlayer1(String n, int t, int l) {
        super(n, t, l);
    }

    public Move getMove(StateTree state)
    {
        {
            for(int j=0; j<state.columns; j++)
            {
                for(int i=0; i<state.rows; i++)
                {

                    if(state.getBoardMatrix()[i][j] == 0)
                    {
                        return new Move(false, j);
                    }

//				try{Thread.sleep(15000);}
//				catch(InterruptedException ex){Thread.currentThread().interrupt();}

//				if(this.turn == 1)
//					return new Move(false, 0);
//				if(this.turn == 2)
//					return new Move(false, 1);
                }

//			if((this.turn == 1 && !state.pop1) || (this.turn == 2 && !state.pop2))
//			{
//				return new Move(true, 0);
//			}

            }
            return new Move(false, 100);
        }
    }




}