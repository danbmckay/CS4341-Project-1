package Players;

import Utilities.MGDMStateTree;
import Utilities.Move;
import Utilities.StateTree;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.System.out;

public class MGDMPlayer extends Player {
    private final ExecutorService myService = Executors.newSingleThreadExecutor();
    private StateTree initState; //the state of the board when its the players turn
    private int maxDepth = 1; //the number of layers that the algorithm is going to dive
    private int currentDepth = 1; //how deep is minimax going for a loop of iterative deepening
    private boolean myPop = true; // does the player still have a pop
    private boolean theirPop = true; // has the opponent used there pop yet
    private StateTree lastBoard; //what did the last board look like to see if the popped
    private List<Move> gKids; //list of immediate moves the player can make
    private List<Double> gScores; //final list of scores to choose from to make a move
    private boolean justPopped = false; //did we pop last turn figure out if they popped
    private boolean firstTurn = true; //is this our first turn
    private Move myFinalMove; //what is the move we have waiting to do
    private int mTurn = 0; //value for our turn
    private int tTurn = 0; //value for their turn
    private static double INFINITY = 10000.0;
    private static double NINFINITY = -10000.0;



    Callable<Object> getNextDepth;


    public MGDMPlayer(String n, int t, int l) {
        super(n, t, l);
        //create a seperate thread to run so you can do iterative deepening
        getNextDepth = new Callable<Object>() {
            public Object call() {
                return startMiniMax();
            }
        };
    }

    //function to return the move
    public Move getMove(StateTree state)
    {
        return IDFS(state);
    }

    //starts the iterative deepening search
    private Move IDFS(StateTree localInitState){

        //make the initial state global
        initState = localInitState;
        //dummy as a place holder move
        Move finalMove;
        //how much buffer time (currently need to be in full seconds)
        long bufferTime = (long)1;
        long myTimeLimit = timeLimit - bufferTime;
        //actions to take on the first turn, initiate the last board to not get null
        //and determines what turn we are as a player
        if(firstTurn){
            lastBoard = localInitState;
            if(getTurn(localInitState)){
                mTurn = 1;
                tTurn = 2;

            }
            else{
                mTurn = 2;
                tTurn = 1;
            }
        }
        //determines if pops have been used and resets the current depth of iterative deepening search
        else {
            popUsed(localInitState);
            currentDepth = 1;
        }
        //creates future thread
        Future<Object> future = null;

        //gets the initial moves and scores for each turn of play
        gKids = getPossibleBoards(initState, true);
        gScores = new ArrayList<>();

        //launch the thread that will timeout appropriately
        try {
            future = myService.submit(getNextDepth);
            finalMove = (Move) future.get(myTimeLimit, TimeUnit.SECONDS);
            return finalMove;
        } catch (TimeoutException e) {
            future.cancel(true);
            maxDepth = 1;
            if(myFinalMove.getPop()){
                myPop = false;
            }
            return myFinalMove;
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace(out);
        } finally {
            maxDepth++;
        }

        return myFinalMove;

    }

    private Move startMiniMax(){
        double alpha = NINFINITY;
        double beta = INFINITY;
        double tempScore = 0;
        while(true) {
            Move finalMove;
            for (int i = 0; i < gKids.size(); i++) {
                MGDMStateTree tempTree = new MGDMStateTree(initState.rows, initState.columns, initState.winNumber, mTurn, myPop, theirPop, initState, mTurn);
                tempTree.testMove(gKids.get(i));
                if (currentDepth > 1) {
                    tempScore =miniMax(tempTree,currentDepth, alpha, beta);
                } else {
                    tempScore = tempTree.evaluate();
                }
                gScores.add(tempScore);
                if(tempScore == INFINITY){
                    break;
                }
                if(tempScore > beta){
                    break;
                }
                else{
                    if(tempScore > alpha){
                        alpha = tempScore;
                    }
                }

            }
            finalMove = gKids.get(gScores.indexOf(Collections.max(gScores)));
            //alpha = INFINITY;
            myFinalMove = finalMove;
            if(Collections.max(gScores) == INFINITY){
                break;
            }

            gScores = new ArrayList<>();



            currentDepth += 1;

        }
        return myFinalMove;

    }


