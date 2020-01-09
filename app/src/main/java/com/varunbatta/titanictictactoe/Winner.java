package com.varunbatta.titanictictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;

import java.util.Hashtable;

public class Winner extends Activity { //} implements ResultCallback<InitiateMatchResult> {

	Context context;
	boolean multiplayer;
//	String matchId;
	String caller;
//	boolean canRematch;
	String winnerName;
	long requestID;
	
//	GoogleApiClient client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("oC", "Winner onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.winner);
		
		Board.keys = new Hashtable<Integer, Button>(6561);
//		Board.bottomPanel.removeAllViews();
//		Board.boardLayout.removeAllViews();
		ButtonPressed.currentTurn = "";
		
		multiplayer = getIntent().getBooleanExtra("Multiplayer", true);
//		matchId = getIntent().getStringExtra("Match ID");
		caller = getIntent().getStringExtra("Client");
//		canRematch = getIntent().getBooleanExtra("Can Rematch", true);
		winnerName = getIntent().getStringExtra("Winner");
		requestID = getIntent().getLongExtra("Request ID", 0);

		
//		if(caller != null) {
//			switch(caller) {
//			case "Index":
//				client = Index.client;
//				break;
//			case "Current Games":
//				client = CurrentGames.client;
//				break;
//			case "Level Menu":
//				client = LevelMenu.client;
//				break;
//			}
//		}
		
		context = getApplicationContext();
		
//		LinearLayout winnerLayout = findViewById(R.id.winnerLayout);
		TextView congrats = findViewById(R.id.congrats);
//		LinearLayout bottomPanel = (LinearLayout) findViewById(R.id.bottomPanel);
//		LinearLayout bottomPanel2 = (LinearLayout) findViewById(R.id.bottomPanel2);
		
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
//		congrats.setTextColor(getResources().getColor(R.color.colorBlack));
//		congrats.setTextSize(50);
//		congrats.setGravity(Gravity.CENTER);
		
//		winnerLayout.setBackgroundColor(getResources().getColor(R.color.colorGreen));
//		winnerLayout.addView(congrats);
		
//		LinearLayout bottomPanel = new LinearLayout(context);
//		bottomPanel.setOrientation(LinearLayout.HORIZONTAL);
//		bottomPanel.setGravity(Gravity.BOTTOM);
		
		Button mainMenu = findViewById(R.id.mainMenu);
//		mainMenu.setText("Main Menu");
		mainMenu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
//		bottomPanel.addView(mainMenu);
		
		Button viewGame = findViewById(R.id.viewGame);
//		viewGame.setText("View Game");
		viewGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				bottomPanelListener(buttonText);
			}
		});
//		bottomPanel.addView(viewGame);
		
//		Button newGame = findViewById(R.id.newGame);
////		newGame.setText("New Game");
//		newGame.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Button button = (Button) v;
//				String buttonText = button.getText().toString();
//				bottomPanelListener(buttonText);
//			}
//		});
//		bottomPanel.addView(newGame);
		
//		Button newWifiGame = new Button(context);
//		newWifiGame.setText("New WiFi Game");
//		newWifiGame.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Button button = (Button) v;
//				String buttonText = button.getText().toString();
//				bottomPanelListener(buttonText);
//			}
//		});
//		bottomPanel2.addView(newWifiGame);
		
//		if(multiplayer) {
			Button rematch = findViewById(R.id.rematch);
//			rematch.setText("Rematch!");
			rematch.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Button button = (Button) v;
					String buttonText = button.getText().toString();
					bottomPanelListener(buttonText);
				}
			});
//			bottomPanel2.addView(rematch);
//		}
		
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
//			Intent board = new Intent(context, Board.class);
//			board.putExtra("Level", Board.level);
//			board.putExtra("My Turn", false);
//			board.putExtra("Finished", true);
//			board.putExtra("Caller", "Winner");
////			if(multiplayer) {
////				board.putExtra("On Going Match", Board.onGoingMatch);
////				board.putExtra("Match ID", matchId);
////			}
//			startActivity(board);
			this.finish();
			break;
