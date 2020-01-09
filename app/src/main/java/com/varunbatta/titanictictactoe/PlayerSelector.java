package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PlayerSelector extends AppCompatActivity {

    static LinearLayout viewGroup;
    static int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player_selector);

        level = getIntent().getIntExtra("Level", 1);

//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.height = 1200;
//        params.width = getWindow().getAttributes().width;
//
//        this.getWindow().setAttributes(params);

        viewGroup = findViewById(R.id.playerSelectorLayout);

        TabLayout multiplayerOptions = findViewById(R.id.multiplayerOptionsTabLayout);
        ViewPager multiplayerOptionsPager = findViewById(R.id.multiplayerOptionsPager);
        multiplayerOptionsPager.setAdapter(new MultiplayerOptionsAdapter(getSupportFragmentManager()));

        multiplayerOptions.setupWithViewPager(multiplayerOptionsPager);
    }

    private class MultiplayerOptionsAdapter extends FragmentPagerAdapter {
        public MultiplayerOptionsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new LocalGameSelected();
                case 1: return new FacebookGameSelected();
            }
            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return getResources().getString(R.string.localGameButton);
                case 1: return getResources().getString(R.string.facebookGameButton);
            }
            return null;
        }
    }

    public void playGameLocally(String opponentName) {
        EditText yourNameEditText = findViewById(R.id.yourNameToFill);

        Intent board = new Intent(getApplicationContext(), Board.class);
        board.putExtra("Level", this.level);
        board.putExtra("Player 1 Name", yourNameEditText.getText().toString());
        board.putExtra("Player 2 Name", opponentName);
        board.putExtra("Caller", "MainMenu");
        board.putExtra("My Turn", true);
        board.putExtra("Finished", false);
        board.putExtra("Multiplayer", false);
        startActivity(board);
//        this.finish();
    }
}
