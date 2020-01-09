package com.varunbatta.titanictictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class LevelMenu extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<InitiateMatchResult> {
	Context context;
	boolean multiplayer;
	Intent level;
	String caller;
	boolean instructions;
	String levelChoice;
    boolean reconnectOnce = true;
	
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean resolvingError = false;
    
    private static final int RC_SAVED_GAMES = 9009;
    
    private boolean success = false;
    
    private byte [] gameData = null;
    
    public static GoogleApiClient client = null;
    
    boolean currentGamesChosen = false;
    
    LinearLayout playerNamesLayout;
	EditText player1;
	public static String player1Name;
	EditText player2;
	public static String player2Name;
	TextView playerNames;
	TextView playerNamesInstructions;
	
	AlertDialog.Builder playerNamesDialogBuilder;
	AlertDialog playerNamesDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.levelmenu);
		
		context = getApplicationContext();
		
		multiplayer = getIntent().getBooleanExtra("Multiplayer", false);
		caller = getIntent().getStringExtra("Caller");
		instructions = getIntent().getBooleanExtra("Instructions", false);
		
		client = new GoogleApiClient.Builder(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
		.addApi(Games.API)
        .addScope(Games.SCOPE_GAMES)
        .addApi(Drive.API)
        .addScope(Drive.SCOPE_APPFOLDER)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
		LinearLayout menu = (LinearLayout) findViewById(R.id.levelMenuLayout);
		TextView welcome = new TextView(context);
		TextView title = new TextView(context);
		if ( multiplayer ) {
			welcome.setText("WiFi Game");
			welcome.setTextSize(40);
			title.setTextSize(30);
		} else {
			welcome.setText("Pass-by-Pass Game");
			welcome.setTextSize(30);
			title.setTextSize(20);
		}
		welcome.setTextColor(Color.BLACK);	
		welcome.setGravity(Gravity.CENTER_HORIZONTAL);		
		title.setText("Please Select Level:");
		title.setTextColor(Color.BLACK);		
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		
		menu.setBackgroundColor(Color.rgb(0, 153, 153));
		menu.addView(welcome);
		menu.addView(title);
		
//		Button instructions = new Button(context);
//		instructions.setText("Instructions");
//		instructions.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Button instructionsButton = (Button) v;
//				String instructionsHeading = instructionsButton.getText().toString();
//				levels(instructionsHeading);
//				
//			}
//		});
//		levelmenu.addView(instructions);
		
		playerNamesLayout = new LinearLayout(context);
		playerNamesLayout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 5);
		
//		playerNames = new TextView(context);
//		playerNames.setText("Player Names");
//		playerNames.setTextSize(40);
//		playerNames.setTextColor(Color.BLACK);
//		playerNames.setGravity(Gravity.CENTER_HORIZONTAL);
//		playerNamesLayout.addView(playerNames);
		
		playerNamesInstructions = new TextView(context);
		playerNamesInstructions.setText("Please enter the two player names below:");
		playerNamesInstructions.setTextSize(20);
		playerNamesInstructions.setTextColor(Color.BLACK);
		playerNamesLayout.addView(playerNamesInstructions);
		
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
		
		playerNamesDialogBuilder = new AlertDialog.Builder(LevelMenu.this);
		
		playerNamesDialogBuilder.setTitle("Player Names");
		playerNamesDialogBuilder.setView(playerNamesLayout);
		
		// Set up the buttons
		playerNamesDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	player1Name = player1.getText().toString();
				player2Name = player2.getText().toString();
			
				if(player1Name.matches("")||player2Name.matches("")) {
					Toast warning = Toast.makeText(context, "Sorry, but you must fill in the names of both players!", Toast.LENGTH_SHORT);
					warning.show();
				} else {
					levels(levelChoice);
				}
		    }
		});
		playerNamesDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	playerNamesDialog.hide();
		    }
		});
		playerNamesDialog = playerNamesDialogBuilder.create();
		
		Button level1 = new Button(context);
		level1.setText("Level 1");
		level1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button levelChoiceButton = (Button) v;
				levelChoice = levelChoiceButton.getText().toString();
				
				if ( !multiplayer ) {
					playerNamesDialog.show();
				} else {
					levels(levelChoice);
				}
			}
		});
		menu.addView(level1);
		
		Button level2 = new Button(context);
		level2.setText("Level 2");
		level2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button levelChoiceButton = (Button) v;
				levelChoice = levelChoiceButton.getText().toString();
				
				if ( !multiplayer ) {
					playerNamesDialog.show();
				} else {
					levels(levelChoice);
				}
			}
		});
		menu.addView(level2);
		
		Button level3 = new Button(context);
		level3.setText("Level 3");
		level3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button levelChoiceButton = (Button) v;
				String levelChoice = levelChoiceButton.getText().toString();
				Toast.makeText(context, levelChoice + " is not available yet, sorry", Toast.LENGTH_SHORT).show();
				
			}
		});
		menu.addView(level3);
		
		Button level4 = new Button(context);
		level4.setText("Level 4");
		level4.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button levelChoiceButton = (Button) v;
				String levelChoice = levelChoiceButton.getText().toString();
				Toast.makeText(context, levelChoice + " is not available yet, sorry", Toast.LENGTH_SHORT).show();
				
			}
		});
		menu.addView(level4);
		
		if(multiplayer) {
			Button matches = new Button(context);

			matches.setText("Current Games");
			matches.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Button choiceButton = (Button) v;
					String choice = choiceButton.getText().toString();
					levels(choice);
				}
			});	
			menu.addView(matches);
		} else {
			Button matches = new Button(context);
			
			matches.setText("Load Saved Game");
			matches.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Button choiceButton = (Button) v;
					String choice = choiceButton.getText().toString();
					levels(choice);
				}
			});
			menu.addView(matches);
		}
