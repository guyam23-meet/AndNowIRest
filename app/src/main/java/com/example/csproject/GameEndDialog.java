package com.example.csproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class GameEndDialog implements View.OnClickListener{
        private Activity activity;
        private AlertDialog dialog;
        public TextView backToHomePage;
        public TextView winnerText;
        GameEndDialog(Activity myActivity) {activity = myActivity;}

        void startGameEndDialog(boolean winner)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_game_end, null);
            winnerText = layout.findViewById(R.id.winner);
            winnerText.setText("You "+(winner?"Won":"Lost"));
            backToHomePage = layout.findViewById(R.id.back_to_home);
            backToHomePage.setOnClickListener(this);
            builder.setView(layout);
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();
        }
        void closeResignDialog() {dialog.dismiss();}

        @Override
        public void onClick(View view)
        {
            if(view==backToHomePage){
                closeResignDialog();
                activity.startActivity(new Intent(activity,HomePageActivity.class));}
        }
}

