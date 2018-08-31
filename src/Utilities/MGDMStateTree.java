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
import java.util.Arrays;

public class MGDMStateTree extends StateTree
{
    public int rows, columns, winNumber, turn; // board parameters
    public int playerNum; // what number player we control
    boolean pop1, pop2; // true if the player has used their pop move
    int[][] boardMatrix; // matrix representing the board (0 = empty, 1 = player1, 2 = player2)
    StateTree parent; // parent state
    ArrayList<MGDMStateTree> children; // list of children states
    private PrintStream out = null;


    public MGDMStateTree(int r, int c, int w, int t, boolean p1, boolean p2, StateTree p, int playerNum)
    {
        super(r,c,w,t,p1,p2,p);
        rows = r;
        columns = c;
        winNumber = w;
        boardMatrix = new int[rows][columns];
        turn = t;
        pop1 = p1;
        pop2 = p2;
        this.playerNum = playerNum;
        if(p != null)
            parent = p;
        if (out == null) {
            out = out;
        }
    }

    /**
     * Calculates the evaluation of the current state
     * @return heuristic evaluation of the current state
     */
    public double evaluate(){
        //TODO check for auto win for us and them
        //TODO check for win from pop
        //keeps track of total number of 1 in a rows, 2 in a rows, etc. for both players
        //array[0] = 1 in a row, array[1] = 2 in a row..... array[N-1] = N in a row
        int[] playerInARow = new int[winNumber];
        Arrays.fill(playerInARow,0);
        int[] oppInARow = new int[winNumber];
        Arrays.fill(oppInARow,0);

        int[] tempInARow;

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                tempInARow = inARow(i, j);
                if(this.getBoardMatrix()[i][j] == playerNum){
                    //Our player
                    for(int temp:tempInARow){
                        playerInARow[temp-1]++;
                    }
                    if(playerInARow[winNumber-1] >= 1){
                        //We Win!
                        return 1.0;
                    }
                }
                else if(this.getBoardMatrix()[i][j] != playerNum && this.getBoardMatrix()[i][j] != 0){
                    //Opposing player
                    for(int temp:tempInARow){
                        oppInARow[temp-1]++;
                    }
                    if(oppInARow[winNumber-1] >= 1){
                        //We Lose!
                        return -1.0;
                    }
                }
            }
        }
        //heuristic value for player board state
        double playerSigmoid = 0;
        //heuristic value for opponent board state
        double oppSigmoid = 0;
        //ignore 1 in a row for heuristic check
        for(int i = 1; i < winNumber; i++){
            playerSigmoid = Math.pow((double)playerInARow[i], (double)i+1);
            oppSigmoid = Math.pow((double)oppInARow[i], (double)i+1);
        }
        double heuristic = playerSigmoid - oppSigmoid;
        return sigmoid(heuristic);
    }

    /**
     * Takes in a value and normalizes it between -1 and 1
     * @param x Difference between our heuristic value and opponent's heuristic value
     * @return Normalized value between -1 and 1
     */
    public double sigmoid(double x){
        //(1-e^2x)/(1+e^2x)
        return (1.0 - Math.pow(Math.E, (-2.0 * x))) / (1.0 + Math.pow(Math.E, (-2.0 * x)));
    }

    /**
     * counts how many pieces in a row there are at the current space. It starts at the space given, and
     * will check how many are in a row to the right, diagonal up and right, up, and diagonal up and left.
      * @param rowNum row number to start check at
     * @param colNum column number to start check at
     * @return An integer array of how many in a row are from this position from each direction checked
     */
    public int[] inARow(int rowNum, int colNum){
        //TODO check for auto win for us and them
        int currentPlayer = this.getBoardMatrix()[rowNum][colNum];
        //array of how many in a row from position to each direction checked
        int[] numInARow = new int[4];
        //initialize numinarow to 1 because there is 1 in a row in every direction
        Arrays.fill(numInARow, 1);
        boolean keepSearching = true;
        int count;
        for(int i = 0; i<4; i++){
            count = 1;
            keepSearching = true;
            while(keepSearching){
                if(i == 0){
                    //right
                    if((colNum + count < this.columns) && (this.getBoardMatrix()[rowNum][colNum + count] == currentPlayer)){
                        numInARow[i]++;
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else if(i == 1){
                    //right and up
                    if((colNum + count < this.columns) && (rowNum + count < this.rows) && (this.getBoardMatrix()[rowNum + count][colNum + count] == currentPlayer)){
                        numInARow[i]++;
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else if(i == 2){
                    //up
                    if((rowNum + count < this.rows) && (this.getBoardMatrix()[rowNum + count][colNum] == currentPlayer)){
                        numInARow[i]++;
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else{
                    //up and to left
                    if((colNum - count >= 0) && (rowNum + count < this.rows) && (this.getBoardMatrix()[rowNum + count][colNum - count] == currentPlayer)){
                        numInARow[i]++;
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
            }
        }

        return numInARow;
    }
}