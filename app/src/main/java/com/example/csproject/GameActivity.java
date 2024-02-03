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
    public LinearLayout gameBoard;
    public TextView[][] tiles;
    public HashMap<Integer, int[]> tilesPositions;
    public DatabaseReference gameRoom;
    public Boolean isHost;
    public TextView enemyName;
    public TextView enemyPlacement;
    public TextView myName;
    public TextView myPlacement;
    public TextView resignIcon;
    public TextView turnIndicator;
    public Troop selectedTroop;
    public ArrayList<int[]> movementOption;
    public boolean turn;
    public ValueEventListener moveListener;
    public static final String hpSymbol = "❤";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_game);
        systemUiChangeManager(getWindow().getDecorView());

        gameBoard = findViewById(R.id.game_board);
        setTilesAndPositions();
        connectViews();

        Bundle bundle = getIntent().getExtras();
        String roomRef = bundle.getString("roomRef");

        gameRoom = database.getReference(roomRef);
        isHost = gameRoom.getKey().equals(mAuth.getCurrentUser().getUid());
        turn = isHost;
        updateGuestAndHostInfoLines();

        updateGamesPlayed();
        gameConstructor();
        readMovesFromGameRoom();
    }

    public void visualSelection(Troop selectedTroop)
    {
        Drawable[] layers = {getDrawable(R.drawable.selected_background),selectedTroop.getImageSRC()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        int[] troopPos = selectedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }

    public void getGameRoomValues(DatabaseReference gameRoom, ICallBack iCallBack)
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

    public void updateGamesPlayed()
    {
        getUserValues(userValues ->
        {
            String userId = userValues[0];
            int userGamesPlayed = Integer.parseInt(userValues[4]);
            database.getReference("users/" + userId + "/games_played").setValue("" + (userGamesPlayed + 1));
        });
    }

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

            turnIndicator.setText("It's "+(turn?"your":enemyName.getText().toString().toUpperCase()+"'s")+" turn");
        });
    }

    public void setTilesAndPositions()
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

    @Override
    public void onClick(View view)
    {
        if(view == resignIcon) {
            resignIcon.setClickable(false);//prevents double clicking
            ResignDialog resignDialog = new ResignDialog(GameActivity.this);
            resignDialog.startResignDialog();
            return;
        }
        int viewId = view.getId();
        if(turn && tilesPositions.get(viewId) != null)//if its your turn, checks if you pressed a tile
            gameClick(tilesPositions.get(viewId));
    }
    public void resign()
    {
        gameRoom.child("move").setValue("resign_"+(isHost?'h':'g'));
        endGame(false);
    }

    public void gameClick(int[] clickPos)
    {
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

    private void returnBackGroundToOrigin(int[] pos)
    {
        if(Arrays.equals(pos,new int[] {0,5})||Arrays.equals(pos,new int[] {5,0})) {
            tiles[pos[0]][pos[1]].setBackgroundResource(R.drawable.figure_throne);
        } else
            tiles[pos[0]][pos[1]].setBackground(null);
        tiles[pos[0]][pos[1]].setText("");
    }

    private void visualizeBuff(Troop clickedTroop)
    {
        Drawable[] layers = {getDrawable(R.drawable.buffed_background),clickedTroop.getImageSRC()};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        clickedTroop.setImageSRC(layerDrawable);
        int[] troopPos = clickedTroop.getPosition();
        TextView tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }

    private void removeVisualSelection(Troop selectedTroop, boolean hasMoved)
    {
        if(!hasMoved) {
            int[] pos = selectedTroop.getPosition();
            TextView tile = tiles[pos[0]][pos[1]];
            tile.setBackground(selectedTroop.getImageSRC());
        }
    }

    public void checkIfWinByDeath()//checks if someone won by killing the other team, the guest wins if all are dead at the same time
    {
        ArrayList<int[]> hostPos = isHost ? Troop.myPositions : Troop.enemyPositions;
        ArrayList<int[]> guestPos = !isHost ? Troop.myPositions : Troop.enemyPositions;
        if(hostPos.isEmpty())
            endGame(!isHost);
        else if(guestPos.isEmpty())
            endGame(isHost);
    }

    public void finishTurn(int[] clickPos)
    {
        submitMoveToDatabase(clickPos);
        attackCycleVisualized();
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

    public void attackCycleVisualized()
    {
        ArrayList<Troop> deadTroops = Troop.attackCycle();
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

    public void updateVisualsAfterMovement(Troop movedTroop, int[] oldPos)
    {
        returnBackGroundToOrigin(oldPos);
        int[] newPos = movedTroop.getPosition();
        tiles[newPos[0]][newPos[1]].setBackground(movedTroop.getImageSRC());
        tiles[newPos[0]][newPos[1]].setText(hpSymbol+movedTroop.getHp()+" ");
    }

    public void checkIfOnThrone(boolean myTeam, int[] pos)//checks if the moved troop has got on the throne
    {
        if(pos[0] == 0 && pos[1] == 5 && myTeam)
            endGame(true);
        if(pos[0] == 5 && pos[1] == 0 && !myTeam)
            endGame(false);
    }

    public void endGame(boolean winner)
    {
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 6; j++) {
                tiles[i][j].setBackground(null);
                tiles[i][j].setText("");
                tiles[i][j].setClickable(false);
            }
        }
        turnIndicator.setText("Good Game!");
        gameBoard.setBackgroundResource(R.drawable.background_pixelated);
        updateUserWins(winner);
        gameRoom.removeEventListener(moveListener);
        if(winner)
            removeGameRoom(gameRoom);
        GameEndDialog gameEndDialog = new GameEndDialog(GameActivity.this);
        gameEndDialog.startGameEndDialog(winner);
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

    public void enemyMove(Troop enemyTroop, int[] newPos)
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
        attackCycleVisualized();
        checkIfWinByDeath();
        turnIndicator.setText("it's your turn");
        turn = true;
    }

    public void readMovesFromGameRoom()//gets the move the other person did
    {
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
            tile.setBackground(troop.getImageSRC());
            tile.setText(hpSymbol+troop.getHp()+" ");
        }
        tiles[5][0].setBackgroundResource(R.drawable.figure_throne);
        tiles[0][5].setBackgroundResource(R.drawable.figure_throne);
    }

    @Override
    public void onBackPressed() {}//disables back press
}