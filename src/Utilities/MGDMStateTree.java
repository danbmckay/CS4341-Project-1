package Utilities;
/**
 * This class extends StateTree, and is used to evaluate the given state
 * of the board.
 *
 * @author Daniel McKay, Manuel Gonsalves
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
    ArrayList<Coordinates> visited;


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
        if(p != null) {
            parent = p;
            for(int i = 0; i < rows; i++){
                for(int j = 0; j <columns; j++){
                    boardMatrix[i][j] = p.getBoardMatrix()[i][j];
                }
            }
        }
        if (out == null) {
            out = out;
        }

        visited = new ArrayList<>();
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
        int tempCount = 0;

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < columns; j++){
                tempInARow = inARow(i, j);
                tempCount = 0;
                if(this.getBoardMatrix()[i][j] == playerNum){
                    //Our player
                    for(int temp:tempInARow){
                        playerInARow[temp-1]++;
                        if(temp == winNumber-1 && super.turn == playerNum){
                            //1 away from N in a row, use tempCount to know which direction it is
                            //0 = right, 1= right up, 2= up, 3= up left
                            //checking for automatic win because it is our turn
                            if(checkEasyAutoWin(i, j, tempCount)){
                                //auto win
                                return 1.0;
                            }
                        }
                        //TODO check same stuff as above but if its not our turn. check for auto win that way, aka 0 1 1 1 0
                        tempCount++;
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
                        if(temp == winNumber-1 && super.turn != playerNum){
                            //1 away from N in a row, use tempCount to know which direction it is
                            //0 = right, 1= right up, 2= up, 3= up left
                            //checking for automatic lose because it is their turn
                            if(checkEasyAutoWin(i, j, tempCount)){
                                //auto lose
                                return -1.0;
                            }
                        }
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
            playerSigmoid += Math.pow((double)playerInARow[i], (double)i);
            oppSigmoid += Math.pow((double)oppInARow[i], (double)i);
        }
        double heuristic = playerSigmoid - oppSigmoid;
        return sigmoid(heuristic);
    }

    /**
     * Checks at location i, j if there is 1 space open for an automatic win, e.g. 1 1 1 0
     * This assumes that starting from space [i,j], there is N-1 in a row
     * @param i row of starting piece
     * @param j column of starting piece
     * @param tempCount direction to check in
     * @return whether or not there is an automatic win
     */
    public boolean checkEasyAutoWin(int i, int j, int tempCount){
        if(tempCount==0){
            if((j+winNumber-1 < columns) && this.getBoardMatrix()[i][j+winNumber-1] == 0){
                if((i>0 && this.getBoardMatrix()[i-1][j+winNumber-1] != 0) || (i == 0)){
                    //auto win
                    return true;
                }
            }
            else if((j-1 >= 0) && this.getBoardMatrix()[i][j-1] == 0){
                if((i>0 && this.getBoardMatrix()[i-1][j-1] != 0) || (i == 0)){
                    //auto win
                    return true;
                }
            }
        }
        else if (tempCount==1){
            if((j+winNumber-1 < columns && i+winNumber-1 < rows) && this.getBoardMatrix()[i+winNumber-1][j+winNumber-1] == 0){
                if((this.getBoardMatrix()[i+winNumber-2][j+winNumber-1] != 0)){
                    //auto win
                    return true;
                }
            }
            else if((j-1 >= 0 && i-1 >= 0) && this.getBoardMatrix()[i-1][j-1] == 0){
                if((i-2 >=0 && this.getBoardMatrix()[i-2][j-1] != 0) || (i-1 == 0)){
                    //auto win
                    return true;
                }
            }
        }
        else if (tempCount==2){
            if((i+winNumber-1 < rows) && this.getBoardMatrix()[i+winNumber-1][j] == 0){
                if((i>0 && this.getBoardMatrix()[i-1][j+winNumber-1] != 0)){
                    //auto win
                    return true;
                }
            }
        }
        else if (tempCount==3){
            if((j-winNumber+1 >= 0 && i+winNumber-1 < rows) && this.getBoardMatrix()[i+winNumber-1][j-winNumber+1] == 0){
                if((this.getBoardMatrix()[i+winNumber-2][j-winNumber+1] != 0)){
                    //auto win
                    return true;
                }
            }
            else if((j+1 < columns && i-1 >= 0) && this.getBoardMatrix()[i-1][j+1] == 0){
                if((i-2 >= 0 && this.getBoardMatrix()[i-2][j+1] != 0) || (i==0)){
                    //auto win
                    return true;
                }
            }
        }
        return false;
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
                    if((colNum + count < this.columns) && (!hasBeenVisited(rowNum,colNum+count,i)) && (this.getBoardMatrix()[rowNum][colNum + count] == currentPlayer)){
                        numInARow[i]++;
                        visited.add(new Coordinates(rowNum,colNum+count,i));
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else if(i == 1){
                    //right and up
                    if((colNum + count < this.columns) && (rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum+count,i)) && (this.getBoardMatrix()[rowNum + count][colNum + count] == currentPlayer)){
                        numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum+count,i));
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else if(i == 2){
                    //up
                    if((rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum,i)) && (this.getBoardMatrix()[rowNum + count][colNum] == currentPlayer)){
                        numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum,i));
                        count++;
                    }
                    else{
                        keepSearching = false;
                    }
                }
                else{
                    //up and to left
                    if((colNum - count >= 0) && (rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum-count,i)) && (this.getBoardMatrix()[rowNum + count][colNum - count] == currentPlayer)){
                        numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum-count,i));
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

    /**
     * When checking for how many in a row each player has, there is the possibility of double counting, such as if it is 3 in a row,
     * it will double count it as 1 3 in a row and 1 2 in a row. This function returns whether or not this piece has been checked yet
     * in the current direction to eliminate this duplication.
     * @param rowNum Row number of piece to be checked
     * @param colNum Column number of piece to be checked
     * @param dir Direction that piece is being checked in
     * @return Whether or not piece has been visited yet
     */
    public boolean hasBeenVisited(int rowNum, int colNum, int dir){
        for(Coordinates c:visited){
            if(c.row == rowNum && c.column == colNum && c.direction == dir){
                return true;
            }
        }
        return false;
    }

    public void testMove(Move move)
    {
        if(move.pop)
        {
            if(super.turn == 1)
                pop1 = true;
            if(super.turn == 2)
                pop2 = true;
            for(int i=0; i<rows-1; i++)
            {
                this.boardMatrix[i][move.column] = boardMatrix[i+1][move.column];
            }
            this.boardMatrix[rows-1][move.column] = 0;
            turn = Math.abs(turn-3);
            return;
        }
        else
        {
            for(int i=0; i<rows; i++)
            {
                if(this.boardMatrix[i][move.column] == 0)
                {
                    this.boardMatrix[i][move.column] = turn;
                    turn = Math.abs(turn-3);
                    return;
                }
            }
        }
    }
}

/**
 * Coordinate class used to store coordinates visited so far on the board
 */
class Coordinates{
    public int row, column, direction;
    public Coordinates(int r, int c, int d){
        row = r;
        column = c;
        direction = d;
    }
}