package com.varunbatta.titanictictactoe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.LoadMatchesResponse;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchBuffer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchesResult;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.varunbatta.titanictictactoe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData.Item;
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
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentGames extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadMatchesResult> {

	public static GoogleApiClient client;
	public static boolean currentGamesVisible;
	
	Context context;
	String [][] playerNamesAndMatchIds;
	TurnBasedMatch [] matches;
	LinearLayout currentGamesLayout;
	boolean multiplayer;
	
	private static final int RC_SAVED_GAMES = 9009;
	
	boolean resolvingError;
	
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    private byte [] gameData = null;
    private boolean success = false;
    
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    private FrameLayout progressBarHolder;
    private ScrollView currentGamesScrollView;
    
    String [] gameOptions;
    boolean cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currentgames);
		
		currentGamesScrollView = (ScrollView) findViewById(R.id.currentGamesScrollView);
		currentGamesScrollView.setBackgroundColor(Color.rgb(0, 173, 173));
		
		progressBarHolder = (FrameLayout) findViewById(R.id.currentGamesProgressBarHolder);
		progressBarHolder.setBackgroundColor(Color.rgb(0, 173, 173));
		
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
		
		context = getApplicationContext();
		
		multiplayer = getIntent().getBooleanExtra("Multiplayer", false);
		
		currentGamesLayout = (LinearLayout) findViewById(R.id.currentGamesLayout);
		
		TextView title = new TextView(context);
		title.setText("Current Games");
		title.setTextColor(Color.BLACK);
		title.setTextSize(40);
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		
		currentGamesLayout.addView(title);
		currentGamesLayout.setBackgroundColor(Color.rgb(0, 173, 173));
		
		client.connect();
		currentGamesVisible = true;
		Log.d("Done", "Done");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		currentGamesVisible = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		currentGamesVisible = false;
	}

	@Override
	public void onResult(LoadMatchesResult matches) {
		Log.d("Result Received", "RR");
		LoadMatchesResponse currentGames = matches.getMatches();
		TurnBasedMatchBuffer myTurns = currentGames.getMyTurnMatches();
		TurnBasedMatchBuffer theirTurns = currentGames.getTheirTurnMatches();
		TurnBasedMatchBuffer finishedGames = currentGames.getCompletedMatches();
		getOpponentNamesAndMatchIds(myTurns, theirTurns, finishedGames);
	}

	private void getOpponentNamesAndMatchIds(TurnBasedMatchBuffer myTurns,
			TurnBasedMatchBuffer theirTurns, TurnBasedMatchBuffer finishedGames) {
		int myTurnsCount = myTurns.getCount();
		int theirTurnsCount = theirTurns.getCount();
		int finishedGamesCount = finishedGames.getCount();
		playerNamesAndMatchIds = new String[myTurnsCount + theirTurnsCount + finishedGamesCount][3];
		matches = new TurnBasedMatch[myTurnsCount + theirTurnsCount + finishedGamesCount];
		
		Log.d("mTC", "" + myTurnsCount);
		Log.d("tTC", "" + theirTurnsCount);
		Log.d("fTC", "" + finishedGamesCount);
		
		for(int i = 0; i < myTurnsCount; i++) {
			TurnBasedMatch match = myTurns.get(i);
			String matchId = match.getMatchId();
			if(matchId != null) {
				putPlayerNames(match, i);
				playerNamesAndMatchIds[i][2] = match.getMatchId();
				matches[i] = match;
				int level = -1;
				if(match.getData() != null) {
					level = getLevel(match.getData());
				}
				Button potentialGameButton = new Button(context);
				potentialGameButton.setText("Your Turn against " + playerNamesAndMatchIds[i][1] + " - Level " + level);
				Log.d("mTTag", i + " " + playerNamesAndMatchIds[i][2]);
				potentialGameButton.setTag(playerNamesAndMatchIds[i][2]);
				potentialGameButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						Log.d("Clicked", "mT");
						gameOptions = new String [2];
						gameOptions[0] = "Play Game";
						gameOptions[1] = "Forfeit";
						AlertDialog.Builder builder = new AlertDialog.Builder(CurrentGames.this);
						builder.setTitle("Game Options")
						.setItems(gameOptions, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Button selectedGame = (Button) v;
								String matchId = (String) selectedGame.getTag();
								boolean cancelled = selectedGame.getText().toString().equals("Cancelled");
								dialogListener(gameOptions[which], matchId, false, cancelled, selectedGame);
							}
						});
					    AlertDialog dialog = builder.create();
					    dialog.show();
	//					Button selectedGame = (Button) v;
	//					String matchId = (String) selectedGame.getTag();
	//					prepareBoardForGame(matchId, false, false);
					}
				});
				
				currentGamesLayout.addView(potentialGameButton);
				potentialGameButton.invalidate();
			} else {
				Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
				myTurnsCount--;
			}
