package com.example.csproject;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CommonFunctions {
    //takes a database and the authentication and returns an array of Strings where:
    //0 - userId
    //1 - email
    //2 - name
    //3 - wins
    //4 - games played
    public static void getUserValues(FirebaseDatabase database, FirebaseAuth mAuth, ICallBack iCallBack)
    {
        String[] values = {"0","0","0","0","0"};

        FirebaseUser userAuth = mAuth.getCurrentUser();

        values[0] = userAuth.getUid();
        values[1] = userAuth.getEmail();

        DatabaseReference userReference = database.getReference("users/" + values[0]);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                values[2] = snapshot.child("name").getValue().toString();
                values[3] = snapshot.child("wins").getValue().toString();
                values[4] = snapshot.child("games_played").getValue().toString();
                iCallBack.onCallBack(values);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}

        });

    }

    public static void getGameRoomValues(DatabaseReference gameRoom, ICallBack iCallBack)
    {
        String[] values = new String[7];
        values[0] = gameRoom.getKey();
        gameRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                values[1] = snapshot.child("host").getValue().toString();
                values[2] = snapshot.child("host_placement").getValue().toString();
                values[3] = snapshot.child("guest").getValue().toString();
                values[4] = snapshot.child("guest_placement").getValue().toString();
                values[5] = snapshot.child("turn").getValue().toString();
                values[6] = snapshot.child("move").getValue().toString();
                iCallBack.onCallBack(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    public static Boolean checkIsHost(DatabaseReference gameRoom,FirebaseAuth mAuth)
    {
        return gameRoom.getKey() == mAuth.getCurrentUser().getEmail();
    }

    //fullscreen functions
    public static int hideSystemBars()
    {
        return View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
    }
    public static void systemUiChangeManager(View decorView)
    {
        decorView.setOnSystemUiVisibilityChangeListener(i ->
        {
            if (i == 0)
                decorView.setSystemUiVisibility(hideSystemBars());
        });
    }
    public static void fullscreenSetup(Window window)
    {
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(hideSystemBars());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
    //fullscreen functions
}
