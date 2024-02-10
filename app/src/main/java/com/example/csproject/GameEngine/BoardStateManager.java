package com.example.csproject.GameEngine;

import com.example.csproject.Troops.Troop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BoardStateManager {
    //static variables for easy game logic
    public HashMap<String, Troop> troopMap;
    public Troop[][] posToTroop;
    public ArrayList<int[]> myPositions;
    public ArrayList<int[]> enemyPositions;

    public BoardStateManager(Troop[] startingBoard)
    {
        this.troopMap = new HashMap<>();
        this.posToTroop = new Troop[6][6];
        this.myPositions = new ArrayList<>();
        this.enemyPositions = new ArrayList<>();
        for(Troop troop : startingBoard){
            troopMap.put(troop.getId(), troop);
            int[] pos = troop.getPosition();
            posToTroop[pos[0]][pos[1]] = troop;
            ArrayList<int[]> posList = troop.getMyTeam()? myPositions:enemyPositions;
            posList.add(pos);
        }
    }

    //move functions
    public void move(Troop troop, int[] position)//updates the lists of position to troop and the team positions list
    {
        posToTroop[troop.getPosition()[0]][troop.getPosition()[1]] = null;
        posToTroop[position[0]][position[1]] = troop;
        ArrayList<int[]> posList = troop.getMyTeam() ? myPositions : enemyPositions;
        posList.remove(troop.getPosition());
        posList.add(position);
        troop.setPosition(position);
    }
    public ArrayList<int[]> getLegalMoves(Troop troop)//returns all the positions the troop can move to
    {
        ArrayList<int[]> moveInRange = troop.getMovesInRange();//returns moves with relative notation
        ArrayList<int[]> legalMoves = new ArrayList<>();
        int[] pos = troop.getPosition();
        for(int[] move: moveInRange)
        {
            if(posToTroop[pos[0]+move[0]][pos[1]+move[1]] != null)
                continue;

            if(move[0]==2||move[0]==-2)
                if(posToTroop[pos[0]+move[0]/2][pos[1]+move[1]]!=null)
                    continue;

            else if(move[1]==2||move[1]==-2)
                if(posToTroop[pos[0]+move[0]][pos[1]+move[1]/2]!=null)
                    continue;

            legalMoves.add(new int[]{pos[0]+move[0],pos[1]+move[1]});//converts the relative to absolute notation
        }
        return legalMoves;
    }
    //end of move functions

    //attack functions
    public ArrayList<Troop> attackCycle()//makes all troops attack and then calls removeDead on dead troops
    {
        for(Troop troop : troopMap.values())
            attack(troop);
        ArrayList<Troop> deadTroops = new ArrayList<>();
        for(Troop troop : troopMap.values()) {
            if(troop.getHp() <= 0) {//set dead
                deadTroops.add(troop);
            }
        }
        removeAllDead(deadTroops);//can't edit troopMap while irritating over it
        return deadTroops;
    }

    private void removeAllDead(ArrayList<Troop> deadTroops)
    {
        for(Troop dead: deadTroops)
            removeDead(dead);
    }

    private void attack(Troop troop)//attacks every troop from the opposite team it can
    {
        ArrayList<Troop> attackableTroops = getAttackableTroops(troop);
        for(Troop attackableTroop : attackableTroops) {//if there is a knight in attack range, attack only the knight
            if(attackableTroop.getType().equals("knight")) {
                troop.attackTroop(attackableTroop);
                return;
            }
        }
        for(Troop attackableTroop : attackableTroops) {
            troop.attackTroop(attackableTroop);
        }
    }
    private ArrayList<Troop> getAttackableTroops(Troop troop)//returns all the troops from opposing team that the troop can attack
    {
        ArrayList<int[]> targetList = troop.getPositionsInAttackRange();
        ArrayList<Troop> attackableTroops = new ArrayList<>();
        ArrayList<int[]> posList = !troop.getMyTeam()? myPositions : enemyPositions;
        for(int[] target : targetList) {
            for(int[] troopPos : posList)
            {
                if(Arrays.equals(target,troopPos)) {
                    attackableTroops.add(posToTroop[troopPos[0]][troopPos[1]]);
                }
            }
        }
        return attackableTroops;
    }

    private void removeDead(Troop troop)//removes the dead troops from the lists
    {
        int[] pos = troop.getPosition();
        posToTroop[pos[0]][pos[1]] = null;
        ArrayList<int[]> posList = troop.getMyTeam() ? myPositions : enemyPositions;
        posList.remove(pos);
        troopMap.remove(troop.getId());
    }
    //end of attack functions
}