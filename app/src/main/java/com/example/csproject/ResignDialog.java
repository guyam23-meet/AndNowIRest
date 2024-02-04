package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResignDialog implements View.OnClickListener{
    //the activity you inflate it in
    private final GameActivity gameActivity;

    //the dialog itself
    private AlertDialog dialog;

    //yes and no buttons
    private TextView resignYes;
    private TextView resignNo;
    //end of yes and no button

    public ResignDialog(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    //dialog start functions
    public void startResignDialog()
    {
        //sets the layout
        LayoutInflater inflater = gameActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_resign, null);

        //connects the views
        connectViews(layout);

        //builds and shows the Dialog
        buildAndShow(layout);
    }
    private void connectViews(View layout)
    {
        resignYes = layout.findViewById(R.id.resign_yes);
        resignNo = layout.findViewById(R.id.resign_no);
        resignYes.setOnClickListener(this);
        resignNo.setOnClickListener(this);
    }
    private void buildAndShow(View layout)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(gameActivity);
        builder.setOnCancelListener(onDialogCanceled -> closeResignDialog());//makes it so when you cancel the dialog it closes it properly
        builder.setView(layout);
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }
    //end of dialog start functions

    //when ending the dialog
    private void closeResignDialog()
    {
        gameActivity.enableResignButton();
        dialog.dismiss();
    }

    @Override
    public void onClick(View view)
    {
        if(view==resignNo)
            closeResignDialog();
        if(view==resignYes){
            gameActivity.resign();
            closeResignDialog();
        }
    }
}
