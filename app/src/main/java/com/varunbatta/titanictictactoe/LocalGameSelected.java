package com.varunbatta.titanictictactoe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class LocalGameSelected extends Fragment {
    // TODO: Make this view better looking, fix the positions and the views in general

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.local_game_selected, container, false);

        final EditText opponentNameEditText = layout.findViewById(R.id.opponentName);

        Button playLocallyButton = layout.findViewById(R.id.localPlayButton);
        playLocallyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String opponentName = opponentNameEditText.getText().toString();
                PlayerSelector playerSelector = (PlayerSelector) getActivity();
                playerSelector.playGameLocally(opponentName);
            }
        });

        return layout;
    }

}
