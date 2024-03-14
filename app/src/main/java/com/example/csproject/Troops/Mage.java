package com.example.csproject.Troops;

import android.app.Activity;

import java.util.Arrays;

public class Mage extends Troop
{
    public Mage(String id, Boolean myTeam, int[] position, Activity activity)
    {
        super("mage", id, myTeam, position, activity);
    }
    public boolean buffTroop(Troop troop)//checks if the troop is already buffed, if it isnt, it buffs the troop and returns true
    {
        int[] troopPos = troop.getPosition();
        if(getPositionsInAttackRange().stream().noneMatch(pos -> Arrays.equals(pos,troopPos)) ||//goes over the pos in getPositions... and compares it to the troopPos, if non fit, returns true
           troop.getMaged())//or already buffed
            return false;
        troop.setDmg(troop.getDmg()+1);
        troop.setMaged(true);
        return true;
    }
}
