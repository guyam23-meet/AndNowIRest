package com.example.csproject.Troops;

import android.app.Activity;

public class Mage extends Troop
{
    public Mage(String id, Boolean myTeam, int[] position, Activity activity)
    {
        super("mage", id, myTeam, position, activity);
    }
    public boolean buffTroop(Troop troop)//checks if the troop is already buffed, if it isnt, it buffs the troop and returns true
    {
        int[] troopPos = troop.getPosition();
        int[] magePos = getPosition();
        if(Math.abs(magePos[0]-troopPos[0])>getAttackRange()|| //if out of range
           Math.abs(magePos[1]-troopPos[1])>getAttackRange()||
           troop.getMaged())//or already buffed
            return false;
        troop.setDmg(troop.getDmg()+1);
        troop.setMaged(true);
        return true;
    }
}
