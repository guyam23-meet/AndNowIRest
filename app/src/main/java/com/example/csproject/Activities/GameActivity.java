package com.example.csproject.Activities;

import static com.example.csproject.CommonUtilities.DatabaseUtilities.database;
import static com.example.csproject.CommonUtilities.FullScreenUtilities.fullscreenSetup;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.getUserValues;
import static com.example.csproject.CommonUtilities.FullScreenUtilities.systemUiChangeManager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.csproject.GameEngine.GameEngine;
import com.example.csproject.R;
import com.example.csproject.Dialogs.ResignDialog;
import com.example.csproject.Services.musicService;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    //visuals outside of the game
    private TextView enemyName;
    private TextView enemyPlacement;
    private TextView myName;
    private TextView myPlacement;
    private TextView resignIcon;
    public GameEngine gameEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_game);
        systemUiChangeManager(getWindow().getDecorView());

        connectViews();

        Bundle bundle = getIntent().getExtras();
        String roomRef = bundle.getString("roomRef");

        gameEngine = new GameEngine(roomRef,findViewById(R.id.game_board),this, findViewById(R.id.turn_indicator));

        updateGuestAndHostInfoLines();
        updateGamesPlayed();

        startService(new Intent(GameActivity.this, musicService.class));
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
    private void updateGamesPlayed()
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
        gameEngine.roomManager.getGameRoomValues(roomValues ->
        {
            String hostName = roomValues[1];
            String guestName = roomValues[2];
            String hostPlacement = roomValues[3];
            String guestPlacement = roomValues[4];
            if(gameEngine.roomManager.isHost) {
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

    public void stopBackgroundMusic()
    {
        stopService(new Intent(GameActivity.this, musicService.class));
    }

    @Override
    public void onClick(View view)
    {
        if(view == resignIcon) {
            onResignPressed();
            return;
        }
        int viewId = view.getId();
        if(gameEngine.userInputManager.tilesPositions.get(viewId) != null){//if you pressed a tile
            gameEngine.initializeGameClick(viewId);

        }
    }
    @Override
    public void onBackPressed() {}//disables back press
    @Override
    protected void onDestroy()
    {//when you leave you resign
        super.onDestroy();
        gameEngine.resign();
    }
}