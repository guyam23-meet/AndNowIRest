package com.example.csproject.Dialogs;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.csproject.Activities.GameActivity;
import com.example.csproject.Activities.HomePageActivity;
import com.example.csproject.R;

public class GameEndDialog implements View.OnClickListener {
    //the activity where you show the dialog
    private final GameActivity gameActivity;

    //the dialog itself
    private AlertDialog dialog;

    //the views
    private TextView backToHomePage;
    private TextView winnerText;
    //end of the views

    public GameEndDialog(GameActivity gameActivity) {this.gameActivity = gameActivity;}

    //dialog start functions
    public void startGameEndDialog(boolean winner)
    {
        //sets the layout
        LayoutInflater inflater = gameActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_game_end, null);

        //connects the views
        connectViews(winner, layout);

        //builds and shows the dialog
        buildAndShow(layout);
    }
    private void buildAndShow(View layout)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(gameActivity);
        builder.setView(layout);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }
    private void connectViews(boolean winner, View layout)
    {
        winnerText = layout.findViewById(R.id.winner);
        winnerText.setText("You " + (winner ? "Won" : "Lost"));
        backToHomePage = layout.findViewById(R.id.back_to_home);
        backToHomePage.setOnClickListener(this);
    }
    //end of dialog start functions

    //what to do when the dialog ends
    private void closeResignDialog()
    {
        gameActivity.stopBackgroundMusic();
        gameActivity.startActivity(new Intent(gameActivity, HomePageActivity.class));
        dialog.dismiss();
    }

    @Override
    public void onClick(View view)
    {
        if(view == backToHomePage)
            closeResignDialog();
    }
}