//			Log.d("pGB", "Your Turn");
//			Toast.makeText(context, "Your Turn", Toast.LENGTH_SHORT).show();
		}
		
		for(int i = 0; i < theirTurnsCount; i++) {
			TurnBasedMatch match = theirTurns.get(i);
			String matchId = match.getMatchId();
			if(matchId != null) {
				int index = i + myTurnsCount;
				putPlayerNames(match, index);
				playerNamesAndMatchIds[index][2] = matchId;
				matches[index] = match;
				Button potentialGameButton = new Button(context);
				potentialGameButton.setText(playerNamesAndMatchIds[index][1] + "'s turn - Level " + getLevel(match.getData()));
				Log.d("tTTag", index + " " + playerNamesAndMatchIds[index][2]);
				potentialGameButton.setTag(playerNamesAndMatchIds[index][2]);
				potentialGameButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						Log.d("Clicked", "tT");
						gameOptions = new String [1];
						gameOptions[0] = "View Game";
						AlertDialog.Builder builder = new AlertDialog.Builder(CurrentGames.this);
						builder.setTitle("Game Options")
						.setItems(gameOptions, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Button selectedGame = (Button) v;
								String matchId = (String) selectedGame.getTag();
								boolean cancelled = selectedGame.getText().toString().equals("Cancelled");
								dialogListener(gameOptions[which], matchId, false, cancelled, selectedGame);
							}
						});
					    AlertDialog dialog = builder.create();
					    dialog.show();
	//					Button selectedGame = (Button) v;
	//					String matchId = (String) selectedGame.getTag();
	//					prepareBoardForGame(matchId, false, false);
					}
				});
				
				currentGamesLayout.addView(potentialGameButton);
				potentialGameButton.invalidate();
			} else {
				Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
				theirTurnsCount--;
			}
