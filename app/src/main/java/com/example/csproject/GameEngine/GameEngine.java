package com.example.csproject.GameEngine;

import static com.example.csproject.CommonUtilities.DatabaseUtilities.database;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.getUserValues;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.mAuth;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.csproject.Activities.GameActivity;
import com.example.csproject.Dialogs.GameEndDialog;
import com.example.csproject.Troops.Mage;
import com.example.csproject.Troops.Troop;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameEngine {
    private Visualizer visualizer;
    private BoardStateManager boardStateManager;
    public RoomManager roomManager;
    public UserInputManager userInputManager;
    private boolean turn;
    public GameEngine(String roomRef, LinearLayout gameBoard, GameActivity gameActivity, TextView turnIndicator)
    {
        Troop[] startingBoard = gameConstructor(gameActivity);
        this.visualizer = new Visualizer(gameBoard, gameActivity, turnIndicator, startingBoard);
        this.boardStateManager = new BoardStateManager(startingBoard);
        this.roomManager = new RoomManager(roomRef, onGameRoomRead);
        this.userInputManager = new UserInputManager(visualizer.getTiles(), gameActivity);
        this.turn = roomManager.isHost;

        visualizer.turnIndicator.setVisibility(turn? View.VISIBLE:View.INVISIBLE);
    }
    private Troop[] gameConstructor(GameActivity gameActivity)
    {

        return new Troop[]{
                //my troops
                new Troop("archer", "ma1", true, new int[]{5, 1},gameActivity),
                new Troop("archer", "ma2", true, new int[]{5, 3},gameActivity),
                new Troop("swordsman", "ms1", true, new int[]{4, 1},gameActivity),
                new Troop("swordsman", "ms2", true, new int[]{4, 3},gameActivity),
                new Mage("mm1", true, new int[]{5, 2},gameActivity),
                new Troop("knight", "mk1", true, new int[]{4, 2},gameActivity),
                //enemy troops
                new Troop("archer", "ea1", false, new int[]{0, 4},gameActivity),
                new Troop("archer", "ea2", false, new int[]{0, 2},gameActivity),
                new Troop("swordsman", "es1", false, new int[]{1, 4},gameActivity),
                new Troop("swordsman", "es2", false, new int[]{1, 2},gameActivity),
                new Mage("em1", false, new int[]{0, 3},gameActivity),
                new Troop("knight", "ek1", false, new int[]{1, 3},gameActivity),
        };

    }

    /**
     executes the right function according to database read
      */
    private final RoomManager.IGameRoomRead onGameRoomRead = (resigned, enemyTroopId, reversedPos) ->
    {
        if(resigned){
            endGame(true);
            return;
        }
        Troop enemyTroop = boardStateManager.troopMap.get(enemyTroopId);
        enemyAction(enemyTroop, reversedPos);
    };


    private void enemyAction(Troop enemyTroop, int[] newPos)
    {
        Troop movePositionTroop = boardStateManager.posToTroop[newPos[0]][newPos[1]];
        if(movePositionTroop != null){//the troop is a mage that buffed an enemy troop in the given position
            ((Mage) enemyTroop).buffTroop(movePositionTroop);
            visualizer.visualizeBuff(movePositionTroop);
        }
        else {//just move the troop
            int[] oldPos = enemyTroop.getPosition();
            boardStateManager.move(enemyTroop,newPos);
            visualizer.updateVisualsAfterMovement(enemyTroop, oldPos);
        }
        finishTurn(newPos);
    }

    //end of enemy turn logic
    //game end management
    private void checkIfWinByDeath()//checks if someone won by killing the other team, the guest wins if all are dead at the same time
    {//it's written using hosts and guest so in case of a draw the guest wins
        ArrayList<int[]> hostPos = roomManager.isHost ? boardStateManager.myPositions : boardStateManager.enemyPositions;
        ArrayList<int[]> guestPos = !roomManager.isHost ? boardStateManager.myPositions : boardStateManager.enemyPositions;
        if(hostPos.isEmpty())
            endGame(!roomManager.isHost);
        else if(guestPos.isEmpty())
            endGame(roomManager.isHost);
    }

    private void checkIfOnThrone(boolean myTeam, int[] pos)//checks if the moved troop has got on the throne
    {
        if(pos[0] == 0 && pos[1] == 5 && myTeam)
            endGame(true);
        else if(pos[0] == 5 && pos[1] == 0 && !myTeam)
            endGame(false);
    }
    private void endGame(boolean winner)
    {
        visualizer.turnIndicator.setText("Good Game!");
        updateUserWins(winner);
        roomManager.closeGame(winner);
        visualizer.clearGameBoard();

        GameEndDialog gameEndDialog = new GameEndDialog(visualizer.gameActivity);
        gameEndDialog.startGameEndDialog(winner);
    }

    private void updateUserWins(boolean winner)
    {
        if(!winner)
            return;
        String uId = mAuth.getCurrentUser().getUid();
        getUserValues(userValues ->
        {
            int wins = Integer.parseInt(userValues[3]);
            database.getReference("users/" + uId).child("wins").setValue((wins + 1) + "");
        });
    }
    public void resign()
    {
        roomManager.resign();
        endGame(false);
    }

    private void finishMyTurn(int[] clickPos)
    {
        roomManager.submitMoveToDatabase(clickPos,userInputManager.selectedTroop, roomManager.isHost);
        finishTurn(clickPos);
    }
    private void finishTurn(int[] clickPos)
    {
        ArrayList<Troop> attackingTroops = boardStateManager.getAttackingTroops();
        ArrayList<Troop> hitTroops = boardStateManager.attackCycle();
        removeBuff(attackingTroops);
        visualizer.attackCycleVisualized(hitTroops,boardStateManager.getDeadTroops(hitTroops));
        checkIfWinByDeath();
        checkIfOnThrone(turn, clickPos);
        visualizer.switchTurnIndicator();
        turn = !turn;
    }

    private void removeBuff(ArrayList<Troop> attackingTroops)
    {
        for(Troop troop: attackingTroops){
            if(troop.getMaged()){
                troop.setMaged(false);
                troop.setDmg(troop.getDmg()-1);
                visualizer.removeVisualBuff(troop);
            }
        }
    }


    /**
     executes the right function according to the userInputManager
     * @param tilePos: the position of the tile that was pressed
     */
    public void initializeGameClick(int[] tilePos)
    {
        if(!turn)
            return;
        userInputManager.gameClick(tilePos,

                //the clicked troop
                boardStateManager.posToTroop[tilePos[0]][tilePos[1]],

                //gets the legal moves and visualizes
                selectTroopFunction,

                //deals with movement
                moveTroopFunction,

                //deals with buffing
                buffTroopFunction,

                //removes the visual selections
                removeSelectorsFunction);
    }
    private final Consumer<Troop> selectTroopFunction = (clickedTroop)->
    {
        userInputManager.selectedTroop = clickedTroop;
        userInputManager.movementOption = boardStateManager.getLegalMoves(userInputManager.selectedTroop);
        visualizer.visualSelection(userInputManager.selectedTroop);
        visualizer.visualMoveOptions(userInputManager.movementOption);
    };
    private final Consumer<int[]> moveTroopFunction = (clickPos)->
    {
        int[] lastPos = userInputManager.selectedTroop.getPosition();
        boardStateManager.move(userInputManager.selectedTroop,clickPos);
        visualizer.updateVisualsAfterMovement(userInputManager.selectedTroop, lastPos);
        finishMyTurn(clickPos);
    };
    private final BiConsumer<int[],Troop> buffTroopFunction = (clickPos, clickedTroop)->
    {
        visualizer.visualizeBuff(clickedTroop);
        finishMyTurn(clickPos);
    };
    private final BiConsumer<Boolean, int[]> removeSelectorsFunction = (hasMoved, clickPos)->
    {
        visualizer.removeVisualSelection(userInputManager.selectedTroop, hasMoved);
        visualizer.removeVisualMoveOption(userInputManager.movementOption, clickPos, hasMoved);
        userInputManager.selectedTroop = null;
        userInputManager.movementOption = null;
    };
}