    private Double miniMax(MGDMStateTree currentState, int depth, double alpha, double beta){
        List<Move> kids;
        double mAlpha = NINFINITY;
        double mBeta = INFINITY;
        double tempScore = 0.0;
        //is the player me
        if(depth%2 == 1) {
            kids = getPossibleBoards(currentState, true);
            //the bottom layer of the search call the heuristic function
            if(depth==currentDepth){
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    MGDMStateTree tempTree = new MGDMStateTree(currentState.rows, currentState.columns, currentState.winNumber, mTurn, myPop, theirPop, currentState, mTurn);
                    tempTree.testMove(kids.get(i));
                    tempScore = tempTree.evaluate();
                    scores.add(tempScore);
                    if(scores.get(i) == INFINITY){
                        return INFINITY;
                    }
                    if(tempScore > beta){
                        return tempScore;
                    }
                }
                return Collections.max(scores);
            }
            //any other layer
            else{
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    MGDMStateTree tempTree = new MGDMStateTree(currentState.rows, currentState.columns, currentState.winNumber, mTurn, myPop, theirPop, currentState, mTurn);
                    tempTree.testMove(kids.get(i));
                    tempScore = miniMax(tempTree, depth+1, mAlpha, mBeta);
                    scores.add(tempScore);
                    if(scores.get(i) == INFINITY){
                        break;
                    }
                    if(tempScore > beta){
                        break;
                    }
                    else{
                        if(tempScore > mAlpha){
                            mAlpha = tempScore;
                        }
                    }
                }
                return Collections.max(scores);
            }

        }
        //is the opponent playing
        else{
            kids = getPossibleBoards(currentState, false);
            //the bottom layer of the search call the heuristic function
            if(depth==currentDepth){
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    MGDMStateTree tempTree = new MGDMStateTree(currentState.rows, currentState.columns, currentState.winNumber, tTurn, myPop, theirPop, currentState, mTurn);
                    tempTree.testMove(kids.get(i));
                    tempScore = tempTree.evaluate();
                    scores.add(tempScore);
                    if(scores.get(i) == NINFINITY){
                        return NINFINITY;
                    }
                    if(tempScore < alpha){
                        return tempScore;
                    }
                }
                return Collections.min(scores);
            }
            //any other layer
            else{
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    MGDMStateTree tempTree = new MGDMStateTree(currentState.rows, currentState.columns, currentState.winNumber, tTurn, myPop, theirPop, currentState, mTurn);
                    tempTree.testMove(kids.get(i));
                    double testScore = tempTree.evaluate();
                    if(testScore == NINFINITY){
                        return NINFINITY;
                    }
                    tempScore = miniMax(tempTree, depth+1, mAlpha, mBeta);
                    scores.add(tempScore);
                    if(scores.get(i) == NINFINITY){
                        return NINFINITY;
                    }
                    if(tempScore < alpha){
                        return tempScore;
                    }
                    else{
                        if(tempScore < mBeta){
                            mBeta = tempScore;
                        }
                    }
                }
                return Collections.min(scores);
            }

        }
    }

    private List<Move> getPossibleBoards(StateTree parentState, boolean isMe){
        List<Move> kids = new ArrayList<>(); //list that will be returned later
        //my turn
        boolean isPop = isPop(isMe);
        if(isPop){
            //make all possible states including possible pops
            for (int i = 0; i < parentState.columns * 2; i++) {
                if (i < parentState.columns) {
                    Move tempMove = new Move(false, i);
                    if (isValidMove(parentState, tempMove)) {
                        kids.add(tempMove);
                    }
                } else {
                    Move tempMove = new Move(true, i - parentState.columns);
                    if (isValidMove(parentState, tempMove)) {
                        kids.add(tempMove);
                    }
                }
            }
        }
        else{
            for (int i = 0; i < parentState.columns; i++) {
                Move tempMove = new Move(false, i);
                if (isValidMove(parentState, tempMove)) {
                    kids.add(tempMove);
                }
            }
        }


        return kids;
    }

    private boolean isPop(boolean isMe){
        //my turn
        if(isMe){
            //i'm player one
            if(myPop){
                return true;
            }
        }
        else{
            if(theirPop){
                return true;
            }
        }
        return false;
    }


    private void popUsed(StateTree currentState){
        int numPopped = 0;
        for(int i = 0; i < currentState.columns; i++){
            if(!(currentState.getBoardMatrix()[0][i] == lastBoard.getBoardMatrix()[0][i])){
                numPopped++;
            }
            if(justPopped){
                justPopped = false;
                if(numPopped == 2){
                    theirPop = false;
                }
            }
            else{
                if(numPopped == 1){
                    theirPop = false;
                }
            }

        }
    }

    private boolean getTurn(StateTree aState){
        for(int i = 0; i < aState.columns; i++){
            if(!(aState.getBoardMatrix()[0][i] == 0)) {
                return false; //we are player two
            }
        }
        return true;
    }

    public boolean isValidMove(StateTree aState, Move cMove){
        if(cMove.getColumn() >= aState.columns || cMove.getColumn() < 0){
            return false;
        }
        if(!cMove.getPop() && aState.getBoardMatrix()[aState.rows-1][cMove.getColumn()] != 0){
            return false;
        }
        if(cMove.getPop())
        {
            if(aState.getBoardMatrix()[0][cMove.getColumn()] != turn)
            {
                return false;
            }
            if((turn == mTurn && myPop) || (turn == tTurn && theirPop))
            {
                return false;
            }
        }
        return true;
    }
    /*
    public boolean validMove(MGDMStateTree aState, Move move)
    {
        if(move.column >= columns || move.column < 0)
        {
            out.println("That column doesn't exist.");
            return false;
        }
        if(!move.pop && aState.boardMatrix[rows-1][move.column] != 0)
        {
            out.println("That column is full.");
            return false;
        }
        if(move.pop)
        {
            if(boardMatrix[0][move.column] != turn)
            {
                out.println("You can't pop a piece that isn't your own.");
                return false;
            }
            if((turn == 1 && pop1) || (turn == 2 && pop2))
            {
                out.println("You can't pop a piece twice in a game.");
                return false;
            }
        }
        return true;
    }

*/



}
