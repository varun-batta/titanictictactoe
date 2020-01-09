package com.varunbatta.titanictictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends Activity {

	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		
		context = MainMenu.this;

        Button instructionsButton = (Button) findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent instructions = new Intent(context, Instructions.class);
                startActivity(instructions);
			}
		});

        Button level1Button = (Button) findViewById(R.id.level1Button);
        level1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelector(1);
            }
        });

        Button level2Button = (Button) findViewById(R.id.level2Button);
        level2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelector(2);
            }
        });

        Button level3Button = (Button) findViewById(R.id.level3Button);
        level3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelector(3);
            }
        });

        Button level4Button = (Button) findViewById(R.id.level4Button);
        level4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelector(4);
            }
        });

        Button currentGamesButton = findViewById(R.id.currentGamesButton);
        currentGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent currentGamesIntent = new Intent(context, CurrentGames.class);
                startActivity(currentGamesIntent);
            }
        });
	}
	
	public void playerSelector(int level) {
		if (level == 3 || level == 4) {
            Toast.makeText(context, "Level " + level + " is not available yet, sorry", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent playerSelectorIntent = new Intent(context, PlayerSelector.class);
		playerSelectorIntent.putExtra("Level", level);
		startActivity(playerSelectorIntent);
//		this.finish();
	}
}
