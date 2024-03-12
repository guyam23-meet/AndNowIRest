package com.example.csproject.CommonUtilities;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Consumer;

public class DatabaseUtilities {

    //the database and authentication, which are imported in every page
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static FirebaseDatabase database = FirebaseDatabase.getInstance("https://csproject-99c38-default-rtdb.europe-west1.firebasedatabase.app/");

    public static void getUserValues(Consumer<String[]> onCallBack)
    //takes a database and the authentication and returns an array of Strings where:
    //0 - userId
    //1 - email
    //2 - name
    //3 - wins
    //4 - games played
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
                onCallBack.accept(values);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    //deletes the gameRoom
    public static void removeGameRoom(DatabaseReference gameRoom)
    {
        gameRoom.removeValue().addOnCompleteListener(task -> {
            if(gameRoom.getKey()!=null)
                gameRoom.removeValue();
        });
    }
}
