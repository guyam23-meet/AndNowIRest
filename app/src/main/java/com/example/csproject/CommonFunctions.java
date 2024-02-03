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

import java.util.EventListener;

public class CommonFunctions {
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static FirebaseDatabase database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");
    //takes a database and the authentication and returns an array of Strings where:
    //0 - userId
    //1 - email
    //2 - name
    //3 - wins
    //4 - games played
    public static void getUserValues(ICallBack iCallBack)
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
    public static void removeGameRoom(DatabaseReference gameRoom)
    {
        gameRoom.removeValue().addOnCompleteListener(task -> {
            if(gameRoom.getKey()!=null)
                gameRoom.removeValue();
        });
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
