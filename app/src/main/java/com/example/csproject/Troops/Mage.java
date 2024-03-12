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
        if(!getPositionsInAttackRange().contains(troopPos)||
           troop.getMaged())//or already buffed
            return false;
        troop.setDmg(troop.getDmg()+1);
        troop.setMaged(true);
        return true;
    }
}
