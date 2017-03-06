package com.varunbatta.titanictictactoe;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.varunbatta.titanictictactoe.R;

public class Winner extends Activity implements ResultCallback<InitiateMatchResult> {

	Context context;
	boolean multiplayer;
	String matchId;
	String caller;
	boolean canRematch;
	String winnerName;
	
	GoogleApiClient client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("oC", "Winner onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.winner);
		
		Board.keys = new Hashtable<Integer, Button>(6561);
		Board.bottomPanel.removeAllViews();
		Board.boardLayout.removeAllViews();
		ButtonPressed.currentTurn = "";
		
		multiplayer = getIntent().getBooleanExtra("Multiplayer", true);
		matchId = getIntent().getStringExtra("Match ID");
		caller = getIntent().getStringExtra("Client");
		canRematch = getIntent().getBooleanExtra("Can Rematch", true);
		winnerName = getIntent().getStringExtra("Winner");
		
		if(caller != null) {
			switch(caller) {
			case "Index":
				client = Index.client;
				break;
			case "Current Games":
				client = CurrentGames.client;
				break;
			case "Level Menu":
				client = LevelMenu.client;
				break;
			}
		}
		
		context = getApplicationContext();
		
		RelativeLayout winnerLayout = (RelativeLayout) findViewById(R.id.winnerLayout);
		TextView congrats = (TextView) findViewById(R.id.congrats);
		LinearLayout bottomPanel = (LinearLayout) findViewById(R.id.bottomPanel);
		LinearLayout bottomPanel2 = (LinearLayout) findViewById(R.id.bottomPanel2);
		
//		TextView congrats = new TextView(context);
		if(winnerName != null) {
			if(winnerName.equals("Tie")) {
				congrats.setText("It's a Tie");
			} else {
				congrats.setText(winnerName + " WINS!!!\nCongrats!");
			}
		} else if(multiplayer) {
			congrats.setText("You've WON!!!\nCongrats!");
		} 
		congrats.setTextColor(Color.BLACK);
		congrats.setTextSize(50);
//		congrats.setGravity(Gravity.CENTER);
		
		winnerLayout.setBackgroundColor(Color.rgb(0, 153, 153));
//		winnerLayout.addView(congrats);
		
//		LinearLayout bottomPanel = new LinearLayout(context);
		bottomPanel.setOrientation(LinearLayout.HORIZONTAL);
//		bottomPanel.setGravity(Gravity.BOTTOM);
		
		Button mainMenu = new Button(context);
		mainMenu.setText("Main Menu");
		mainMenu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
		bottomPanel.addView(mainMenu);
		
		Button viewGame = new Button(context);
		viewGame.setText("View Game");
		viewGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
		bottomPanel.addView(viewGame);
		
		Button newGame = new Button(context);
		newGame.setText("New Game");
		newGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
		bottomPanel.addView(newGame);
		
		Button newWifiGame = new Button(context);
		newWifiGame.setText("New WiFi Game");
		newWifiGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
		bottomPanel2.addView(newWifiGame);
		
		if(multiplayer) {
			Button rematch = new Button(context);
			rematch.setText("Rematch!");
			rematch.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Button button = (Button) v;
					String buttonText = button.getText().toString();
					bottomPanelListener(buttonText);
				}
			});
			bottomPanel2.addView(rematch);
		}
		
//		winnerLayout.addView(bottomPanel);
		
	}
	
	private void bottomPanelListener(String buttonText) {
//		Board.keys = new Hashtable<Integer, Button>(6561);	
//		Board.bottomPanel.removeAllViews();
//		Board.boardLayout.removeAllViews();
//		ButtonPressed.currentTurn = "";
		switch(buttonText) {
		case "Main Menu":
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			Intent mainMenu = new Intent(context, MainMenu.class);
			startActivity(mainMenu);
			this.finish();
			break;
		case "View Game":
			Intent board = new Intent(context, Board.class);
			board.putExtra("Level", Board.n);
			board.putExtra("My Turn", false);
			board.putExtra("Finished", true);
			board.putExtra("Caller", "Winner");
//			if(multiplayer) {
//				board.putExtra("On Going Match", Board.onGoingMatch);
//				board.putExtra("Match ID", matchId);
//			}
			startActivity(board);
			this.finish();
			break;
		case "New WiFi Game":
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			Intent wifiGameIntent = new Intent(context, LevelMenu.class);
			wifiGameIntent.putExtra("Multiplayer", true);
			wifiGameIntent.putExtra("Caller", "Winner");
			wifiGameIntent.putExtra("Instructions", false);
			startActivity(wifiGameIntent);
			this.finish();
			break;
		case "New Game":
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			Intent playGameIntent = new Intent(context, LevelMenu.class);
			playGameIntent.putExtra("Multiplayer", false);
            playGameIntent.putExtra("Caller", "Winner");
            playGameIntent.putExtra("Instructions", false);
			startActivity(playGameIntent);
			this.finish();
			break;
		case "Rematch!":
			if(canRematch) {
				PendingResult<InitiateMatchResult> rematch = Games.TurnBasedMultiplayer.rematch(client, matchId);
				rematch.setResultCallback(this);
			} else {
				Toast.makeText(context, "Sorry, cannot rematch", Toast.LENGTH_SHORT).show();
			}
			this.finish();
			break;
		}
	}

	@Override
	public void onResult(InitiateMatchResult result) {
		TurnBasedMatch match = result.getMatch();
		
		Intent board = new Intent(context, Board.class);
		board.putExtra("On Going Match", match.getData());
		board.putExtra("Match ID", match.getMatchId());
		String pendingPlayerId = getPendingParticipantId(match);
		board.putExtra("Pending Player", pendingPlayerId);
		board.putExtra("Current Player", match.getParticipantId(Games.Players.getCurrentPlayerId(client)));
		String pendingPlayerDisplayName = match.getParticipant(pendingPlayerId).getDisplayName();
		board.putExtra("Player 2 Name", pendingPlayerDisplayName);
		String currentPlayerId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
		String currentPlayerDisplayName = match.getParticipant(currentPlayerId).getDisplayName();
		board.putExtra("Player 1 Name", currentPlayerDisplayName);
		board.putExtra("Caller", "LevelMenu");
		board.putExtra("My Turn", true);
		board.putExtra("Finished", false);
		board.putExtra("Can Rematch", match.canRematch());
		startActivity(board);
	}

	private String getPendingParticipantId(TurnBasedMatch match) {
		ArrayList<String> participantIds = match.getParticipantIds();
		String pendingParticipantId = "";
		String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
		if( currentParticipantId.equals( participantIds.get(0) ) ) {
			pendingParticipantId = participantIds.get(1);
		}
		if( currentParticipantId.equals( participantIds.get(1) ) ) {
			pendingParticipantId = participantIds.get(0);
		}
		return pendingParticipantId;
	}
}
