package com.varunbatta.titanictictactoe;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.varunbatta.titanictictactoe.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayGame extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	
	public static GoogleApiClient client;
	
	LinearLayout playGameLayout;
	
	Context context;
	
	private static final int RC_SAVED_GAMES = 9009;
	
	boolean resolvingError;
	
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    private byte [] gameData = null;
    private boolean success = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playgame);
		
		context = getApplicationContext();
		
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
		
		playGameLayout = (LinearLayout) findViewById(R.id.playGameLayout);
		playGameLayout.setBackgroundColor(Color.rgb(0, 153, 153));
		
		TextView title = new TextView(context);
		title.setText("Pass-By-Pass");
		title.setTextSize(40);
		title.setTextColor(Color.BLACK);
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		playGameLayout.addView(title);
		
		TextView subtitle = new TextView(context);
		subtitle.setText("Please Select:");
		subtitle.setTextSize(30);
		subtitle.setTextColor(Color.BLACK);
		subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
		playGameLayout.addView(subtitle);
		
		Button newGame = new Button(context);
		newGame.setText("New Game");
		newGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				selection(buttonText);
			}
		});
		playGameLayout.addView(newGame);
		
		Button loadGame = new Button(context);
		loadGame.setText("Load Game");
		loadGame.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String buttonText = button.getText().toString();
				selection(buttonText);
			}
		});
		playGameLayout.addView(loadGame);
	}
	
	private void selection(String buttonText) {
		switch(buttonText) {
		case "New Game":
			Intent newGame = new Intent(context, PlayerNames.class);
			startActivity(newGame);
			break;
		case "Load Game":
			client.connect();
		    break;
		}
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
	public void onConnected(Bundle connectionHint) {
	    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(client,
	            "Saved Games", false, true, Snapshots.DISPLAY_LIMIT_NONE);
	    startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
	}

	@Override
	public void onConnectionSuspended(int result) {
		Log.d("CS", "Connection Suspended");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent intent) {
	    Log.d("oAR", "onActivityResult");
		if (intent != null) {
	        if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
	            Log.d("LG", "Load Game");
	        	// Load a snapshot.
	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
	                    intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
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
