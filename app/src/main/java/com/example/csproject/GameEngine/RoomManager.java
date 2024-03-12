package com.example.csproject.GameEngine;

import static com.example.csproject.CommonUtilities.DatabaseUtilities.database;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.mAuth;
import static com.example.csproject.CommonUtilities.DatabaseUtilities.removeGameRoom;

import androidx.annotation.NonNull;

import com.example.csproject.CommonUtilities.DatabaseUtilities;
import com.example.csproject.Troops.Troop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Consumer;

public class RoomManager {

    private final DatabaseReference gameRoom;
    private ValueEventListener moveListener;
    public boolean isHost;

    public RoomManager(String roomRef, IGameRoomRead iGameRoomRead)
    {
        this.gameRoom = database.getReference(roomRef);
        this.isHost = gameRoom.getKey().equals(mAuth.getCurrentUser().getUid());
        this.moveListener = getMoveListener(iGameRoomRead);
        gameRoom.addValueEventListener(moveListener);
    }
    public void submitMoveToDatabase(int[] clickPos, Troop selectedTroop, boolean isHost)
    {
        String troopId = selectedTroop.getId();
        String yx = "" + clickPos[0] + clickPos[1];
        String isHostLabel = isHost ? "h" : "g";
        gameRoom.child("move").setValue(troopId + "_" + yx + "_" + isHostLabel);
    }
    public void closeGame(boolean winner)
    {
        gameRoom.removeEventListener(moveListener);
        if(winner)
            removeGameRoom(gameRoom);
    }

    public interface IGameRoomRead {
        void onGameRoomRead(boolean resigned, String enemyTroopId, int[] reversedPos);
    }

    public void getGameRoomValues(Consumer<String[]> onCallBack)
    {
        String[] values = new String[7];
        values[0] = gameRoom.getKey();
        gameRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                values[1] = snapshot.child("host").getValue().toString();
                values[2] = snapshot.child("guest").getValue().toString();
                values[3] = snapshot.child("host_placement").getValue().toString();
                values[4] = snapshot.child("guest_placement").getValue().toString();
                if(snapshot.hasChild("move"))
                    values[6] = snapshot.child("move").getValue().toString();
                else
                    values[6] = "";
                onCallBack.accept(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        });
    }

    public void resign()
    {
        gameRoom.child("move").setValue("resign_" + (isHost ? 'h' : 'g'));
    }

    private ValueEventListener getMoveListener(IGameRoomRead iGameRoomRead)
    {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!snapshot.hasChild("move"))//both handles unexpected calls to DB and disables the function when it is declared
                    return;
                String moveValue = snapshot.child("move").getValue().toString();

                char hostId = isHost ? 'h' : 'g';
                if(hostId == moveValue.charAt(7))//check if its your action
                    return;

                String movedTroopPos = moveValue.substring(4, 6);
                String reversedTroopId = "e" + moveValue.substring(1, 3);
                //replaces id from other players screen to the corresponding one in this screen
                int y = Character.getNumericValue(movedTroopPos.charAt(0));
                int x = Character.getNumericValue(movedTroopPos.charAt(1));
                int[] reversedPos = new int[]{5 - y, 5 - x};//replaces position from other players screen to the corresponding one in this screen

                boolean resigned = false;
                if(moveValue.startsWith("resign"))
                    resigned = true;

                iGameRoomRead.onGameRoomRead(resigned, reversedTroopId, reversedPos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        };
    }
}
