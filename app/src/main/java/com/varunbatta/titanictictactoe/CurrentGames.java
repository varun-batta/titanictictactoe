package com.varunbatta.titanictictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.facebook.HttpMethod;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.GameRequestDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CurrentGames extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
    // TODO: This entire file needs to be refactored and cleaned up
	public static GoogleApiClient client;
	public static boolean currentGamesVisible;
	
	Context context;
    static Map<Long, Game> currentGames;
	LinearLayout currentGamesLayout;
	private int currentGameIndex = 1;
	boolean multiplayer;
	
	private static final int RC_SAVED_GAMES = 9009;
	private static final int RC_SIGN_IN = 7007;
	
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

//		currentGamesScrollView = findViewById(R.id.currentGamesScrollView);
//		currentGamesScrollView.setBackgroundColor(Color.rgb(0, 173, 173));

		progressBarHolder = findViewById(R.id.currentGamesProgressBarHolder);
//		progressBarHolder.setBackgroundColor(Color.rgb(0, 173, 173));
		
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
		
//		multiplayer = getIntent().getBooleanExtra("Multiplayer", false);
		
		currentGamesLayout = findViewById(R.id.currentGamesLayout);
		prepareCurrentGamesButtons();
		prepareSavedGamesButtons();
		
//		TextView title = new TextView(context);
//		title.setText("Current Games");
//		title.setTextColor(Color.BLACK);
//		title.setTextSize(40);
//		title.setGravity(Gravity.CENTER_HORIZONTAL);
		
//		currentGamesLayout.addView(title);
//		currentGamesLayout.setBackgroundColor(Color.rgb(0, 173, 173));
		
//		client.connect();
		currentGamesVisible = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		currentGamesVisible = true;
		signInSilently();
	}

    private void signInSilently() {
        GoogleSignInOptions signInOption =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Add the APPFOLDER scope for Snapshot support.
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();

        final GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOption);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
