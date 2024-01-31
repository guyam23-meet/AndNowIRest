package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;

public class WaitingForPlayersDialog implements View.OnClickListener{
    private Fragment fragment;
    private AlertDialog dialog;
    private DatabaseReference gameRoom;
    private TextView cancelGame;
    WaitingForPlayersDialog(Fragment myFragment,DatabaseReference gameRoom)
    {
        this.fragment = myFragment;
        this.gameRoom = gameRoom;
    }

    void startWaitingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        LayoutInflater inflater = fragment.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_waiting_for_players,null);
        cancelGame = layout.findViewById(R.id.cancel_game);
        cancelGame.setOnClickListener(this);
        builder.setView(layout);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }
    void closeWaitingDialog() {dialog.dismiss();}

    @Override
    public void onClick(View view)
    {
        if(view==cancelGame)
        {
            ((HomeFragment)fragment).cancelGame(gameRoom);
            closeWaitingDialog();
        }
    }
}