//		case "New WiFi Game":
//			ButtonPressed.wincheck = new String [10][9];
//			ButtonPressed.metawincheck = new String [3][3];
//			Board.wincheck = new String [10][9];
//			Intent wifiGameIntent = new Intent(context, LevelMenu.class);
//			wifiGameIntent.putExtra("Multiplayer", true);
//			wifiGameIntent.putExtra("Caller", "Winner");
//			wifiGameIntent.putExtra("Instructions", false);
//			startActivity(wifiGameIntent);
//			this.finish();
//			break;
//		case "New Game":
//			ButtonPressed.wincheck = new String [10][9];
//			ButtonPressed.metawincheck = new String [3][3];
//			Board.wincheck = new String [10][9];
//			Intent playGameIntent = new Intent(context, LevelMenu.class);
//			playGameIntent.putExtra("Multiplayer", false);
//            playGameIntent.putExtra("Caller", "Winner");
//            playGameIntent.putExtra("Instructions", false);
//			startActivity(playGameIntent);
//			this.finish();
//			break;
		case "Rematch?":
            Intent board = new Intent(getApplicationContext(), Board.class);
            board.putExtra("Level", PlayerSelector.level);
            board.putExtra("Caller", "Winner");
            board.putExtra("My Turn", true);
            board.putExtra("Finished", false);
            board.putExtra("Multiplayer", multiplayer);
            if (multiplayer) {
//                GameRequest deleteRequest = new GameRequest();
//                deleteRequest.createNewGameRequest("/" + requestID, null, HttpMethod.DELETE);
//                new GraphRequests().execute(deleteRequest);

                Game currentGame = Index.availableGames.get(requestID);

                if (AccessToken.getCurrentAccessToken().getUserId().equals("" + currentGame.player2.playerFBID)) {
                    Player temp = currentGame.player1;
                    currentGame.player1 = currentGame.player2;
                    currentGame.player2 = temp;
                }

                board.putExtra("Player 1 Name", currentGame.player1.playerName);
                board.putExtra("Player 2 Name", currentGame.player2.playerName);
                board.putExtra("Player 2 FBID", currentGame.player2.playerFBID);
                board.putExtra("Player 1 FBID", currentGame.player1.playerFBID);
                Index.availableGames.remove(requestID);
            } else {
                board.putExtra("Player 1 Name", Board.game.player1.playerName);
                board.putExtra("Player 2 Name", Board.game.player2.playerName);
                board.putExtra("Player 2 FBID", Board.game.player2.playerFBID);
                board.putExtra("PLayer 1 FBID", Board.game.player2.playerFBID);
            }
            startActivity(board);
            this.finish();
//			if(canRematch) {
//				PendingResult<InitiateMatchResult> rematch = Games.TurnBasedMultiplayer.rematch(client, matchId);
//				rematch.setResultCallback(this);
//			} else {
//				Toast.makeText(context, "Sorry, cannot rematch", Toast.LENGTH_SHORT).show();
//			}
//			this.finish();
			break;
		}
	}

//	@Override
//	public void onResult(InitiateMatchResult result) {
//		TurnBasedMatch match = result.getMatch();
//
//		Intent board = new Intent(context, Board.class);
//		board.putExtra("On Going Match", match.getData());
//		board.putExtra("Match ID", match.getMatchId());
//		String pendingPlayerId = getPendingParticipantId(match);
//		board.putExtra("Pending Player", pendingPlayerId);
//		board.putExtra("Current Player", match.getParticipantId(Games.Players.getCurrentPlayerId(client)));
//		String pendingPlayerDisplayName = match.getParticipant(pendingPlayerId).getDisplayName();
//		board.putExtra("Player 2 Name", pendingPlayerDisplayName);
//		String currentPlayerId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
//		String currentPlayerDisplayName = match.getParticipant(currentPlayerId).getDisplayName();
//		board.putExtra("Player 1 Name", currentPlayerDisplayName);
//		board.putExtra("Caller", "LevelMenu");
//		board.putExtra("My Turn", true);
//		board.putExtra("Finished", false);
//		board.putExtra("Can Rematch", match.canRematch());
//		startActivity(board);
//	}
//
//	private String getPendingParticipantId(TurnBasedMatch match) {
//		ArrayList<String> participantIds = match.getParticipantIds();
//		String pendingParticipantId = "";
//		String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
//		if( currentParticipantId.equals( participantIds.get(0) ) ) {
//			pendingParticipantId = participantIds.get(1);
//		}
//		if( currentParticipantId.equals( participantIds.get(1) ) ) {
//			pendingParticipantId = participantIds.get(0);
//		}
//		return pendingParticipantId;
//	}
}
