package com.example.csproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
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

    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public LinearLayout gameBoard;
    public AppCompatButton[][] tiles;
    public HashMap<Integer, int[]> tilesPositions;
    public DatabaseReference gameRoom;
    public Boolean isHost;
    public TextView enemyName;
    public TextView enemyPlacement;
    public TextView myName;
    public TextView myPlacement;
    public TextView resignIcon;
    public Troop selectedTroop;
    public boolean turn;
    public ChildEventListener moveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CommonFunctions.fullscreenSetup(getWindow());
        setContentView(R.layout.activity_game);
        CommonFunctions.systemUiChangeManager(getWindow().getDecorView());

        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");
        mAuth = FirebaseAuth.getInstance();

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
        Drawable[] layers = {selectedTroop.getImageSRC(), new ColorDrawable(getColor(R.color.selected))};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        int[] troopPos = selectedTroop.getPosition();
        AppCompatButton tile = tiles[troopPos[0]][troopPos[1]];
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
        resignIcon = findViewById(R.id.resign_icon);
        resignIcon.setOnClickListener(this);
    }

    public void updateGamesPlayed()
    {
        CommonFunctions.getUserValues(database, mAuth, userValues ->
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
        });
    }

    public void setTilesAndPositions()
    {
        ConstraintLayout row;
        AppCompatButton tile;
        tiles = new AppCompatButton[6][6];
        tilesPositions = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            row = (ConstraintLayout) gameBoard.getChildAt(i);
            for(int j = 0; j < 6; j++) {
                tile = (AppCompatButton) row.getChildAt(j);
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
            ResignDialog resignDialog = new ResignDialog(GameActivity.this);
            resignDialog.startResignDialog();
            return;
        }
        int viewId = view.getId();
        for(int i = 0; i < 6; i++) {//checks if what was pressed is a tile
            for(int j = 0; j < 6; j++) {
                if(tilesPositions.get(viewId) != null) {
                    gameClick(tilesPositions.get(viewId));
                }
            }
        }
    }

    public void resign()
    {
        gameRoom.child("move").setValue("resign");
        endGame(false);
    }

    public void gameClick(int[] clickPos)
    {
        if(!turn)
            return;
        Troop clickedTroop = Troop.posToTroop[clickPos[0]][clickPos[1]];//clicked troop

        if(selectedTroop == null)//if there isnt a selected troop
        {
            if(clickedTroop != null && clickedTroop.getMyTeam()) {//and you pressed on a one of your troops
                selectedTroop = clickedTroop;//select it
                visualSelection(selectedTroop);
            }
            return;
        }
        //there is a troop selected
        boolean hasMoved = false;
        if(clickedTroop == null)//the place you picked is empty
        {
            int[] lastPos = selectedTroop.getPosition();
            if(selectedTroop.moveTo(clickPos))//attempt to move the selected troop
            {
                hasMoved = true;
                updateVisualsAfterMovement(selectedTroop, lastPos);
                checkIfOnThrone(true, clickPos);
                finishTurn(clickPos);
            }
        }
        else if(clickedTroop.getMyTeam() && selectedTroop instanceof Mage && !(clickedTroop instanceof Mage))//if the clicked troop is my troop and not a mage
            if(((Mage) selectedTroop).buffTroop(clickedTroop))//attempt to buff the clicked troop
                visualizeBuff(clickedTroop);
                finishTurn(clickPos);
        removeVisualSelection(selectedTroop, hasMoved);
        selectedTroop = null;
    }

    private void visualizeBuff(Troop clickedTroop)
    {
        Drawable[] layers = {clickedTroop.getImageSRC(), new ColorDrawable(getColor(R.color.buffed))};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        clickedTroop.setImageSRC(layerDrawable);
        int[] troopPos = clickedTroop.getPosition();
        AppCompatButton tile = tiles[troopPos[0]][troopPos[1]];
        tile.setBackground(layerDrawable);
    }

    private void removeVisualSelection(Troop selectedTroop, boolean hasMoved)
    {
        if(!hasMoved) {
            int[] pos = selectedTroop.getPosition();
            AppCompatButton tile = tiles[pos[0]][pos[1]];
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
        attackCycleVisualized();
        checkIfWinByDeath();
        submitMoveToDatabase(clickPos);
        turn = false;
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
        Troop.attackCycle();
        for(Troop troop : Troop.troopMap.values()) {
            if(!troop.getAlive()) {
                int[] deadPos = troop.getPosition();
                tiles[deadPos[0]][deadPos[1]].setBackground(Drawable.createFromPath("@drawable/custom_view_black_border"));
            }
        }
    }

    public void updateVisualsAfterMovement(Troop movedTroop, int[] oldPos)
    {
        tiles[oldPos[0]][oldPos[1]].setBackground(Drawable.createFromPath("@drawable/custom_view_black_border"));
        int[] newPos = movedTroop.getPosition();
        tiles[newPos[0]][newPos[1]].setBackground(movedTroop.getImageSRC());
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
                tiles[i][j].setClickable(false);
            }
        }
        updateUserWins(winner);
        gameRoom.child("move").removeEventListener(moveListener);
        CommonFunctions.removeGameRoom(gameRoom);
        GameEndDialog gameEndDialog = new GameEndDialog(GameActivity.this);
        gameEndDialog.startGameEndDialog(winner);
    }


    private void updateUserWins(boolean winner)
    {
        String uId = mAuth.getCurrentUser().getUid();
        int score = winner ? 1 : 0;
        CommonFunctions.getUserValues(database, mAuth, userValues ->
        {
            int wins = Integer.parseInt(userValues[3]);
            database.getReference("users/" + uId).child("wins").setValue((wins + score) + "");
        });
    }

    public void enemyMove(Troop enemyTroop, int[] newPos)
    {
        Troop movePositionTroop = Troop.posToTroop[newPos[0]][newPos[1]];
        if(movePositionTroop != null)//the troop is a mage that buffed an enemy troop in the given position
            ((Mage) enemyTroop).buffTroop(movePositionTroop);

        else {//just move the troop
            int[] oldPos = enemyTroop.getPosition();
            enemyTroop.moveTo(newPos);
            updateVisualsAfterMovement(enemyTroop, oldPos);
            checkIfOnThrone(false, newPos);
        }
        attackCycleVisualized();
        turn = true;
    }

    public void readMovesFromGameRoom()//gets the move the other person did
    {
        moveListener = gameRoom.child("move").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                onChildChanged(snapshot, previousChildName);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                String moveValue = snapshot.getValue().toString();
                if(moveValue.equals("resign"))
                    endGame(true);
                String movedTroopId = moveValue.substring(0, 2);
                String movedTroopPos = moveValue.substring(4, 5);
                String reversedTroopId = "e" + movedTroopId.substring(1);//replaces id from other players screen to the corresponding one in this screen
                int y = Character.getNumericValue(movedTroopPos.charAt(0));
                int x = Character.getNumericValue(movedTroopPos.charAt(1));
                int[] reversedPos = new int[]{5 - y, 5 - x};//replaces position from other players screen to the corresponding one in this screen
                Troop enemyTroop = Troop.troopMap.get(reversedTroopId);
                enemyMove(enemyTroop, reversedPos);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
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
            tiles[troopPos[0]][troopPos[1]].setBackground(troop.getImageSRC());
        }
        tiles[5][0].setBackgroundResource(R.drawable.figure_throne);
        tiles[0][5].setBackgroundResource(R.drawable.figure_throne);
        ;
    }

    @Override
    public void onBackPressed()
    {
    }//disables back press
}