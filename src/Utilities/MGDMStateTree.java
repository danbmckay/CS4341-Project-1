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
    public boolean pop1, pop2; // true if the player has used their pop move
    int[][] boardMatrix; // matrix representing the board (0 = empty, 1 = player1, 2 = player2)
    StateTree parent; // parent state
    ArrayList<MGDMStateTree> children; // list of children states
    private PrintStream out = null;
    ArrayList<Coordinates> visited;

    public static final double MAX_VALUE = 10000.0;
    public static final double MIN_VALUE = -10000.0;


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
                        if(temp == winNumber){
                            return MAX_VALUE;
                        }
                        playerInARow[temp-1]++;
//                        if(temp == winNumber-1 && turn == playerNum){
//                            //1 away from N in a row, use tempCount to know which direction it is
//                            //0 = right, 1= right up, 2= up, 3= up left
//                            //checking for automatic win because it is our turn
//                            if(checkEasyAutoWin(i, j, tempCount)){
//                                //auto win
//                                return MAX_VALUE;
//                            }
//                        }
//                        else if(temp == winNumber-1 && turn!=playerNum){
//                            if(checkDoubleAutoWin(i, j, tempCount)){
//                                //auto win
//                                return MAX_VALUE;
//                            }
//                        }
//                        tempCount++;
                    }
                    if(playerInARow[winNumber-1] >= 1){
                        //We Win!
                        return MAX_VALUE;
                    }
                }
                else if(this.getBoardMatrix()[i][j] != playerNum && this.getBoardMatrix()[i][j] != 0){
                    //Opposing player
                    for(int temp:tempInARow){
                        if(temp == winNumber){
                            return MIN_VALUE;
                        }
                        oppInARow[temp-1]++;
//                        if(temp == winNumber-1 && turn != playerNum){
//                            //1 away from N in a row, use tempCount to know which direction it is
//                            //0 = right, 1= right up, 2= up, 3= up left
//                            //checking for automatic lose because it is their turn
//                            if(checkEasyAutoWin(i, j, tempCount)){
//                                //auto lose
//                                return MIN_VALUE;
//                            }
//                        }
//                        else if(temp == winNumber-1 && turn == playerNum){
//                            if(checkDoubleAutoWin(i, j, tempCount)){
//                                //auto lose
//                                return MIN_VALUE;
//                            }
//                        }
//                        tempCount++;
                    }
                    if(oppInARow[winNumber-1] >= 1){
                        //We Lose!
                        return MIN_VALUE;
                    }
                }
            }
        }
        //heuristic value for player board state
        double playerValue = 0;
        //heuristic value for opponent board state
        double oppValue = 0;
        //ignore 1 in a row for heuristic check
        for(int i = 1; i < winNumber; i++){
            playerValue += Math.pow((double)i+1, 2.0) * playerInARow[i];
            oppValue += Math.pow((double)i+1, 2.0) * oppInARow[i];
        }
