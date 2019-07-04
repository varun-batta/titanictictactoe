package com.varunbatta.titanictictactoe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class LocalGameSelected extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
