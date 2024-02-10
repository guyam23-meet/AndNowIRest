package com.example.csproject.Troops;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.csproject.R;

import java.util.ArrayList;

public class Troop {
    //troop stats
    private final String type;
    private final String id;
    private final boolean myTeam;
    private int movement;
    private int attackRange;
    private int dmg;
    private int hp;
    private int[] position;
    private boolean isMaged;
    private Drawable troopIcon;

    
    //constructor
    public Troop(String type, String id, Boolean myTeam, int[] position, Activity activity)
    {
        this.type = type;
        this.isMaged = false;
        this.myTeam = myTeam;
        this.position = position;
        this.id = id;
        setStatsForType(type, activity);
    }
    private void setStatsForType(String type,Activity activity)
    {
        int movement = 0;
        int attackRange = 0;
        int dmg = 0;
        int hp= 0;
        Drawable troopIcon = null;
        switch(type) {
            case "swordsman":
                movement = 2;
                attackRange = 1;
                dmg = 2;
                hp = 8;
                troopIcon = AppCompatResources.getDrawable(activity,myTeam? R.drawable.figure_swordsman:R.drawable.figure_enemy_swordsman);
                break;
            case "knight":
                movement = 1;
                attackRange = 1;
                dmg = 3;
                hp = 11;
                troopIcon = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_knight:R.drawable.figure_enemy_knight);
                break;
            case "archer":
                movement = 1;
                attackRange = 2;
                dmg = 2;
                hp = 6;
                troopIcon = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_archer:R.drawable.figure_enemy_archer);
                break;
            case "mage":
                movement = 1;
                attackRange = 1;
                dmg = 0;
                hp = 6;
                troopIcon = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_mage:R.drawable.figure_enemy_mage);
                break;
        }
        this.movement = movement;
        this.attackRange = attackRange;
        this.dmg = dmg;
        this.hp = hp;
        this.troopIcon = troopIcon;
    }

    //attack troop methods
    public void attackTroop(Troop attackedTroop)//reduces the hp and deals with troop death
    {
        attackedTroop.setHp(attackedTroop.getHp()-getDmg());
    }
    public ArrayList<int[]> getPositionsInAttackRange()//return all positions in the attack range of the troop
    {
        ArrayList<int[]> targetList = new ArrayList<>();
        int[] pos = getPosition();
        int posY = pos[0];
        int posX = pos[1];
        int range = getAttackRange();
        for(int i = -range; i <= range; i++) {
            for(int j = -range; j <= range; j++)
            {
                if(!(i == j || i == -j || i == 0 || j == 0) ||//not diagonal or axis
                        (i == 0 && j == 0) ||//the same position as you
                        (posY + i > 5 || posY + i < 0 || posX + j > 5 || posX + j < 0))//out of bounds
                    continue;
                targetList.add(new int[]{posY + i, posX + j});
            }
        }
        return targetList;
    }

    //movement troop methods
    public ArrayList<int[]> getMovesInRange(){
        ArrayList<int[]> moves = new ArrayList<>();
        int[] pos = getPosition();
        int posY = pos[0];
        int posX = pos[1];
        int movement = getMovement();
        for(int i = -movement; i <= movement; i++) {
            for(int j = -movement; j <= movement; j++)
            {
                if(
                        (i == 0 && j == 0) ||//your current position
                        (posY + i > 5 || posY + i < 0 || posX + j > 5 || posX + j < 0) ||//out of bounds
                        (Math.abs(i)+Math.abs(j)>movement)//out of movement range
                ) {continue;}
                moves.add(new int[]{i,j});
            }
        }
        return moves;
    }

    //getters
    public int getMovement()
    {
        return movement;
    }
    public int getAttackRange()
    {
        return attackRange;
    }
    public int getDmg()
    {
        return dmg;
    }
    public int getHp()
    {
        return hp;
    }
    public String getId()
    {
        return id;
    }
    public Drawable getTroopIcon()
    {
        return troopIcon;
    }
    public boolean getMyTeam()
    {
        return myTeam;
    }
    public int[] getPosition()
    {
        return position;
    }
    public void setTroopIcon(Drawable troopIcon)
    {
        this.troopIcon = troopIcon;
    }
    public boolean getMaged()
    {
        return isMaged;
    }
    public String getType()
    {
        return type;
    }

    //setters
    public void setDmg(int dmg)
    {
        this.dmg = dmg;
    }
    public void setHp(int hp)
    {
        this.hp = hp;
    }
    public void setPosition(int[] position)
    {
        this.position = position;
    }
    public void setMaged(Boolean maged)
    {
        isMaged = maged;
    }
}
