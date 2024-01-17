package com.example.csproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatsFragment extends Fragment {
    public TextView name;
    public TextView wins;
    public TextView games_played;
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    public TextView winRate;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View stats_fragment_layout = inflater.inflate(R.layout.fragment_stats,container,false);

        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference userId_reference = database.getReference("users").child(userId);

       name = stats_fragment_layout.findViewById(R.id.user_name_stats);

       wins = stats_fragment_layout.findViewById(R.id.wins_stats);

       games_played = stats_fragment_layout.findViewById(R.id.games_played_stats);

       winRate = stats_fragment_layout.findViewById(R.id.win_rate_stats);

       userId_reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               String games_played_value = snapshot.child("games_played").getValue().toString();
               String wins_value = snapshot.child("wins").getValue().toString();
               name.setText(snapshot.child("name").getValue().toString());

               if(snapshot.hasChild("wins"))
                   wins.setText(" - Wins: "+wins_value);

               if(snapshot.hasChild("games_played"))
                   games_played.setText(" - Games Played: "+games_played_value);

               if(snapshot.hasChild("wins") && snapshot.hasChild("games_played"))
                   winRate.setText(" - Win Rate: "+ Integer.valueOf(wins_value)*100/Integer.valueOf(games_played_value)+"%");

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    return stats_fragment_layout;
    }
}
