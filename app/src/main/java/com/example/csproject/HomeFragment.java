package com.example.csproject;

import static com.example.csproject.CommonFunctions.database;
import static com.example.csproject.CommonFunctions.getUserValues;
import static com.example.csproject.CommonFunctions.mAuth;

import android.content.Intent;
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

    public boolean eventListenerHandler;
    public ValueEventListener hostWaitingListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View homeFragmentLayout = inflater.inflate(R.layout.fragment_home, container, false);


        name = homeFragmentLayout.findViewById(R.id.tv_name_fragment_home);
        placement = homeFragmentLayout.findViewById(R.id.tv_placement_fragment_home);
        play = homeFragmentLayout.findViewById(R.id.btn_play_fragment_home);

        play.setOnClickListener(this);

        updateViewsFromUser();
        return homeFragmentLayout;
    }

    public void updateViewsFromUser()
    {
        getUserValues(userValues ->
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
        if (view == play) {
            gameSetUpManager();
            play.setClickable(false);
        }
    }

    //game setup functions
    public void gameSetUpManager()
    {
        DatabaseReference games = database.getReference("games");
        games.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.hasChildren())
                {
                    for (DataSnapshot room: snapshot.getChildren()) {
                        if(room.getKey().equals(mAuth.getCurrentUser().getUid()))//prevents joining from same user another device
                            return;
                    }
                    for (DataSnapshot room: snapshot.getChildren())
                    {
                        if (!room.hasChild("guest"))
                        {
                            joinGame(games.child(room.getKey()));
                            return;
                        }
                    }
                }
                openGame(games);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    public void joinGame(DatabaseReference room)
    {
        getUserValues(userValues -> {
            String guestName = userValues[2];
            int guestWins = Integer.valueOf(userValues[3]);
            int guestGamesPlayed = Integer.valueOf(userValues[4]);

            room.child("guest").setValue(guestName);
            room.child("guest_placement").setValue(""+(2*guestWins-guestGamesPlayed));
            Bundle bundle = new Bundle();
            bundle.putString("roomRef","games/"+room.getKey());
            Intent i = new Intent(getActivity(), GameActivity.class).putExtras(bundle);
            startActivity(i);
        });

    }
    public void openGame(DatabaseReference games)
    {
        getUserValues(userValues ->
        {
            String hostId = userValues[0];
            String hostName = userValues[2];
            int hostWins = Integer.valueOf(userValues[3]);
            int hostGamesPlayed = Integer.valueOf(userValues[4]);

            DatabaseReference room = games.child(hostId);

            room.child("host").setValue(hostName);
            room.child("host_placement").setValue(""+(2*hostWins-hostGamesPlayed));

            waitForPlayers(room);
        });
    }
    public void waitForPlayers(DatabaseReference room)
    {
        WaitingForPlayersDialog waitDialog = new WaitingForPlayersDialog(HomeFragment.this,room);
        waitDialog.startWaitingDialog();
        eventListenerHandler = false;
        hostWaitingListener = new ValueEventListener() {//this needs to be accessed by cancel
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!eventListenerHandler) {
                    eventListenerHandler  = true;
                    return;
                }
                waitDialog.closeWaitingDialog();
                Intent i = new Intent(getActivity(), GameActivity.class).putExtra("roomRef","games/"+room.getKey());
                getActivity().startActivity(i);
                room.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        room.addValueEventListener(hostWaitingListener);
    }
    //end game functions
    public void cancelGame(DatabaseReference gameRoom)
    {
        gameRoom.removeEventListener(hostWaitingListener);
        CommonFunctions.removeGameRoom(gameRoom);
        play.setClickable(true);
    }
}
