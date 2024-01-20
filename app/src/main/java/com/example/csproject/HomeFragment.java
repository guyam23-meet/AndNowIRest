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

public class HomeFragment extends Fragment {
    public TextView name_home;
    public TextView placement_home;
    public Button play_home;
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View home_fragment_layout = inflater.inflate(R.layout.fragment_home,container,false);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

        name_home = home_fragment_layout.findViewById(R.id.name_home);

        placement_home = home_fragment_layout.findViewById(R.id.placement_home);

        play_home = home_fragment_layout.findViewById(R.id.play_home);

        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference userId_reference = database.getReference("users").child(userId);

        userId_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name_value;

                String games_played_value;

                String wins_value;


                if(snapshot.hasChild("name")) {
                    name_value = snapshot.child("name").getValue().toString();
                    name_home.setText(name_value);
                }
                if(snapshot.hasChild("wins") && snapshot.hasChild("games_played")) {

                    wins_value = snapshot.child("wins").getValue().toString();

                    games_played_value = snapshot.child("games_played").getValue().toString();

                    placement_home.setText(String.valueOf(Integer.valueOf(wins_value)*2-Integer.valueOf(games_played_value)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return home_fragment_layout;
    }
}
