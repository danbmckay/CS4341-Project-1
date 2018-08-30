package Utilities;
/**
 * This class represents the state of a game at any time.
 * This class contains information on the board size and configuration
 * as well as whose turn it is. This class also has the ability
 * to be given a parent and children to the current state. This will help with building the tree
 * that you will min-max on. You don't have to use any of the variables given
 * if you don't want to.
 *
 * You might want to make a whole different representation for states which
 * ok, this is just something to get you started but make sure your player
 * can still interpret the RefereeBoard objects its getting and is still
 * able to return moves.
 *
 * @author Ethan Prihar
 */

import java.io.PrintStream;
import java.util.ArrayList;

public class StateTreeEval extends StateTree
{
    public int rows, columns, winNumber, turn; // board parameters
    boolean pop1, pop2; // true if the player has used their pop move
    int[][] boardMatrix; // matrix representing the board (0 = empty, 1 = player1, 2 = player2)
    StateTree parent; // parent state
    ArrayList<StateTreeEval> children; // list of children states
    private PrintStream out = null;


    public StateTreeEval(int r, int c, int w, int t, boolean p1, boolean p2, StateTree p)
    {
        super(r,c,w,t,p1,p2,p);
        rows = r;
        columns = c;
        winNumber = w;
        boardMatrix = new int[rows][columns];
        turn = t;
        pop1 = p1;
        pop2 = p2;
        if(p != null)
            parent = p;
        if (out == null) {
            out = out;
        }
    }

    /**
     * Calculates the evaluation of the current state
     * @return
     */
    public double evaluate(){
        return 0.0;
    }

    // Checks if the given move is valid in the current state
    public boolean validMove(Move move)
    {
        if(move.column >= columns || move.column < 0)
        {
            return false;
        }
        if(!move.pop && boardMatrix[rows-1][move.column] != 0)
        {
            return false;
        }
        if(move.pop)
        {
            if(boardMatrix[0][move.column] != turn)
            {
                return false;
            }
            if((turn == 1 && pop1) || (turn == 2 && pop2))
            {
                return false;
            }
        }
        return true;
    }

    // Makes the given move on the board and updates the state
    public void makeMove(Move move)
    {
        if(move.pop)
        {
            if(turn == 1)
                pop1 = true;
            if(turn == 2)
                pop2 = true;
            for(int i=0; i<rows-1; i++)
            {
                boardMatrix[i][move.column] = boardMatrix[i+1][move.column];
            }
            boardMatrix[rows-1][move.column] = 0;
            turn = Math.abs(turn-3);
            return;
        }
        else
        {
            for(int i=0; i<rows; i++)
            {
                if(boardMatrix[i][move.column] == 0)
                {
                    boardMatrix[i][move.column] = turn;
                    turn = Math.abs(turn-3);
                    return;
                }
            }
        }
    }

    // Prints the board
    public void display()
    {
        for (int i=rows-1; i>=0; i--)
        {
            for (int j = 0; j < columns; j++)
            {
                out.print(boardMatrix[i][j] + " ");
            }
            out.println();
        }
        out.println();
    }

    public int[][]  getBoardMatrix() {
        return boardMatrix;
    }

    public void setOut(PrintStream printStream) {
        out = printStream;
    }
}