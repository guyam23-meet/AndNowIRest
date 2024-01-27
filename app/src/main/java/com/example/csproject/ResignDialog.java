package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResignDialog implements View.OnClickListener{
    private Activity activity;
    private AlertDialog dialog;
    public TextView resignYes;
    public TextView resignNo;
    ResignDialog(Activity myActivity) {activity = myActivity;}

    void startResignDialog(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_resign, null);
        resignYes = layout.findViewById(R.id.resign_yes);
        resignNo = layout.findViewById(R.id.resign_no);

        resignYes.setOnClickListener(this);
        resignNo.setOnClickListener(this);

        builder.setView(layout);
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }
    void closeResignDialog() {dialog.dismiss();}

    @Override
    public void onClick(View view)
    {
        if(view==resignNo)
            closeResignDialog();
        if(view==resignYes)
        {

        }
    }
}
