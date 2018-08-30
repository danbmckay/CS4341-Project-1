package Players;

import Utilities.Move;
import Utilities.StateTree;
import Utilities.StateTreeEval;

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
    private int currentDepth = 1; //how deep is minimax going
    private boolean myPop = false; // does the player still have a pop
    private boolean theirPop = false; // has the opponent used there pop yet
    private StateTree lastBoard; //what did the last board look like to see if the popped
    private List<Move> gKids; //list of immediate moves the player can make
    private List<Double> gScores; //final list of scores to choose from to make a move
    private boolean justPopped = false; //did we pop last turn figure out if they popped
    private boolean firstTurn = true; //is this our first turn
    private Move myFinalMove; //what is the move we have waiting to do
    private int mTurn = 0; //value for our turn
    private int tTurn = 0; //value for their turn

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
        else {
            popUsed(localInitState);
            currentDepth = 1;
        }
        Future<Object> future = null;

        gKids = getPossibleBoards(initState, true);
        gScores = new ArrayList<>();

        try {
            future = myService.submit(getNextDepth);
            finalMove = (Move) future.get(myTimeLimit, TimeUnit.SECONDS);
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
        while(true) {
            Move finalMove;
            for (int i = 0; i < gKids.size(); i++) {
                StateTreeEval tempTree = new StateTreeEval(initState.rows, initState.columns, initState.winNumber, 1, myPop, theirPop, initState);
                tempTree.makeMove(gKids.get(i));
                if (maxDepth > 1) {
                    gScores.add(miniMax(tempTree,currentDepth));
                } else {
                    gScores.add(tempTree.evaluate());
                }
            }
            finalMove = gKids.get(gScores.indexOf(Collections.min(gScores)));
            myFinalMove = finalMove;
            currentDepth += 1;
        }
    }


    private Double miniMax(StateTree currentState, int depth){
        List<Move> kids;
        //is the player me
        if(depth%2 == 1) {
            kids = getPossibleBoards(currentState, true);
            //the bottom layer of the search call the heuristic function
            if(depth==currentDepth){
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    StateTreeEval tempTree = new StateTreeEval(currentState.rows, currentState.columns, currentState.winNumber, mTurn, myPop, theirPop, currentState);
                    tempTree.makeMove(kids.get(i));
                    scores.add(tempTree.evaluate());
                }
                return Collections.max(scores);
            }
            //any other layer
            else{
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    StateTreeEval tempTree = new StateTreeEval(currentState.rows, currentState.columns, currentState.winNumber, mTurn, myPop, theirPop, currentState);
                    tempTree.makeMove(kids.get(i));
                    scores.add(miniMax(tempTree, depth+1));
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
                    StateTreeEval tempTree = new StateTreeEval(currentState.rows, currentState.columns, currentState.winNumber, tTurn, myPop, theirPop, currentState);
                    tempTree.makeMove(kids.get(i));
                    scores.add(tempTree.evaluate());
                }
                return Collections.min(scores);
            }
            //any other layer
            else{
                List<Double> scores = new ArrayList<>();
                for(int i = 0 ; i < kids.size(); i++) {
                    StateTreeEval tempTree = new StateTreeEval(currentState.rows, currentState.columns, currentState.winNumber, tTurn, myPop, theirPop, currentState);
                    tempTree.makeMove(kids.get(i));
                    scores.add(miniMax(tempTree,depth+1));
                }
                return Collections.min(scores);
            }

        }
    }

    private List<Move> getPossibleBoards(StateTree parentState, boolean isMe){
        List<Move> kids = new ArrayList<>(); //list that will be returned later
        //whos turn is it?
        if(isMe) {
            //do i still have my pop
            if (!myPop) {
                //make all possible states including possible pops
                for (int i = 0; i < parentState.columns*2; i++) {
                    if(i < parentState.columns) {
                        Move tempMove = new Move(false, i);
                        if(parentState.validMove(tempMove)) {
                            kids.add(tempMove);
                        }
                    }
                    else{
                        Move tempMove = new Move(true,i-7);
                        if(parentState.validMove(tempMove)) {
                            kids.add(tempMove);
                        }
                    }
                }
            }
            //make all possible moves when we no longer have a pop
            else{
                for (int i = 0; i < parentState.columns; i++) {
                    Move tempMove = new Move(false, i);
                    if(parentState.validMove(tempMove)) {
                        kids.add(tempMove);
                    }
                }
            }
        }
        // its their turn
        else{
            // do they have their pop
            if(!theirPop) {
                //make all possible states if they have their pop
                for (int i = 0; i < parentState.columns*2; i++) {
                    if(i < parentState.columns) {
                        Move tempMove = new Move(false, i);
                        if(parentState.validMove(tempMove)) {
                            kids.add(tempMove);
                        }
                    }
                    else{
                        Move tempMove = new Move(true,i-7);
                        if(parentState.validMove(tempMove)) {
                            kids.add(tempMove);
                        }
                    }
                }
            }
            //make all possible moves with no pops
            else{
                for (int i = 0; i < parentState.columns; i++) {
                    Move tempMove = new Move(false, i);
                    if(parentState.validMove(tempMove)) {
                        kids.add(tempMove);
                    }
                }
            }
        }

        return kids;
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





}
