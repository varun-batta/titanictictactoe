package com.varunbatta.titanictictactoe;

import com.varunbatta.titanictictactoe.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainMenu extends Activity {

	Context context;
	String buttonLabels [] = {"Play Game", "Wifi Game", "Instructions"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		
		context = getApplicationContext();
		
		LinearLayout menu = (LinearLayout) findViewById(R.id.mainMenuLayout);
		
		TextView welcome = new TextView(context);
		welcome.setText("Welcome!");
		welcome.setTextColor(Color.BLACK);
		welcome.setTextSize(40);
		welcome.setGravity(Gravity.CENTER_HORIZONTAL);
		
		TextView title = new TextView(context);
		title.setText("Main Menu");
		title.setTextColor(Color.BLACK);
		title.setTextSize(30);
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		
		menu.setBackgroundColor(Color.rgb(0, 153, 153));
		menu.addView(welcome);
		menu.addView(title);
		
		Button instructions = new Button(context);
		instructions.setText("Instructions");
		instructions.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button instructionsButton = (Button) v;
				String instructionsHeading = instructionsButton.getText().toString();
				buttonListener(instructionsHeading);
				
			}
		});
		menu.addView(instructions);
		
		Button playGame = new Button(context);
		playGame.setText("Pass-by-Pass Game");
		playGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button playGameButton = (Button) v;
				String playGameHeading = playGameButton.getText().toString();
				buttonListener(playGameHeading);
				
			}
		});
		menu.addView(playGame);
		
		Button wifiGame = new Button(context);
		wifiGame.setText("WiFi Game");
		wifiGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button wifiGameButton = (Button) v;
				String wifiGameHeading = wifiGameButton.getText().toString();
				buttonListener(wifiGameHeading);
				
 			}
		});
		menu.addView(wifiGame);
	}
	
	public void buttonListener(String buttonText) {
		switch(buttonText) {
		case "Instructions":
//			Intent instructions = new Intent(context, LevelMenu.class);
//			instructions.putExtra("Multiplayer", false);
//			instructions.putExtra("Caller", "MainMenu");
//			instructions.putExtra("Instructions", true);
			Intent instructions = new Intent(context, Instructions.class);
			startActivity(instructions);
			break;
		case "Pass-by-Pass Game":
			Intent playGameIntent = new Intent(context, LevelMenu.class);
			playGameIntent.putExtra("Multiplayer", false);
			playGameIntent.putExtra("Caller", "MainMenu");
			playGameIntent.putExtra("Instructions", false);
			startActivity(playGameIntent);
			break;
		case "WiFi Game":
			Intent wifiGameIntent = new Intent(context, LevelMenu.class);
			wifiGameIntent.putExtra("Multiplayer", true);
			wifiGameIntent.putExtra("Caller", "MainMenu");
			wifiGameIntent.putExtra("Instructions", false);
			startActivity(wifiGameIntent);
			break;
		}
	}
}
