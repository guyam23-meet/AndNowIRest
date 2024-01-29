package com.example.csproject;

public class Mage extends Troop
{
    public Mage(String id, Boolean myTeam, int[] position)
    {
        super("mage", id, myTeam, position);
    }
    public boolean buffTroop(Troop troop)//checks if the troop is already buffed, if it isnt, it buffs the troop and returns true
    {
        if(troop.getMaged())
            return false;
        troop.setDmg(troop.getDmg()+1);
        troop.setMaged(true);
        return true;
    }
}