//			Log.d("pGB", "Their Turn");
//			Toast.makeText(context, "Their Turn", Toast.LENGTH_SHORT).show();
		}
		
		for(int i = 0; i < finishedGamesCount; i++) {
			TurnBasedMatch match = finishedGames.get(i);
			String matchId = match.getMatchId();
			if(matchId != null) {
				int index = i + myTurnsCount + theirTurnsCount;
				putPlayerNames(match, index);
				playerNamesAndMatchIds[index][2] = matchId;
				matches[index] = match;
				String currentPlayerId = Games.Players.getCurrentPlayerId(client);
				Log.d("cPID", currentPlayerId);
				String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
				Log.d("cPaID", currentParticipantId);
//				ParticipantResult winning = new ParticipantResult(currentParticipantId, ParticipantResult.MATCH_RESULT_WIN, ParticipantResult.PLACING_UNINITIALIZED);
//				ParticipantResult losing = new ParticipantResult(currentParticipantId, ParticipantResult.MATCH_RESULT_LOSS, ParticipantResult.PLACING_UNINITIALIZED);
//				ParticipantResult tie = new ParticipantResult(currentParticipantId, ParticipantResult.MATCH_RESULT_TIE, ParticipantResult.PLACING_UNINITIALIZED);
				Button finishedGameButton = new Button(context);
//				Log.d("Result", "" + match.getParticipant(currentParticipantId).getResult().getResult());
				if(match.getStatus() == TurnBasedMatch.MATCH_STATUS_CANCELED) {
					finishedGameButton.setText("Cancelled");
				} else if ( match.getParticipant(currentParticipantId).getResult().getResult() == ParticipantResult.MATCH_RESULT_WIN ) {
					finishedGameButton.setText("Winner - " + match.getParticipant(currentParticipantId).getDisplayName());
				} else if ( match.getParticipant(currentParticipantId).getResult().getResult() == ParticipantResult.MATCH_RESULT_LOSS ) {
					finishedGameButton.setText("Winner - " + match.getParticipant(getPendingParticipantId(match)).getDisplayName());
				} else if ( match.getParticipant(currentParticipantId).getResult().getResult() == ParticipantResult.MATCH_RESULT_TIE ) {
					finishedGameButton.setText("Tie between " + match.getParticipant(currentParticipantId).getDisplayName() + " & " + match.getParticipant(getPendingParticipantId(match)).getDisplayName());
				} else {
					finishedGameButton.setText("Sorry, Unknown Data");
				}
				Log.d("fGTag", index + " " + playerNamesAndMatchIds[index][2]);
				finishedGameButton.setTag(playerNamesAndMatchIds[index][2]);
				finishedGameButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						gameOptions = new String [3];
						gameOptions[0] = "View Game";
						gameOptions[1] = "Remove";
						gameOptions[2] = "Rematch!";
						AlertDialog.Builder builder = new AlertDialog.Builder(CurrentGames.this);
						builder.setTitle("Game Options")
						.setItems(gameOptions, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Button selectedGame = (Button) v;
								String matchId = (String) selectedGame.getTag();
								boolean cancelled = selectedGame.getText().toString().equals("Cancelled");
								dialogListener(gameOptions[which], matchId, true, cancelled, selectedGame);
							}
						});
					    AlertDialog dialog = builder.create();
					    dialog.show();
	//					Button selectedGame = (Button) v;
	//					String matchId = (String) selectedGame.getTag();
	//					boolean cancelled = selectedGame.getText().toString().equals("Cancelled");
	//					prepareBoardForGame(matchId, false, cancelled);		
					}
				});
				currentGamesLayout.addView(finishedGameButton);
				finishedGameButton.invalidate();
			} else {
				Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
				finishedGamesCount--;
			}
		}
		
		if(myTurnsCount == 0 && theirTurnsCount == 0 && finishedGamesCount == 0) {
			TextView none = new TextView(context);
			none.setText("None!");
			none.setTextSize(30);
			none.setTextColor(Color.BLACK);
			none.setGravity(Gravity.CENTER);
			currentGamesLayout.addView(none);
			none.invalidate();
		}

	}
	
	private void dialogListener(String option, final String matchId, boolean finished, boolean cancelled, final Button selectedButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(CurrentGames.this);
	    AlertDialog dialog;
		switch(option) {
		case "Play Game":
			Log.d("PGC", "Play Game Called");
			prepareBoardForGame(matchId, finished, cancelled);
			break;
		case "View Game":
			prepareBoardForGame(matchId, finished, cancelled);
			break;
		case "Forfeit":
			builder.setTitle("Forfeit?")
				.setMessage("Are you sure you want to forfeit?")
				.setPositiveButton("Forfeit", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						forfeitGame(matchId, selectedButton);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		    dialog = builder.create();
		    dialog.show();
			break;
		case "Remove":
			builder.setTitle("Remove?")
				.setMessage("Are you sure you want to remove this game?")
				.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
						currentGamesLayout.removeView(selectedButton);
						currentGamesLayout.invalidate();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
			dialog = builder.create();
		    dialog.show();
			break;
		case "Rematch!":
			Winner winner = new Winner();
			int index = -1;
			for(int i = 0; i < playerNamesAndMatchIds.length; i++) {
				if(playerNamesAndMatchIds[i][2].equals(matchId)) {
					index = i;
					break;
				}
			}
			TurnBasedMatch match = matches[index];
			if(match.canRematch()) {
				PendingResult<InitiateMatchResult> rematch = Games.TurnBasedMultiplayer.rematch(client, matchId);
				rematch.setResultCallback(winner);
			} else {
				Toast.makeText(context, "Sorry, cannot rematch", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void forfeitGame(String matchId, Button selectedButton) {
		int index = -1;
		for(int i = 0; i < playerNamesAndMatchIds.length; i++) {
			if(playerNamesAndMatchIds[i][2].equals(matchId)) {
				index = i;
				break;
			}
		}
		TurnBasedMatch match = matches[index];
		
		ParticipantResult currentPlayer;
		ParticipantResult pendingPlayer;
		currentPlayer = new ParticipantResult(match.getParticipantId(Games.Players.getCurrentPlayerId(client)), ParticipantResult.MATCH_RESULT_LOSS, ParticipantResult.PLACING_UNINITIALIZED);
		pendingPlayer = new ParticipantResult(getPendingParticipantId(match), ParticipantResult.MATCH_RESULT_WIN, ParticipantResult.PLACING_UNINITIALIZED);
		List<ParticipantResult> results = new ArrayList<ParticipantResult>();
		results.add(currentPlayer);
		results.add(pendingPlayer);
		GoogleApiClient client = Index.client;
		Games.TurnBasedMultiplayer.finishMatch(client, matchId, match.getData(), results);
		ButtonPressed.currentTurn = "";
		Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
		currentGamesLayout.removeView(selectedButton);
		currentGamesLayout.invalidate();
	}

	private void putPlayerNames(TurnBasedMatch match, int row) {
		ArrayList<String> participantIds = match.getParticipantIds();
		String pendingParticipantId = "";
		String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
		if( currentParticipantId.equals( participantIds.get(0) ) ) {
			pendingParticipantId = participantIds.get(1);
		}
		if( currentParticipantId.equals( participantIds.get(1) ) ) {
			pendingParticipantId = participantIds.get(0);
		}
		playerNamesAndMatchIds[row][0] = match.getParticipant(currentParticipantId).getDisplayName();
		playerNamesAndMatchIds[row][1] = match.getParticipant(pendingParticipantId).getDisplayName();
	}
	
	private void prepareBoardForGame(String matchId, boolean finished, boolean cancelled) {
//		Log.d("pBFG", "Called");
		Log.d("MatchId", matchId);
		Log.d("Length", "" + playerNamesAndMatchIds.length);
		int index = -1;
		for(int i = 0; i < playerNamesAndMatchIds.length; i++) {
			Log.d("ArrayMatchId", i + " " + playerNamesAndMatchIds[i][2]);
			if(playerNamesAndMatchIds[i][2].equals(matchId)) {
				index = i;
				break;
			}
		}
		TurnBasedMatch match = matches[index];
		Log.d("Match", match.toString());
		byte[] gameData = match.getData();
		String pendingParticipantId = getPendingParticipantId(match);
		int level = getLevel(gameData);
		Log.d("Level", "" + level);
		int status = match.getTurnStatus();
		boolean myTurn = false;
		
		if(status == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
			myTurn = true;
		}
		
		Log.d("mT", "" + myTurn);
		
		Intent board = new Intent(this, Board.class);
		board.putExtra("Level", level);
		board.putExtra("On Going Match", gameData);
		board.putExtra("Match ID", matchId);
		board.putExtra("Pending Player", pendingParticipantId);
		board.putExtra("Current Player", match.getParticipantId(Games.Players.getCurrentPlayerId(client)));
		board.putExtra("Caller", "CurrentGames");
		board.putExtra("My Turn", myTurn);
		board.putExtra("Finished", finished);
		board.putExtra("Can Rematch", match.canRematch());
		board.putExtra("Multiplayer", true);
		startActivity(board);
		
		if(cancelled) {
			Log.d("lM", "Leaving Match");
			Games.TurnBasedMultiplayer.dismissMatch(client, matchId);
		}
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
	
	private int getLevel(byte[] game) {
		int level = 0;
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
		level = Integer.parseInt(board[81][5]);
		return level;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("CF", "Connection Failed");
		if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
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
            resolvingError = true;
        }
	}

	@Override
	public void onConnected(Bundle connectionResult) {
		Log.d("OCCG", "Connected CurrentGames");
		AsyncTask<Void, Void, Boolean> updateTask = new AsyncTask<Void, Void, Boolean>() {
            
    		@Override
            protected void onPreExecute() {
                super.onPreExecute();
//                button.setEnabled(false);
                inAnimation = new AlphaAnimation(0f, 1f);
                inAnimation.setDuration(200);
                progressBarHolder.setAnimation(inAnimation);
                progressBarHolder.setVisibility(View.VISIBLE);
    		}
    		
    		@Override
            protected Boolean doInBackground(Void... params) {
    			int [] statuses = {TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN, TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN, 
    					TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE};
    			PendingResult<LoadMatchesResult> activeGames = Games.TurnBasedMultiplayer.loadMatchesByStatus(client, statuses);
    			setResultCallback(activeGames);
    			return true;
            }

			@Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                progressBarHolder.setAnimation(outAnimation);
                progressBarHolder.setVisibility(View.GONE);
            }
        };
        updateTask.execute();
	}

	@Override
	public void onConnectionSuspended(int result) {
		Log.d("CS", "Connection Suspended");
	}
	
    private void setResultCallback(
			PendingResult<LoadMatchesResult> activeGames) {
		activeGames.setResultCallback(this);	
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("keyCode", "KEYCODE_BACK");
			currentGamesVisible = false;
			this.finish();	
		}
		return super.onKeyDown(keyCode, event);
    }
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode,
//	                                Intent intent) {
//	    Log.d("oAR", "onActivityResult");
//		if (intent != null) {
//	        if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
//	            Log.d("LG", "Load Game");
//	        	// Load a snapshot.
//	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
//	                    intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
//	            final String snapshotNm = snapshotMetadata.getUniqueName();
//	            Log.d("snapshotNm", snapshotNm);
//	            
//	            AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
//	                @Override
//	                protected Integer doInBackground(Void... params) {
//	                    // Open the saved game using its name.
//	                    Snapshots.OpenSnapshotResult result = Games.Snapshots.open(client,
//	                            snapshotNm, true).await();
//
//	                    // Check the result of the open operation
//	                    if (result.getStatus().isSuccess()) {
//	                    	Log.d("status", "success");
//	                        Snapshot snapshot = result.getSnapshot();
//	                        // Read the byte content of the saved game.
//	                        try {
//	                            gameData = snapshot.getSnapshotContents().readFully();
//	                            success = true;
//	                        } catch (IOException e) {
//	                            Log.e("Error", "Error while reading Snapshot.", e);
//	                        }
//	                    } else{
//	                        Log.e("Error", "Error while loading: " + result.getStatus().getStatusCode());
//	                    }
//
//	                    return result.getStatus().getStatusCode();
//	                }
//	            };
//
//	            task.execute();
//	            
//	            while(!success) {
//	            	Log.d("success", "" + success);
//	            }
//	            
//	            String [] playerNames = snapshotNm.split("-");
//	            String player1 = playerNames[0];
//	            String player2 = playerNames[1];
//	            int level = Integer.parseInt(playerNames[2].substring(5, playerNames[2].length()));
//	            
//	            Intent board = new Intent(context, Board.class);
//	            board.putExtra("Multiplayer", false);
//	            board.putExtra("Caller", "CurrentGames");
//	            board.putExtra("Level", level);
//	            board.putExtra("On Going Match", gameData);
//	            startActivity(board);
//	            
//	        } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
//	            // Create a new snapshot named with a unique string
//	        	final String snapshotNm = Board.player1 + "-" + Board.player2 + "-" + "Level" + Board.n;
//	        	Log.d("snapshotNm", snapshotNm);
//
//	        	AsyncTask<Void, Void, Boolean> updateTask = new AsyncTask<Void, Void, Boolean>() {
//	                @Override
//	                protected Boolean doInBackground(Void... params) {
//	                    Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
//	                            client, snapshotNm, true).await();
//
//	                    if (!open.getStatus().isSuccess()) {
//	                        Log.w("!success", "Could not open Snapshot for update.");
//	                        return false;
//	                    }
//
//	                    // Change data but leave existing metadata
//	                    Snapshot snapshot = open.getSnapshot();
//	                    snapshot.getSnapshotContents().writeBytes(wincheckerToByteArray(Board.wincheck));
//
//	                    Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
//	                            client, snapshot, SnapshotMetadataChange.EMPTY_CHANGE).await();
//
//	                    if (!commit.getStatus().isSuccess()) {
//	                        Log.w("!success", "Failed to commit Snapshot.");
//	                        return false;
//	                    }
//
//	                    // No failures
//	                    return true;
//	                }
//	            };
//	            updateTask.execute();
//	            Log.d("Executed", "Saved");
//	        }
//	    }
//	}
	
//	private byte[] wincheckerToByteArray(String[][] winchecker) {
//		String gameString = "";
//		for(int i = 0; i < winchecker.length; i++) {
//			for(int j = 0; j < winchecker[0].length; j++) {
//				gameString = gameString + winchecker[i][j] + ",";
//			}
//			gameString = gameString + ";";
//		}
//		return gameString.getBytes();
//	}
}
