package com.example.csproject;

import static com.example.csproject.CommonFunctions.database;
import static com.example.csproject.CommonFunctions.fullscreenSetup;
import static com.example.csproject.CommonFunctions.getUserValues;
import static com.example.csproject.CommonFunctions.mAuth;
import static com.example.csproject.CommonFunctions.removeGameRoom;
import static com.example.csproject.CommonFunctions.systemUiChangeManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    //visuals outside of the game
    private TextView enemyName;
    private TextView enemyPlacement;
    private TextView myName;
    private TextView myPlacement;
    private TextView resignIcon;
    private TextView turnIndicator;
    //end of visuals outside of the game

    //visuals in the game
    private LinearLayout gameBoard;
    private TextView[][] tiles;
    private static final String hpSymbol = "❤";
    private HashMap<Integer, int[]> tilesPositions;
    //end of visuals in the game

    //game logic variables
    private boolean turn;
    private Troop selectedTroop;
    private ArrayList<int[]> movementOption;
    //end of game logic variables

    //database related variables
    private DatabaseReference gameRoom;
    private boolean isHost;
    private ValueEventListener moveListener;
    //end of database related variables
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_game);
        systemUiChangeManager(getWindow().getDecorView());

        //connects the Views outside of the game
        connectViews();

        //connects the Views in the game
        gameBoard = findViewById(R.id.game_board);
        setTilesAndPositions();

        //connects the game room to the database
        Bundle bundle = getIntent().getExtras();
        String roomRef = bundle.getString("roomRef");
        gameRoom = database.getReference(roomRef);

        //sets the relevant booleans according to database
        isHost = gameRoom.getKey().equals(mAuth.getCurrentUser().getUid());
        turn = isHost;

        //sets the turn indicator according to the turn
        turnIndicator.setText("It's "+(turn?"your":enemyName.getText().toString().toUpperCase()+"'s")+" turn");

        //reads from database and updates relevant info
        updateGuestAndHostInfoLines();
        updateGamesPlayed();

        //sets the game and database listener for the game
        gameConstructor();
        readEnemyMovesFromGameRoom();

        //start background music
        startService(new Intent(GameActivity.this, musicService.class));
    }

    //non logic set ups
    private void updateGuestAndHostInfoLines()
    {
        getGameRoomValues(gameRoom, roomValues ->
        {
            String hostName = roomValues[1];
            String guestName = roomValues[2];
            String hostPlacement = roomValues[3];
            String guestPlacement = roomValues[4];
            if(isHost) {
                myName.setText(hostName);
                enemyName.setText(guestName);
                myPlacement.setText(hostPlacement);
                enemyPlacement.setText(guestPlacement);
            } else {
                myName.setText(guestName);
                enemyName.setText(hostName);
                myPlacement.setText(guestPlacement);
                enemyPlacement.setText(hostPlacement);
            }
        });
    }
    private void setTilesAndPositions()
    {
        ConstraintLayout row;
        TextView tile;
        tiles = new TextView[6][6];
        tilesPositions = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            row = (ConstraintLayout) gameBoard.getChildAt(i);
            for(int j = 0; j < 6; j++) {
                tile = (TextView) row.getChildAt(j);
                tiles[i][j] = tile;
                tile.setOnClickListener(this);
                tilesPositions.put(tile.getId(), new int[]{i, j});
            }
        }
    }
    private void getGameRoomValues(DatabaseReference gameRoom, ICallBack iCallBack)
    {
        String[] values = new String[7];
        values[0] = gameRoom.getKey();
        gameRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                values[1] = snapshot.child("host").getValue().toString();
                values[2] = snapshot.child("guest").getValue().toString();
                values[3] = snapshot.child("host_placement").getValue().toString();
                values[4] = snapshot.child("guest_placement").getValue().toString();
                if(snapshot.hasChild("move"))
                    values[6] = snapshot.child("move").getValue().toString();
                else
                    values[6] = "";
                iCallBack.onCallBack(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        });

    }
    private void connectViews()
    {
        enemyName = findViewById(R.id.tv_enemy_name_activity_game);
        enemyPlacement = findViewById(R.id.tv_enemy_placement_activity_game);
        myName = findViewById(R.id.tv_name_activity_game);
        myPlacement = findViewById(R.id.tv_placement_activity_game);
        turnIndicator = findViewById(R.id.turn_indicator);
        resignIcon = findViewById(R.id.resign_icon);
        resignIcon.setOnClickListener(this);
    }
    private void updateGamesPlayed()
    {
        getUserValues(userValues ->
        {
            String userId = userValues[0];
            int userGamesPlayed = Integer.parseInt(userValues[4]);
            database.getReference("users/" + userId + "/games_played").setValue("" + (userGamesPlayed + 1));
        });
    }
    //end non logic set ups

    //logic set ups
    private void gameConstructor()
    {
        Troop.resetStaticVariables();
        Troop[] startingBoard = new Troop[]{
                //my troops
                new Troop("archer", "ma1", true, new int[]{5, 1},this),
                new Troop("archer", "ma2", true, new int[]{5, 3},this),
                new Troop("swordsman", "ms1", true, new int[]{4, 1},this),
                new Troop("swordsman", "ms2", true, new int[]{4, 3},this),
                new Mage("mm1", true, new int[]{5, 2},this),
                new Troop("knight", "mk1", true, new int[]{4, 2},this),
                //enemy troops
                new Troop("archer", "ea1", false, new int[]{0, 4},this),
                new Troop("archer", "ea2", false, new int[]{0, 2},this),
                new Troop("swordsman", "es1", false, new int[]{1, 4},this),
                new Troop("swordsman", "es2", false, new int[]{1, 2},this),
                new Mage("em1", false, new int[]{0, 3},this),
                new Troop("knight", "ek1", false, new int[]{1, 3},this),
        };

        for(Troop troop : startingBoard) {
            int[] troopPos = troop.getPosition();
            TextView tile = tiles[troopPos[0]][troopPos[1]];
            tile.setBackground(troop.getTroopIcon());
            tile.setText(hpSymbol+troop.getHp()+" ");
        }
        tiles[5][0].setBackgroundResource(R.drawable.figure_throne);
        tiles[0][5].setBackgroundResource(R.drawable.figure_throne);
    }

    //visual management
    private void visualSelection(Troop selectedTroop)
    {
        Drawable[] layers = {getDrawable(R.drawable.selected_background),selectedTroop.getTroopIcon()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        int[] troopPos = selectedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }
    private void removeVisualSelection(Troop selectedTroop, boolean hasMoved)
    {
        if(!hasMoved) {
            int[] pos = selectedTroop.getPosition();
            TextView tile = tiles[pos[0]][pos[1]];
            tile.setBackground(selectedTroop.getTroopIcon());
        }
    }
    private void visualMoveOptions(ArrayList<int[]> movementOptions)
    {
        for(int[] pos: movementOptions) {
            tiles[pos[0]][pos[1]].setBackgroundResource(R.drawable.move_option);
        }
    }
    private void removeVisualMoveOption(ArrayList<int[]> moveOptions,int[] newPos,boolean hasMoved)
    {
        for(int[] pos: moveOptions)
        {
            if(hasMoved && Arrays.equals(pos, newPos))
                continue;
            returnBackGroundToOrigin(pos);
        }
    }
    private void visualizeBuff(Troop clickedTroop)
    {
        Drawable[] layers = {getDrawable(R.drawable.buffed_background),clickedTroop.getTroopIcon()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        clickedTroop.setTroopIcon(layerDrawable);
        int[] troopPos = clickedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }
    private void returnBackGroundToOrigin(int[] pos)
    {
        if(Arrays.equals(pos,new int[] {0,5})||Arrays.equals(pos,new int[] {5,0})) {
            tiles[pos[0]][pos[1]].setBackgroundResource(R.drawable.figure_throne);
        } else
            tiles[pos[0]][pos[1]].setBackground(null);
        tiles[pos[0]][pos[1]].setText("");
    }
    private void attackCycleVisualized(ArrayList<Troop> deadTroops)
    {
        for(Troop troop : Troop.troopMap.values()) {
            int[] pos = troop.getPosition();
            tiles[pos[0]][pos[1]].setText(hpSymbol+troop.getHp()+" ");
        }
        for(Troop dead: deadTroops)
        {
            int[] deadPos = dead.getPosition();
            returnBackGroundToOrigin(deadPos);
            Troop.troopMap.remove(dead.getId());
        }
    }
    private void updateVisualsAfterMovement(Troop movedTroop, int[] oldPos)
    {
        returnBackGroundToOrigin(oldPos);
        int[] newPos = movedTroop.getPosition();
        tiles[newPos[0]][newPos[1]].setBackground(movedTroop.getTroopIcon());
        tiles[newPos[0]][newPos[1]].setText(hpSymbol+movedTroop.getHp()+" ");
    }
    //end of visual management

    //resign functions
    private void onResignPressed()
    {
        resignIcon.setClickable(false);//prevents double clicking
        ResignDialog resignDialog = new ResignDialog(GameActivity.this);
        resignDialog.startResignDialog();
    }
    public void enableResignButton()
    {
        resignIcon.setClickable(true);
    }
    public void resign()
    {
        gameRoom.child("move").setValue("resign_"+(isHost?'h':'g'));
        endGame(false);
    }
    //end of resign functions

    //my turn logic
    private void gameClick(int[] clickPos)
    {
        if(!turn)
            return;
        Troop clickedTroop = Troop.posToTroop[clickPos[0]][clickPos[1]];//clicked troop
        if(selectedTroop == null)//if there isn't a selected troop
        {
            if(clickedTroop != null && clickedTroop.getMyTeam()) {//and you pressed on a one of your troops
                selectedTroop = clickedTroop;//select it
                movementOption = selectedTroop.getMovingOptions();
                visualSelection(selectedTroop);
                visualMoveOptions(movementOption);
            }
            return;
        }
        //there is a troop selected
        boolean hasMoved = false;
        boolean isMoveOption = false;
        for(int[] moveOption:movementOption){//checks if a moveOption was clicked
            if(Arrays.equals(moveOption,clickPos)) {
                isMoveOption = true;
                break;
            }
        }
        if(isMoveOption)//the place you picked is empty
        {
            int[] lastPos = selectedTroop.getPosition();
            selectedTroop.moveTo(clickPos);
            hasMoved = true;
            updateVisualsAfterMovement(selectedTroop, lastPos);
            finishTurn(clickPos);
        }
        else if(clickedTroop!=null &&
                clickedTroop.getMyTeam() &&
                selectedTroop instanceof Mage &&
                !(clickedTroop instanceof Mage)){//if the clicked troop is my troop and not a mage

            if(((Mage) selectedTroop).buffTroop(clickedTroop))
            {//attempt to buff the clicked troop
                visualizeBuff(clickedTroop);
                finishTurn(clickPos);
            }
        }
        removeVisualSelection(selectedTroop, hasMoved);
        removeVisualMoveOption(movementOption, clickPos, hasMoved);
        selectedTroop = null;
        movementOption = null;
    }
    private void finishTurn(int[] clickPos)
    {
        submitMoveToDatabase(clickPos);
        ArrayList<Troop> deadTroops = Troop.attackCycle();
        attackCycleVisualized(deadTroops);
        checkIfWinByDeath();
        checkIfOnThrone(true, clickPos);
        turn = false;
        turnIndicator.setText("it's "+ enemyName.getText().toString().toUpperCase()+"'s turn");
    }
    private void submitMoveToDatabase(int[] clickPos)
    {
        String troopId = selectedTroop.getId();
        String yx = "" + clickPos[0] + clickPos[1];
        String isHostLabel = isHost ? "h" : "g";
        gameRoom.child("move").setValue(troopId + "_" + yx + "_" + isHostLabel);
    }
    //end of my turn logic

    //enemy turn logic
    private void readEnemyMovesFromGameRoom()
    {//gets the move the other person did
        moveListener = gameRoom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!snapshot.hasChild("move"))//both handles unexpected calls to DB and disables the function when it is declared
                    return;
                String moveValue = snapshot.child("move").getValue().toString();

                char hostId = isHost?'h':'g';
                if(hostId==moveValue.charAt(7))//check if its your action
                    return;

                if(moveValue.startsWith("resign")) {
                    endGame(true);
                    return;
                }
                String movedTroopPos = moveValue.substring(4, 6);
                String reversedTroopId = "e" + moveValue.substring(1, 3);;//replaces id from other players screen to the corresponding one in this screen
                int y = Character.getNumericValue(movedTroopPos.charAt(0));
                int x = Character.getNumericValue(movedTroopPos.charAt(1));
                int[] reversedPos = new int[]{5 - y, 5 - x};//replaces position from other players screen to the corresponding one in this screen
                Troop enemyTroop = Troop.troopMap.get(reversedTroopId);
                enemyMove(enemyTroop, reversedPos);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void enemyMove(Troop enemyTroop, int[] newPos)
    {
        Troop movePositionTroop = Troop.posToTroop[newPos[0]][newPos[1]];
        if(movePositionTroop != null){//the troop is a mage that buffed an enemy troop in the given position
            ((Mage) enemyTroop).buffTroop(movePositionTroop);
            visualizeBuff(movePositionTroop);
        }

        else {//just move the troop
            int[] oldPos = enemyTroop.getPosition();
            enemyTroop.moveTo(newPos);
            updateVisualsAfterMovement(enemyTroop, oldPos);
            checkIfOnThrone(false, newPos);
        }
        ArrayList<Troop> deadTroops = Troop.attackCycle();
        attackCycleVisualized(deadTroops);
        checkIfWinByDeath();
        turnIndicator.setText("it's your turn");
        turn = true;
    }
    //end of enemy turn logic

    //game end management
    private void checkIfWinByDeath()//checks if someone won by killing the other team, the guest wins if all are dead at the same time
    {
        ArrayList<int[]> hostPos = isHost ? Troop.myPositions : Troop.enemyPositions;
        ArrayList<int[]> guestPos = !isHost ? Troop.myPositions : Troop.enemyPositions;
        if(hostPos.isEmpty())
            endGame(!isHost);
        else if(guestPos.isEmpty())
            endGame(isHost);
    }
    private void checkIfOnThrone(boolean myTeam, int[] pos)//checks if the moved troop has got on the throne
    {
        if(pos[0] == 0 && pos[1] == 5 && myTeam)
            endGame(true);
        if(pos[0] == 5 && pos[1] == 0 && !myTeam)
            endGame(false);
    }
    private void endGame(boolean winner)
    {
        turnIndicator.setText("Good Game!");
        updateUserWins(winner);
        gameRoom.removeEventListener(moveListener);
        if(winner)
            removeGameRoom(gameRoom);
        clearGameBoard();
        GameEndDialog gameEndDialog = new GameEndDialog(GameActivity.this);
        gameEndDialog.startGameEndDialog(winner);
    }
    private void clearGameBoard()
    {
        gameBoard.setBackgroundResource(R.drawable.background_pixelated);
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 6; j++) {
                tiles[i][j].setBackground(null);
                tiles[i][j].setText("");
                tiles[i][j].setClickable(false);
            }
        }
    }
    private void updateUserWins(boolean winner)
    {
        String uId = mAuth.getCurrentUser().getUid();
        int score = winner? 1 : 0;
        getUserValues(userValues ->
        {
            int wins = Integer.parseInt(userValues[3]);
            database.getReference("users/" + uId).child("wins").setValue((wins + score) + "");
        });
    }
    public void stopBackgroundMusic()
    {
        stopService(new Intent(GameActivity.this, musicService.class));
    }
    //end of game end management

    //overrides
    @Override
    public void onClick(View view)
    {
        if(view == resignIcon) {
            onResignPressed();
            return;
        }
        int viewId = view.getId();
        if(tilesPositions.get(viewId) != null){//if you pressed a tile
            gameClick(tilesPositions.get(viewId));
        }
    }
    @Override
    public void onBackPressed() {}//disables back press
    @Override
    protected void onDestroy()
    {//when you leave you resign
        super.onDestroy();
        resign();
    }
    //end of overrides
}