//        if(playerValue-oppValue>0) {
//            System.out.println(playerValue - oppValue);
//        }
        return playerValue - oppValue;
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
                return true;
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
     * Checks at location i, j if there are 2 spaces open for an automatic win, e.g. 0 1 1 1 0
     * This assumes that starting from space [i,j], there is N-1 in a row
     * @param i row of starting piece
     * @param j column of starting piece
     * @param tempCount direction to check in
     * @return whether or not there is an automatic win
     */
    public boolean checkDoubleAutoWin(int i, int j, int tempCount){
        if(tempCount==0){
            if((j+winNumber-1 < columns) && this.getBoardMatrix()[i][j+winNumber-1] == 0){
                if((i>0 && this.getBoardMatrix()[i-1][j+winNumber-1] != 0) || (i == 0)){
                    if((j-1 >= 0) && this.getBoardMatrix()[i][j-1] == 0){
                        if((i>0 && this.getBoardMatrix()[i-1][j-1] != 0) || (i == 0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        else if (tempCount==1){
            if((j+winNumber-1 < columns && i+winNumber-1 < rows) && this.getBoardMatrix()[i+winNumber-1][j+winNumber-1] == 0){
                if((this.getBoardMatrix()[i+winNumber-2][j+winNumber-1] != 0)){
                    if((j-1 >= 0 && i-1 >= 0) && this.getBoardMatrix()[i-1][j-1] == 0){
                        if((i-2 >=0 && this.getBoardMatrix()[i-2][j-1] != 0) || (i-1 == 0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        else if (tempCount==3){
            if((j-winNumber+1 >= 0 && i+winNumber-1 < rows) && this.getBoardMatrix()[i+winNumber-1][j-winNumber+1] == 0){
                if((this.getBoardMatrix()[i+winNumber-2][j-winNumber+1] != 0)){
                    if((j+1 < columns && i-1 >= 0) && this.getBoardMatrix()[i-1][j+1] == 0){
                        if((i-2 >= 0 && this.getBoardMatrix()[i-2][j+1] != 0) || (i==0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int[][]  getBoardMatrix() {
        return boardMatrix;
    }

    /**
     * Takes a position on the board, the direction to check, and the number in a row that was recorded
     * for that position, and checks if it is blocked. For example, 2 2 0 is not blocked, but 2 2 2 1
     * is blocked, and is therefore not useful. Returns true if the pieces are not blocked in the given
     * direction, and false if they are blocked and therefore should not be counted.
     * @param i row of piece
     * @param j column of piece
     * @param tempCount direction to check in (0 = right, 1= right up, 2= up, 3= up left)
     * @param numInARow Number of pieces in a row for given piece
     * @return Whether or not it is blocked
     */
    public boolean checkNotBlocked(int i, int j, int tempCount, int numInARow){
        if(tempCount==0){
            if((j+numInARow-1 < columns) && this.getBoardMatrix()[i][j+numInARow-1] == 0){
                if((i>0 && this.getBoardMatrix()[i-1][j+numInARow-1] != 0) || (i == 0)){
                    if((j-1 >= 0) && this.getBoardMatrix()[i][j-1] == 0){
                        if((i>0 && this.getBoardMatrix()[i-1][j-1] != 0) || (i == 0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        else if (tempCount==1){
            if((j+numInARow-1 < columns && i+numInARow-1 < rows) && this.getBoardMatrix()[i+numInARow-1][j+numInARow-1] == 0){
                if((this.getBoardMatrix()[i+numInARow-2][j+numInARow-1] != 0)){
                    if((j-1 >= 0 && i-1 >= 0) && this.getBoardMatrix()[i-1][j-1] == 0){
                        if((i-2 >=0 && this.getBoardMatrix()[i-2][j-1] != 0) || (i-1 == 0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        else if (tempCount==2){
            if((i+numInARow-1 < rows) && this.getBoardMatrix()[i+numInARow-1][j] == 0){
                return true;
            }
        }
        else if (tempCount==3){
            if((j-numInARow+1 >= 0 && i+numInARow-1 < rows) && this.getBoardMatrix()[i+numInARow-1][j-numInARow+1] == 0){
                if((this.getBoardMatrix()[i+numInARow-2][j-numInARow+1] != 0)){
                    if((j+1 < columns && i-1 >= 0) && this.getBoardMatrix()[i-1][j+1] == 0){
                        if((i-2 >= 0 && this.getBoardMatrix()[i-2][j+1] != 0) || (i==0)){
                            //auto win
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * counts how many pieces in a row there are at the current space. It starts at the space given, and
     * will check how many are in a row to the right, diagonal up and right, up, and diagonal up and left.
      * @param rowNum row number to start check at
     * @param colNum column number to start check at
     * @return An integer array of how many in a row are from this position from each direction checked
     */
    public int[] inARow(int rowNum, int colNum){
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
                        if(numInARow[i]<winNumber)
                            numInARow[i]++;
                        visited.add(new Coordinates(rowNum,colNum+count,i));
                        count++;
                    }
                    else{
                        //make sure that it isn't blocked ex.) 1 2 2 2 1 <= this is useless
                        if(numInARow[i]!=1 && !checkNotBlocked(rowNum,colNum,i,numInARow[i])){
                            numInARow[i] = 1;
                        }
                        keepSearching = false;
                    }
                }
                else if(i == 1){
                    //right and up
                    if((colNum + count < this.columns) && (rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum+count,i)) && (this.getBoardMatrix()[rowNum + count][colNum + count] == currentPlayer)){
                        if(numInARow[i]<winNumber)
                            numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum+count,i));
                        count++;
                    }
                    else{
                        if(numInARow[i]!=1 && !checkNotBlocked(rowNum,colNum,i,numInARow[i])){
                            numInARow[i] = 1;
                        }
                        keepSearching = false;
                    }
                }
                else if(i == 2){
                    //up
                    if((rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum,i)) && (this.getBoardMatrix()[rowNum + count][colNum] == currentPlayer)){
                        if(numInARow[i]<winNumber)
                            numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum,i));
                        count++;
                    }
                    else{
                        if(numInARow[i]!=1 && !checkNotBlocked(rowNum,colNum,i,numInARow[i])){
                            numInARow[i] = 1;
                        }
                        keepSearching = false;
                    }
                }
                else{
                    //up and to left
                    if((colNum - count >= 0) && (rowNum + count < this.rows) && (!hasBeenVisited(rowNum+count,colNum-count,i)) && (this.getBoardMatrix()[rowNum + count][colNum - count] == currentPlayer)){
                        if(numInARow[i]<winNumber)
                            numInARow[i]++;
                        visited.add(new Coordinates(rowNum+count,colNum-count,i));
                        count++;
                    }
                    else{
                        if(numInARow[i]!=1 && !checkNotBlocked(rowNum,colNum,i,numInARow[i])){
                            numInARow[i] = 1;
                        }
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
            if(turn == 1)
                pop1 = true;
            if(turn == 2)
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

    public void display()
    {
        for (int i=rows-1; i>=0; i--)
        {
            for (int j = 0; j < columns; j++)
            {
                System.out.print(boardMatrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
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