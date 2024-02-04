package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;

public class WaitingForPlayersDialog implements View.OnClickListener {
    //the fragment where you show the dialog
    private final HomeFragment homeFragment;

    //the dialog itself
    private AlertDialog dialog;

    //the game you are waiting to start
    private final DatabaseReference gameRoom;

    //the view in the dialog
    private TextView cancelGame;

    public WaitingForPlayersDialog(HomeFragment homeFragment, DatabaseReference gameRoom)
    {
        this.homeFragment = homeFragment;
        this.gameRoom = gameRoom;
    }

    //dialog start functions
    public void startWaitingDialog()
    {
        //sets the layout
        LayoutInflater inflater = homeFragment.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_waiting_for_players, null);

        //connects the views
        connectViews(layout);

        //builds and shows the Dialog
        buildAndShow(layout);
    }
    private void buildAndShow(View layout)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(homeFragment.getContext());
        builder.setView(layout);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }
    private void connectViews(View layout)
    {
        cancelGame = layout.findViewById(R.id.cancel_game);
        cancelGame.setOnClickListener(this);
    }
    //end of dialog start functions

    //what to do when the dialog ends
    public void closeWaitingDialog()
    {
        dialog.dismiss();
    }

    @Override
    public void onClick(View view)
    {
        if(view == cancelGame) {
            homeFragment.cancelGame(gameRoom);
            closeWaitingDialog();
        }
    }
}