//                            onConnected(task.getResult());
                        } else {
//                            Intent signInIntent = signInClient.getSignInIntent();
//                            startActivityForResult(signInIntent, RC_SIGN_IN);
                        }
                    }
                });
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		currentGamesVisible = false;
	}


	// TODO: Get rid of the spinner once done
	private void prepareSavedGamesButtons() {
        Button savedGamesButton = findViewById(R.id.savedGamesButton);
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {

        }
        savedGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SnapshotsClient snapshotsClient = Games.getSnapshotsClient(CurrentGames.this, GoogleSignIn.getLastSignedInAccount(CurrentGames.this));
                int maxNumberOfSavedGamesToShow = 5;

                Task<Intent> intentTask = snapshotsClient.getSelectSnapshotIntent("See My Saves", true, true, maxNumberOfSavedGamesToShow);

                intentTask.addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_SAVED_GAMES);
                    }
                });
            }
        });
	}

	// TODO: Fix UI as well as the way this is all handled
	private void prepareCurrentGamesButtons() {
        try {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("fields", "data, paging");
            GameRequest getCurrentGamesRequest = new GameRequest();
            getCurrentGamesRequest.createNewGameRequest("/me/apprequests", parameters, HttpMethod.GET);
            ArrayList<Long> activeGameRequests = (ArrayList<Long>) new GraphRequests().execute(getCurrentGamesRequest).get();
            currentGames = new HashMap<Long, Game>();
            for (long requestID : activeGameRequests) {
                Map<String, String> gameDataRequestParameters = new HashMap<String, String>();
                gameDataRequestParameters.put("fields", "id, action_type, application, created_time, data, from, message, object, to");
                GameRequest getGameDataRequest = new GameRequest();
                getGameDataRequest.createNewGameRequest("/" + requestID, gameDataRequestParameters, HttpMethod.GET);
                try {
                    com.varunbatta.titanictictactoe.Game activeGame = (com.varunbatta.titanictictactoe.Game) new GraphRequests().execute(getGameDataRequest).get();
                    currentGames.put(requestID, activeGame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            for (final Long requestID : currentGames.keySet()) {
                Button gameButton = new Button(context);
                Player fromPlayer = new Player();
                if (currentGames.get(requestID).lastMove.equals("X")) {
                    fromPlayer = currentGames.get(requestID).player1;
                } else if (currentGames.get(requestID).lastMove.equals("O")) {
                    fromPlayer = currentGames.get(requestID).player2;
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(30, 0, 30, 30);
                gameButton.setLayoutParams(layoutParams);
                gameButton.setText(fromPlayer.playerName + " - Level " + currentGames.get(requestID).level);
                gameButton.setTextColor(getResources().getColor(R.color.colorWhite));
                gameButton.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                gameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        final String[] options = {"Play Game", "View Game", "Forfeit"};
                        new AlertDialog.Builder(CurrentGames.this)
                                .setTitle("What would you like to do?")
                                .setItems(options, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int i) {
                                        dialogListener(options[i], requestID, (Button) view);
                                    }
                                })
                                .create()
                                .show();
                    }
                });
                currentGamesLayout.addView(gameButton, currentGameIndex);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        currentGamesLayout.invalidate();
    }

    // Handles the choice made by user when selecting an action on the dialog
	private void dialogListener(String option, final long requestID, final Button selectedButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(CurrentGames.this);
	    AlertDialog dialog;
		// TODO: Might want to change these options to an enum or something
	    switch(option) {
		case "Play Game":
			Index.receiving = true;
            Intent board = new Intent(getApplicationContext(), Board.class);
            board.putExtra("Level", currentGames.get(requestID).level);
            board.putExtra("Player 1 Name", currentGames.get(requestID).player1.playerName);
            board.putExtra("Player 2 Name", currentGames.get(requestID).player2.playerName);
            if (currentGames.get(requestID).lastMove.equals("X")) {
                board.putExtra("Pending Player", currentGames.get(requestID).player1.playerFBID);
                board.putExtra("Current Player", currentGames.get(requestID).player2.playerFBID);
            } else {
                board.putExtra("Pending Player", currentGames.get(requestID).player2.playerFBID);
                board.putExtra("Current Player", currentGames.get(requestID).player1.playerFBID);
            }
            board.putExtra("Caller", "Current Games");
            board.putExtra("My Turn", true);
            board.putExtra("Finished", false);
            board.putExtra("Multiplayer", true);
            board.putExtra("GameRequestID", requestID);
            startActivity(board);
			break;
		case "View Game":
            board = new Intent(getApplicationContext(), Board.class);
            board.putExtra("Level", currentGames.get(requestID).level);
            board.putExtra("Player 1 Name", currentGames.get(requestID).player1.playerName);
            board.putExtra("Player 2 Name", currentGames.get(requestID).player2.playerName);
            if (currentGames.get(requestID).lastMove.equals("X")) {
                board.putExtra("Pending Player", currentGames.get(requestID).player1.playerFBID);
                board.putExtra("Current Player", currentGames.get(requestID).player2.playerFBID);
            } else {
                board.putExtra("Pending Player", currentGames.get(requestID).player2.playerFBID);
                board.putExtra("Current Player", currentGames.get(requestID).player1.playerFBID);
            }
            board.putExtra("Caller", "Current Games");
            board.putExtra("My Turn", false);
            board.putExtra("Finished", false);
            board.putExtra("Multiplayer", true);
            board.putExtra("GameRequestID", requestID);
            startActivity(board);
			break;
		case "Forfeit":
			builder.setTitle("Forfeit?")
				.setMessage("Are you sure you want to forfeit?")
				.setPositiveButton("Forfeit", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						forfeitGame(requestID, selectedButton);
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
		}
	}

	private void forfeitGame(long requestID, Button selectedButton) {
        Game forfeitedGame = currentGames.get(requestID);
	    Player toPlayer;
        Player fromPlayer;
        if (forfeitedGame.lastMove.equals("O")) {
            toPlayer = forfeitedGame.player2;
            fromPlayer = forfeitedGame.player1;
        } else {
            toPlayer = forfeitedGame.player1;
            fromPlayer = forfeitedGame.player2;
        }
        String messageText = fromPlayer.playerName + " has forfeit the game. You win!";
        String titleText = "Forfeit";
        String game = gameDataToString(forfeitedGame);
        Map<String, String> params = new HashMap<String, String>();
        params.put("data", game);
        params.put("message", messageText);
        params.put("title", titleText);
        GameRequestContent.Builder requestContent = new GameRequestContent.Builder();
        requestContent.setRecipients(Arrays.asList("" + toPlayer.playerFBID));
        requestContent.setMessage(params.get("message"));
        requestContent.setData(params.get("data"));
        requestContent.setTitle(params.get("title"));
        requestContent.setActionType(GameRequestContent.ActionType.TURN);

        GameRequestDialog forfeitDialog = new GameRequestDialog(this);
        forfeitDialog.show(requestContent.build());

        GameRequest deleteRequest = new GameRequest();
        deleteRequest.createNewGameRequest("/" + requestID, null, HttpMethod.DELETE);
        new GraphRequests().execute(deleteRequest);
		currentGamesLayout.removeView(selectedButton);
		currentGamesLayout.invalidate();
	}

    private String gameDataToString(Game forfeitedGame) {
        StringBuilder game = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (forfeitedGame.data[i][j] != null) {
                    game.append(forfeitedGame.data[i][j]);
                }
                game.append(",");
            }
            game.append(";");
        }
        game.append(forfeitedGame.data[forfeitedGame.data.length - 1][0]).append(",");
        game.append(forfeitedGame.data[forfeitedGame.data.length - 1][1]).append(",");
        game.append(forfeitedGame.data[forfeitedGame.data.length - 1][4]).append(",");
        game.append(forfeitedGame.level).append(",,,,,,;");
        return game.toString();
    }

	@Override
	public void onConnectionFailed(ConnectionResult result) {
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
            // TODO: Show error dialog (using GooglePlayServicesUtil.getErrorDialog()?)
//            showErrorDialog(result.getErrorCode());
            resolvingError = true;
        }
	}

	@Override
	public void onConnected(Bundle connectionResult) {
	    // TODO: Are these logs necessary or can we just leave this blank?
		Log.d("OCCG", "Connected CurrentGames");
	}

	@Override
	public void onConnectionSuspended(int result) {
	    // TODO: Should we do anything else like let the user know?
		Log.d("CS", "Connection Suspended");
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
	
    // TODO: Document this code and refactor as necessary
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent intent) {
	    super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = "Error, Unable to Sign In";
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
		if (intent != null) {
	        if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
	        	// Load a snapshot.
	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
	                    intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
	            final String snapshotNm = snapshotMetadata.getUniqueName();

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
	            String player1 = playerNames[0];
	            String player2 = playerNames[1];
	            int level = Integer.parseInt(playerNames[2].substring(5, playerNames[2].length()));

	            Intent board = new Intent(context, Board.class);
	            board.putExtra("Multiplayer", false);
	            board.putExtra("Caller", "CurrentGames");
	            board.putExtra("Level", level);
	            board.putExtra("Player 1 Name", player1);
	            board.putExtra("Player 2 Name", player2);
	            board.putExtra("Saved Game", gameData);
	            startActivity(board);

	        } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
	            // Create a new snapshot named with a unique string
	        	final String snapshotNm = Board.game.player1.playerName + "-" + Board.game.player2.playerName + "-" + "Level" + Board.level;

	        	AsyncTask<Void, Void, Boolean> updateTask = new AsyncTask<Void, Void, Boolean>() {
	                @Override
	                protected Boolean doInBackground(Void... params) {
	                    Snapshots.OpenSnapshotResult open = Games.Snapshots.open(
	                            client, snapshotNm, true).await();

	                    if (!open.getStatus().isSuccess()) {
	                        Log.w("!success", "Could not open Snapshot for update.");
	                        return false;
	                    }

	                    // Change data but leave existing metadata
	                    Snapshot snapshot = open.getSnapshot();
	                    snapshot.getSnapshotContents().writeBytes(wincheckerToByteArray(Board.winCheck));

	                    Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
	                            client, snapshot, SnapshotMetadataChange.EMPTY_CHANGE).await();

	                    if (!commit.getStatus().isSuccess()) {
	                        Log.w("!success", "Failed to commit Snapshot.");
	                        return false;
	                    }

	                    // No failures
	                    return true;
	                }
	            };
	            updateTask.execute();
	        }
	    }
	}

    // TODO: Can this be a global function?
	private byte[] wincheckerToByteArray(String[][] wincheck) {
        StringBuilder game = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (wincheck[i][j] != null) {
                    game.append(wincheck[i][j]);
                }
                game.append(",");
            }
            game.append(";");
        }
        game.append(wincheck[wincheck.length - 1][0]).append(",");
        game.append(wincheck[wincheck.length - 1][1]).append(",");
        game.append(wincheck[wincheck.length - 1][4]).append(",");
        game.append(Board.level).append(",,,,,,;");
        return game.toString().getBytes();
	}
}
