package com.example.csproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment implements View.OnClickListener {

    public TextView name;
    public TextView placement;
    public Button play;
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View homeFragmentLayout = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        name = homeFragmentLayout.findViewById(R.id.tv_name_fragment_home);
        placement = homeFragmentLayout.findViewById(R.id.tv_placement_fragment_home);
        play = homeFragmentLayout.findViewById(R.id.btn_play_fragment_home);

        play.setOnClickListener(this);

        updateViewsFromUser();

        return homeFragmentLayout;
    }

    public void updateViewsFromUser()
    {
        CommonFunctions.getUserValues(database, mAuth, userValues ->
        {
            String userName = userValues[2];
            String userWins = userValues[3];
            String userGamesPlayed = userValues[4];

            name.setText(userName);
            placement.setText(String.valueOf(Integer.valueOf(userWins) * 2 - Integer.valueOf(userGamesPlayed)));
        });
    }
    @Override
    public void onClick(View view)
    {
        if (view == play)
            gameSetUpManager();
    }

    //game setup functions
    public void gameSetUpManager()
    {
        DatabaseReference games = database.getReference("Games");
        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.hasChildren())
                {
                    for (DataSnapshot room : snapshot.getChildren())
                    {
                        if (!room.hasChild("Guest"))
                        {
                            joinRoom(games.child(room.getKey()));
                            return;
                        }}}
                openRoom(games);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void joinRoom(DatabaseReference room)
    {

    }
    public void openRoom(DatabaseReference games)
    {
        CommonFunctions.getUserValues(database, mAuth, userValues->
        {
            String hostEmail = userValues[1];
            String hostName = userValues[2];

            DatabaseReference room = games.child(hostEmail);

            room.child("host").setValue(hostName);
            room.child("turn").setValue(true);

            waitForPlayers(room);
        });
    }
    //add a loading screen that disappears when a game is found
    public void waitForPlayers(DatabaseReference room)
    {
        //add loading screen
        room.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //disable the loading screen

                //go to the game page
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    //end game functions

}
