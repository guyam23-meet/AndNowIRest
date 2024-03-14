package com.example.csproject.GameEngine;

import android.widget.TextView;

import com.example.csproject.Activities.GameActivity;
import com.example.csproject.Troops.Mage;
import com.example.csproject.Troops.Troop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UserInputManager{
    public HashMap<Integer, int[]> tilesPositions;
    public Troop selectedTroop;
    public ArrayList<int[]> movementOption;

    public UserInputManager(TextView[][] tiles,GameActivity gameActivity){
        tilesPositions = new HashMap<>();
        for(int i = 0; i < tiles.length; i++) {
            for(int j = 0; j < tiles[i].length; j++) {
                tilesPositions.put(tiles[i][j].getId(),new int[]{i,j});
                tiles[i][j].setOnClickListener(gameActivity);
            }
        }
    }

    public void gameClick(int[] clickPos, Troop clickedTroop,
                          Consumer<Troop> selectTroopFunction,
                          Consumer<int[]> moveTroopFunction,
                          BiConsumer<int[],Troop> buffTroopFunction,
                          BiConsumer<Boolean,int[]> removeSelectorsFunction)
    {
        if(selectedTroop == null) {//if there isn't a selected troop
            if(clickedTroop != null && clickedTroop.getMyTeam())//and you pressed on a one of your troops
                selectTroopFunction.accept(clickedTroop);//select it
        }
        else
        {
            boolean legalMove = checkIsMoveOption(clickPos);
            if(legalMove)//the place you picked is empty
                moveTroopFunction.accept(clickPos);

            else if(checkIsMageBuffing(clickedTroop))//if the clicked troop is my troop and not a mage
                if(((Mage) selectedTroop).buffTroop(clickedTroop))//attempt to buff the clicked troop
                    buffTroopFunction.accept(clickPos,clickedTroop);

            removeSelectorsFunction.accept(legalMove, clickPos);
        }
    }

    private boolean checkIsMageBuffing(Troop clickedTroop)
    {
        return clickedTroop != null &&
                clickedTroop.getMyTeam() &&
                selectedTroop instanceof Mage &&
                !(clickedTroop instanceof Mage);
    }

    private boolean checkIsMoveOption(int[] clickPos)
    {
        boolean isMoveOption = false;
        for(int[] moveOption:movementOption){//checks if a moveOption was clicked
            if(Arrays.equals(moveOption, clickPos)) {
                isMoveOption = true;
                break;
            }
        }
        return isMoveOption;
    }
}
