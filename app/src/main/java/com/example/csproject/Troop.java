package com.example.csproject;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Troop {

    private int movement;
    private int attackRange;
    private int dmg;
    private int hp;
    private final String type;
    private final String id;
    private Drawable imageSRC;
    private final boolean myTeam;
    private int[] position;
    private boolean isMaged;
    private boolean isAlive;
    public static HashMap<String, Troop> troopMap = new HashMap<>();
    public static Troop[][] posToTroop = new Troop[6][6];
    public static ArrayList<int[]> myPositions = new ArrayList<>();
    public static ArrayList<int[]> enemyPositions = new ArrayList<>();



    public Troop(String type, String id, Boolean myTeam, int[] position, Activity activity)
    {
        troopMap.put(id, this);
        posToTroop[position[0]][position[1]] = this;
        ArrayList<int[]> posList = myTeam ? myPositions : enemyPositions;
        posList.add(position);

        this.type = type;
        this.isAlive = true;
        this.isMaged = false;
        this.myTeam = myTeam;
        this.position = position;
        this.id = id;
        switch(type) {
            case "swordsman":
                this.movement = 2;
                this.attackRange = 1;
                this.dmg = 2;
                this.hp = 8;
                this.imageSRC = AppCompatResources.getDrawable(activity,myTeam? R.drawable.figure_swordsman:R.drawable.figure_enemy_swordsman);
                break;
            case "knight":
                this.movement = 1;
                this.attackRange = 1;
                this.dmg = 3;
                this.hp = 11;
                this.imageSRC = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_knight:R.drawable.figure_enemy_knight);
                break;
            case "archer":
                this.movement = 1;
                this.attackRange = 2;
                this.dmg = 2;
                this.hp = 6;
                this.imageSRC = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_archer:R.drawable.figure_enemy_archer);
                break;
            case "mage":
                this.movement = 1;
                this.attackRange = 1;
                this.dmg = 0;
                this.hp = 6;
                this.imageSRC = AppCompatResources.getDrawable(activity,myTeam?R.drawable.figure_mage:R.drawable.figure_enemy_mage);
                break;
        }
    }
    public static void resetStaticVariables()
    {
        troopMap.clear();
        enemyPositions.clear();
        myPositions.clear();
        for(int i=0;i<6;i++) {
            for(int j=0;j<6;j++)
                posToTroop[i][j] = null;
        }
    }
    public static ArrayList<Troop> attackCycle()
    {
        for(Troop troop:troopMap.values())
            troop.attack();
        ArrayList<Troop> deadTroops = new ArrayList<>();
        for(Troop troop:troopMap.values())
        {
            if(troop.getHp()<=0){//set dead
                deadTroops.add(troop);
                troop.setAlive(false);
                troop.updateStaticsAfterDeath();
            }
        }
        return deadTroops;
    }
    public ArrayList<int[]> getMovingOptions()//returns all the positions the troop can move to
    {
        ArrayList<int[]> posList = new ArrayList<>();
        int posY = getPosition()[0];
        int posX = getPosition()[1];
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++)
            {
                if((i == 0 && j == 0) ||//your current position
                        posY + i > 5 || posY + i < 0 || posX + j > 5 || posX + j < 0 ||//out of bounds
                        posToTroop[posY + i][posX + j] != null)//is taken by another troop
                    continue;

                if(i==0||j==0)
                    posList.add(new int[]{posY + i, posX + j});//adds neighbors

                if(getMovement() == 1)
                    continue;

                if(i!=0&&j!=0) {
                    posList.add(new int[]{posY + i, posX + j});//adds the corners
                    continue;
                }
                if(!(posY + i*2 > 5 || posY + i*2 < 0 || posX + j*2 > 5 || posX + j*2 < 0)&&//in bounds
                        (posToTroop[posY + i * 2][posX + j * 2] == null))//is empty
                    posList.add(new int[]{posY + i * 2, posX + j * 2});
            }
        }
        return posList;
    }

    public void moveTo(int[] position)//only moves if its within movement options and returns true if it had moved
    {
        updateStaticsAfterMovement(position);
        setPosition(position);
    }

    private void updateStaticsAfterMovement(int[] position)//updates the lists of position to troop and the team positions list
    {
        posToTroop[getPosition()[0]][getPosition()[1]] = null;
        posToTroop[position[0]][position[1]] = this;
        ArrayList<int[]> posList = getMyTeam() ? myPositions : enemyPositions;
        posList.remove(getPosition());
        posList.add(position);
    }

    public void attack()//attacks every troop from the opposite team it can
    {
        ArrayList<Troop> attackableTroops = getAttackableTroops();
        for(Troop attackableTroop : attackableTroops) {//if there is a knight in attack range, attack only the knight
            if(attackableTroop.getType().equals("knight")) {
                attackTroop(attackableTroop);
                return;
            }
        }
        for(Troop attackableTroop : attackableTroops) {
            attackTroop(attackableTroop);
        }
    }

    private ArrayList<Troop> getAttackableTroops()//returns all the troops from opposing team that the troop can attack
    {
        ArrayList<int[]> targetList = getPositionsInAttackRange();
        ArrayList<Troop> attackableTroops = new ArrayList<>();
        ArrayList<int[]> posList = !getMyTeam()?myPositions:enemyPositions;
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

    public void attackTroop(Troop attackedTroop)//reduces the hp and deals with troop death
    {
        attackedTroop.setHp(attackedTroop.getHp()-getDmg());
    }

    private void updateStaticsAfterDeath()
    {
        int[] pos = getPosition();
        posToTroop[pos[0]][pos[1]] = null;
        ArrayList<int[]> posList = getMyTeam()?myPositions:enemyPositions;
        posList.remove(pos);
    }

    public ArrayList<int[]> getPositionsInAttackRange()//return all positions in the attack range of the troop
    {
        ArrayList<int[]> targetList = new ArrayList<>();
        int posY = getPosition()[0];
        int posX = getPosition()[1];
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

    public Drawable getImageSRC()
    {
        return imageSRC;
    }

    public boolean getMyTeam()
    {
        return myTeam;
    }

    public int[] getPosition()
    {
        return position;
    }
    public void setImageSRC(Drawable imageSRC)
    {
        this.imageSRC = imageSRC;
    }

    public boolean getMaged()
    {
        return isMaged;
    }

    public boolean getAlive()
    {
        return isAlive;
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

    public void setAlive(Boolean alive)
    {
        isAlive = alive;
    }
}
