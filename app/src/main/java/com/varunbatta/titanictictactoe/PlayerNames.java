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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerNames extends Activity {

	LinearLayout playerNamesLayout;
	EditText player1;
	EditText player2;
	TextView playerNames;
	TextView instructions;
	
	public static String player1name;
	public static String player2name;
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playernames);
		
		context = getApplicationContext();
		
		playerNamesLayout = (LinearLayout) findViewById(R.id.playerNamesLayout);
		playerNamesLayout.setBackgroundColor(Color.rgb(0, 153, 153));
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 5);
		
		playerNames = new TextView(context);
		playerNames.setText("Player Names");
		playerNames.setTextSize(40);
		playerNames.setTextColor(Color.BLACK);
		playerNames.setGravity(Gravity.CENTER_HORIZONTAL);
		playerNamesLayout.addView(playerNames);
		
		instructions = new TextView(context);
		instructions.setText("Please enter the two player names below:");
		instructions.setTextSize(20);
		instructions.setTextColor(Color.BLACK);
		instructions.setGravity(Gravity.CENTER_HORIZONTAL);
		playerNamesLayout.addView(instructions);
		
		player1 = new EditText(context);
		player1.setHint("Player 1");
		player1.setHintTextColor(Color.DKGRAY);
		player1.setTextColor(Color.BLACK);
		player1.setBackgroundColor(Color.WHITE);
		playerNamesLayout.addView(player1, layoutParams);
		
		player2 = new EditText(context);
		player2.setHint("Player 2");
		player2.setHintTextColor(Color.DKGRAY);
		player2.setTextColor(Color.BLACK);
		player2.setBackgroundColor(Color.WHITE);
		playerNamesLayout.addView(player2, layoutParams);
		
		Button ok = new Button(context);
		ok.setText("OK");
		ok.setTextSize(30);
		ok.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
		        frameChange(v);
			}
		});
		playerNamesLayout.addView(ok);
		
	}
	
	public void frameChange(View v){
		player1name = player1.getText().toString();
		player2name = player2.getText().toString();
		
		
		Button okconfirmed = (Button) v;
		if(!okconfirmed.isSelected()){
			if(player1name.matches("")||player2name.matches("")){
				Toast warning = Toast.makeText(context, "Sorry, but you must fill in the names of both players!", Toast.LENGTH_SHORT);
				warning.show();
			} else{
				Intent menu = new Intent(context, LevelMenu.class);
				menu.putExtra("Mulitplayer", false);
				menu.putExtra("Caller", "PlayGame");
				startActivity(menu);
			}
		}
	}
}
