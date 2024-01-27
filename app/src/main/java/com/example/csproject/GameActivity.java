package com.example.csproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public LinearLayout gameBoard;
    public AppCompatButton[][] tiles;
    public HashMap<Integer, Integer[]> tilesPositions;
    public DatabaseReference gameRoom;
    public Boolean isHost;
    public TextView enemyName;
    public TextView enemyPlacement;
    public TextView myName;
    public TextView myPlacement;
    public TextView resignIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonFunctions.fullscreenSetup(getWindow());
        setContentView(R.layout.activity_game);
        CommonFunctions.systemUiChangeManager(getWindow().getDecorView());

        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");
        mAuth = FirebaseAuth.getInstance();

        gameBoard = findViewById(R.id.game_board);
        setTilesAndPositions();

        Bundle bundle = getIntent().getExtras();
        String roomRef = bundle!=null?(String)bundle.get("roomRef"):"games";
        gameRoom = database.getReference(roomRef);
        isHost = checkIsHost();

        enemyName = findViewById(R.id.tv_enemy_name_activity_game);
        enemyPlacement = findViewById(R.id.tv_enemy_placement_activity_game);
        myName = findViewById(R.id.tv_name_activity_game);
        myPlacement = findViewById(R.id.tv_placement_activity_game);
        resignIcon = findViewById(R.id.resign_icon);
        resignIcon.setOnClickListener(this);

        if(gameRoom.getKey()!="games") {
            updateGuestAndHostInfoLines();
            updateGamesPlayed();
        }
    }
    public void updateGamesPlayed()
    {
        CommonFunctions.getUserValues(database, mAuth, userValues ->
        {
            String userId = userValues[0];
            int userGamesPlayed = Integer.valueOf(userValues[4]);
            database.getReference("users/"+userId+"/games_played").setValue(""+(userGamesPlayed+1));
        });
    }
    private void updateGuestAndHostInfoLines()
    {
        gameRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String hostName = snapshot.child("host").getValue().toString();
                String guestName = snapshot.child("guest").getValue().toString();
                String hostPlacement = snapshot.child("host_placement").getValue().toString();
                String guestPlacement = snapshot.child("guest_placement").getValue().toString();
                if(isHost)
                {
                    myName.setText(hostName);
                    enemyName.setText(guestName);
                    myPlacement.setText(hostPlacement);
                    enemyPlacement.setText(guestPlacement);
                }
                else
                {
                    myName.setText(guestName);
                    enemyName.setText(hostName);
                    myPlacement.setText(guestPlacement);
                    enemyPlacement.setText(hostPlacement);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void setTilesAndPositions()
    {
        ConstraintLayout row;
        AppCompatButton tile;
        tiles = new AppCompatButton[6][6];
        tilesPositions = new HashMap<Integer, Integer[]>();
        for(int i=0;i<6;i++)
        {
            row = (ConstraintLayout) gameBoard.getChildAt(i);
            for(int j = 0; j < 6; j++)
            {
                tile = (AppCompatButton) row.getChildAt(j);
                tiles[i][j] = tile;
                //tile.setOnClickListener(this);
                tilesPositions.put(tile.getId(), new Integer[] {i, j});
            }
        }
    }
    public Boolean checkIsHost()
    {
        if(gameRoom.getKey()==mAuth.getCurrentUser().getEmail())
            return true;
        return false;
    }
    @Override
    public void onClick(View view)
    {
        if(view==resignIcon)
        {
            ResignDialog resignDialog = new ResignDialog(GameActivity.this);
            resignDialog.startResignDialog(resignIcon);
        }
    }
}