package com.example.csproject;

import static com.example.csproject.CommonFunctions.getUserValues;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class StatsFragment extends Fragment {

    public TextView name;
    public TextView wins;
    public TextView gamesPlayed;
    public TextView winRate;
    public TextView placement;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View statsFragmentLayout = inflater.inflate(R.layout.fragment_stats, container, false);

        name = statsFragmentLayout.findViewById(R.id.tv_name_fragment_stats);
        wins = statsFragmentLayout.findViewById(R.id.tv_winsValue_fragment_stats);
        gamesPlayed = statsFragmentLayout.findViewById(R.id.tv_gamesPlayedValue_fragment_stats);
        winRate = statsFragmentLayout.findViewById(R.id.tv_winRateValue_fragment_stats);
        placement = statsFragmentLayout.findViewById(R.id.tv_placementValue_fragment_stats);

        updateViewsFromUser();

        return statsFragmentLayout;
    }

    public void updateViewsFromUser()
    {
        getUserValues(userValues ->
        {
            String userName = userValues[2];
            String userWins = userValues[3];
            String userGamesPlayed = userValues[4];

            name.setText(userName);
            wins.setText(userWins);
            gamesPlayed.setText(userGamesPlayed);
            winRate.setText(Integer.valueOf(userWins) * 100 / Math.max(Integer.valueOf(userGamesPlayed),1) + "%");
            placement.setText(String.valueOf(Integer.valueOf(userWins) * 2 - Integer.valueOf(userGamesPlayed)));
        });

    }
}
