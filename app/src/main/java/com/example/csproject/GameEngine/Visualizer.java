package com.example.csproject.GameEngine;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.csproject.Activities.GameActivity;
import com.example.csproject.R;
import com.example.csproject.Troops.Troop;

import java.util.ArrayList;
import java.util.Arrays;

public class Visualizer {
    
    private final String hpSymbol;
    private TextView[][] tiles;
    private final LinearLayout gameBoard;
    public GameActivity gameActivity;
    public TextView turnIndicator;

    public Visualizer(LinearLayout gameBoard, GameActivity gameActivity, TextView turnIndicator, Troop[] startingBoard)
    {
        this.gameActivity = gameActivity;
        this.hpSymbol = "❤";
        this.gameBoard = gameBoard;
        this.turnIndicator = turnIndicator;
        setTiles();
        visualizeStartingBoard(startingBoard);
    }//visual management
    public void setTiles()
    {
        ConstraintLayout row;
        TextView tile;
        tiles = new TextView[6][6];
        for(int i = 0; i < 6; i++) {
            row = (ConstraintLayout) gameBoard.getChildAt(i);
            for(int j = 0; j < 6; j++) {
                tile = (TextView) row.getChildAt(j);
                tiles[i][j] = tile;
            }
        }
    }
    public void visualizeStartingBoard(Troop[] startingBoard){
        for(Troop troop : startingBoard) {
            int[] troopPos = troop.getPosition();
            TextView tile = tiles[troopPos[0]][troopPos[1]];
            tile.setBackground(troop.getTroopIcon());
            tile.setText(hpSymbol+troop.getHp()+" ");
        }
    }
    public void visualSelection(Troop selectedTroop)
    {
        Drawable[] layers = {gameActivity.getDrawable(R.drawable.visuals_selected_background), selectedTroop.getTroopIcon()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        int[] troopPos = selectedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }

    public void removeVisualSelection(Troop selectedTroop, boolean hasMoved)
    {
        if(!hasMoved) {
            int[] pos = selectedTroop.getPosition();
            TextView tile = tiles[pos[0]][pos[1]];
            tile.setBackground(selectedTroop.getTroopIcon());
        }
    }

    public void visualMoveOptions(ArrayList<int[]> movementOptions)
    {
        for(int[] pos : movementOptions) {
            tiles[pos[0]][pos[1]].setBackgroundResource(R.drawable.visuals_move_option);
        }
    }

    public void removeVisualMoveOption(ArrayList<int[]> moveOptions, int[] newPos, boolean hasMoved)
    {
        for(int[] pos : moveOptions) {
            if(hasMoved && Arrays.equals(pos, newPos))
                continue;
            returnBackGroundToOrigin(pos);
        }
    }

    public void visualizeBuff(Troop clickedTroop)
    {
        Drawable[] layers = {gameActivity.getDrawable(R.drawable.visuals_buffed_background), clickedTroop.getTroopIcon()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        clickedTroop.setTroopIcon(layerDrawable);
        int[] troopPos = clickedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }

    private void returnBackGroundToOrigin(int[] pos)
    {
        tiles[pos[0]][pos[1]].setBackground(null);
        tiles[pos[0]][pos[1]].setText("");
    }

    public void attackCycleVisualized(ArrayList<Troop> hitTroops,ArrayList<Troop> deadTroops)
    {
        for(Troop troop : hitTroops) {//need to make all of them flash white for a sec
            int[] pos = troop.getPosition();
            tiles[pos[0]][pos[1]].setText(hpSymbol + troop.getHp() + " ");
            tiles[pos[0]][pos[1]].setBackground(visualizeHit(troop));
        }
        new Handler().postDelayed(() -> {
            for(Troop troop : hitTroops) {
                int[] pos = troop.getPosition();
                if(deadTroops.contains(troop))
                    returnBackGroundToOrigin(pos);
                else
                    tiles[pos[0]][pos[1]].setBackground(troop.getTroopIcon());
            }
        }, 1000);
    }

    private Drawable visualizeHit(Troop troop)
    {
        Drawable[] layers = {gameActivity.getDrawable(R.drawable.visuals_hit_background), troop.getTroopIcon()};
        return new LayerDrawable(layers);
    }

    public void updateVisualsAfterMovement(Troop movedTroop, int[] oldPos)
    {
        returnBackGroundToOrigin(oldPos);
        int[] newPos = movedTroop.getPosition();
        tiles[newPos[0]][newPos[1]].setBackground(movedTroop.getTroopIcon());
        tiles[newPos[0]][newPos[1]].setText(hpSymbol + movedTroop.getHp() + " ");
    }

    public void clearGameBoard()
    {
        gameBoard.setBackgroundResource(R.drawable.gameboard_background);
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 6; j++) {
                tiles[i][j].setBackground(null);
                tiles[i][j].setText("");
                tiles[i][j].setClickable(false);
            }
        }
    }

    public TextView[][] getTiles()
    {
        return tiles;
    }

    public void removeVisualBuff(Troop troop)
    {
        Drawable originalIcon = ((LayerDrawable)troop.getTroopIcon()).getDrawable(1);
        troop.setTroopIcon(originalIcon);
        int[] pos = troop.getPosition();
        tiles[pos[0]][pos[1]].setBackground(troop.getTroopIcon());
    }

    public void switchTurnIndicator()
    {
        if(turnIndicator.getVisibility() == View.INVISIBLE)
            turnIndicator.setVisibility(View.VISIBLE);
        else
            turnIndicator.setVisibility(View.INVISIBLE);
    }
}