//		Button multiplayer = new Button(getApplicationContext());
//		multiplayer.setText("Multiplayer");
//		multiplayer.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Button levelChoiceButton = (Button) v;
//				String levelChoice = levelChoiceButton.getText().toString();
//				levels(levelChoice);
//			}
//		});
//		levelmenu.addView(multiplayer);
		
		
	}
	
	protected void levels(String levelChoice) {
		level = new Intent(context, Board.class);
		level.putExtra("Multiplayer", multiplayer);
		level.putExtra("Caller", "LevelMenu");
		level.putExtra("Player 1 Name", player1Name);
        level.putExtra("Player 2 Name", player2Name);
		if(instructions) {
			level.putExtra("Test", true);
			level.putExtra("Player 1 Name", "Player 1");
			level.putExtra("Player 2 Name", "Player 2");
		}
		if(caller.equals("Board") || caller.equals("Winner")) {
			Board.keys = new Hashtable<Integer, Button>(6561);	
//			Board.bottomPanel.removeAllViews();
//			Board.boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
		}
		switch(levelChoice){
		case "Level 1":
			level.putExtra("Level", 1);
			if(multiplayer) {
				if(client.isConnected()) {
					client.disconnect();
				}
				client.connect();
			} else {
				startActivity(level);
			}
			break;
		case "Level 2":
			level.putExtra("Level", 2);
			if(multiplayer) {
				if(client.isConnected()) {
					client.disconnect();
				}
				client.connect();
			} else {
				startActivity(level);
			}
			break;
//		case "Level 3":
//				Board game = new Board();
//				game.wincheckBoardSize(3);
//				game.play(3);
//				newDossier.Index.mainmenu.setVisible(false);
//			break;
//		case "Level 4":
//				Board game = new Board();
//				game.wincheckBoardSize(4);
//				game.play(4);
//				newDossier.Index.mainmenu.setVisible(false);
//			break;
//		case "Saved Games":
//			Intent savedGames = new Intent(context, CurrentGames.class);
//			savedGames.putExtra("Multiplayer", false);
//			startActivity(savedGames);
//			break;
		case "Current Games":
			currentGamesChosen = true;
			Intent currentGames = new Intent(context, CurrentGames.class);
			currentGames.putExtra("Multiplayer", true);
			startActivity(currentGames);
			break;
		case "Load Saved Game":
			client.connect();
			break;
//		case "Instructions":
//			Intent instructionsIntent = new Intent(getApplicationContext(), Instructions.class);
//			startActivity(instructionsIntent);
//			break;
//			case "Multiplayer":
//				client.connect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d("CM", "Menu Connected");
		if ( multiplayer ) {
			Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(client, 1, 1, true);
			startActivityForResult(intent, 12345);
		} else {
			Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(client,
		            "Saved Games", false, true, Snapshots.DISPLAY_LIMIT_NONE);
		    startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
		}
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.d("Suspended", "" + cause);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("CFM", "Menu Connection Failed");
		if (resolvingError) {
			Log.d("RE", "Resolving Error");
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
        	Log.d("R", "Resolution " + result.getErrorCode());
            try {
                resolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                client.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
//            showErrorDialog(result.getErrorCode());
        	Log.d("NR", "No Resolution");
        	client.connect();
            resolvingError = true;
        }
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        
        if (request == REQUEST_RESOLVE_ERROR) {
        	client.connect();
        }
        
        if (request == 12345) {
            if (response != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

			Log.d("Invitees", invitees.get(0));

            // Get auto-match criteria.
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            Log.d("aMC", "" + autoMatchCriteria);

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria)
                    .build();

            Log.d("TBMC", tbmc.getInvitedPlayerIds()[0]);

            // Create and start the match.
            Games.TurnBasedMultiplayer
                .createMatch(client, tbmc)
                .setResultCallback(this);
        }
        
        if (request == RC_SAVED_GAMES) {
        	if (data != null) {
    	        if (data.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
    	            Log.d("LG", "Load Game");
    	        	// Load a snapshot.
    	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
    	                    data.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
    	            final String snapshotNm = snapshotMetadata.getUniqueName();
    	            Log.d("snapshotNm", snapshotNm);
    	            
    	            AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
    	                @Override
    	                protected Integer doInBackground(Void... params) {
    	                    // Open the saved game using its name.
    	                    Snapshots.OpenSnapshotResult result = Games.Snapshots.open(client,
    	                            snapshotNm, true).await();

    	                    // Check the result of the open operation
    	                    if (result.getStatus().isSuccess()) {
    	                    	Log.d("status", "success");
    	                        Snapshot snapshot = result.getSnapshot();
    	                        // Read the byte content of the saved game.
    	                        try {
    	                            gameData = snapshot.getSnapshotContents().readFully();
    	                            success = true;
    	                        } catch (IOException e) {
    	                            Log.e("Error", "Error while reading Snapshot.", e);
    	                        }
    	                    } else{
    	                        Log.e("Error", "Error while loading: " + result.getStatus().getStatusCode());
    	                    }

    	                    return result.getStatus().getStatusCode();
    	                }
    	            };

    	            task.execute();
    	            
    	            while(!success) {
    	            	Log.d("success", "" + success);
    	            }
    	            
    	            String [] playerNames = snapshotNm.split("-");
    	            int level = Integer.parseInt(playerNames[2].substring(5, playerNames[2].length()));
    	            
    	            Intent board = new Intent(context, Board.class);
    	            board.putExtra("Multiplayer", false);
    	            board.putExtra("Caller", "CurrentGames");
    	            board.putExtra("Level", level);
    	            board.putExtra("On Going Match", gameData);
    	            board.putExtra("Saved Game", true);
    	            startActivity(board);
    	        }
    	    }
        }
    }

	@Override
	public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
		// Check if the status code is not success.
        Status status = result.getStatus();
        Log.d("Status", status.toString());
//        if (status.isSuccess()) {
////            showErrorDialog(status.getStatusCode());
//            return;
//        }

        TurnBasedMatch match = result.getMatch();

        // If this player is not the first player in this match, continue.
//        if (match.getData() != null) {
//            showTurnUI(match);
//            return;
//        }

        // Otherwise, this is the first player. Initialize the game state.
//		level = new Intent(context, Board.class);
		level.putExtra("On Going Match", match.getData());
		level.putExtra("Match ID", match.getMatchId());
		String pendingPlayerId = getPendingParticipantId(match);
		level.putExtra("Pending Player", pendingPlayerId);
		level.putExtra("Current Player", match.getParticipantId(Games.Players.getCurrentPlayerId(client)));
		String pendingPlayerDisplayName = match.getParticipant(pendingPlayerId).getDisplayName();
		level.putExtra("Player 2 Name", pendingPlayerDisplayName);
		String currentPlayerId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
		String currentPlayerDisplayName = match.getParticipant(currentPlayerId).getDisplayName();
		level.putExtra("Player 1 Name", currentPlayerDisplayName);
		level.putExtra("Caller", "LevelMenu");
		level.putExtra("My Turn", true);
		level.putExtra("Finished", false);
		level.putExtra("Can Rematch", match.canRematch());
		level.putExtra("Multiplayer", multiplayer);
		startActivity(level);
//
//        // Let the player take the first turn
//        showTurnUI(match);
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

//	private void showTurnUI(TurnBasedMatch match) {
//		Intent level2 = new Intent(getApplicationContext(), Board.class);
//		level2.putExtra("Level", 2);
//		level2.putExtra("On Going Match", match.getData());
//		level2.putExtra("Match ID", match.getMatchId());
//		String pendingPlayerId = getPendingParticipantId(match);
//		level2.putExtra("Pending Player", pendingPlayerId);
//		String pendingPlayerDisplayName = match.getParticipant(pendingPlayerId).getDisplayName();
//		level2.putExtra("Player 2 Name", pendingPlayerDisplayName);
//		String currentPlayerId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
//		String currentPlayerDisplayName = match.getParticipant(currentPlayerId).getDisplayName();
//		level2.putExtra("Player 1 Name", currentPlayerDisplayName);
//		startActivity(level2);
//	}

	public void startIntentActivity(Intent level2) {
		startActivity(level2);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean backPressed = false;
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            backPressed = true;
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent mainMenuIntent = new Intent(context, MainMenu.class);
            startActivity(mainMenuIntent);
            this.finish();
		}
        if(backPressed) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
	}
}
