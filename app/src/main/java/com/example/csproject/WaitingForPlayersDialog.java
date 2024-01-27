package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;

public class WaitingForPlayersDialog {
    private Fragment fragment;
    private AlertDialog dialog;
    WaitingForPlayersDialog(Fragment myFragment) {fragment = myFragment;}

    void startWaitingDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        LayoutInflater inflater = fragment.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_waiting_for_players,null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }
    void closeWaitingDialog() {dialog.dismiss();}
}
