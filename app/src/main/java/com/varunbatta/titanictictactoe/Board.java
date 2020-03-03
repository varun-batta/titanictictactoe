package com.varunbatta.titanictictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.HttpMethod;
import com.facebook.share.widget.GameRequestDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class Board extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    // TODO: See how many of these variables are required
	static Game game = null;
	TextView playerTurn;
	long requestID;
	static String turn;
	String caller;
    static int level;
	int row;
	int column;
	public static Hashtable<Integer, Button> keys = new Hashtable<>(81);
	byte [] onGoingMatch = null;
	static String [][] winCheck = new String [10][9];
    ButtonPressed bp;
	Context context;
	LinearLayout bottomPanel;
	LinearLayout boardLayout;
	boolean myTurn;
	boolean savedGame;
	boolean finished;
	boolean canRematch;
	static boolean multiplayer;
	boolean test;
	boolean boardVisible;
	boolean saveCalled = false;
	boolean onCreateCalled = false;
	static boolean winOrTie = false;
	
	public static GoogleApiClient client;
	
	private static final int RC_SAVED_GAMES = 9009;
	
	boolean resolvingError;
	
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    static FrameLayout progressBarHolder;
    
    BasicBoardView board;
    
    static SharedPreferences sharedPreferences;

    static Board boardActivity;

    CallbackManager callbackManager;
    static GameRequestDialog requestDialog;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        onCreateCalled = true;
		boardVisible = true;
		setContentView(R.layout.board);
		context = getApplicationContext();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		level = getIntent().getIntExtra("Level", 0);
		onGoingMatch = getIntent().getByteArrayExtra("On Going Match");
		caller = getIntent().getStringExtra("Caller");
		myTurn = getIntent().getBooleanExtra("My Turn", true);
		finished = getIntent().getBooleanExtra("Finished", false);
		canRematch = getIntent().getBooleanExtra("Can Rematch", true);
		multiplayer = getIntent().getBooleanExtra("Multiplayer", false);
		test = getIntent().getBooleanExtra("Test", false);
		savedGame = getIntent().getBooleanExtra("Saved Game", false);
		requestID = getIntent().getLongExtra("GameRequestID", 0);

		if (requestID != 0) {
		    if (caller.equals("Index")) {
                game = Index.availableGames.get(requestID);
            } else if (caller.equals("Current Games")) {
		        game = CurrentGames.currentGames.get(requestID);
            }
        }

        if (game == null) {
		    game = new Game();
		    game.level = level;

		    // TODO: See if there's a better way to populate this data
		    Player player1 = new Player();
		    player1.playerName = getIntent().getStringExtra("Player 1 Name");
		    player1.playerFBID = getIntent().getLongExtra("Player 1 FBID", 0);

		    Player player2 = new Player();
		    player2.playerName = getIntent().getStringExtra("Player 2 Name");
		    player2.playerFBID = getIntent().getLongExtra("Player 2 FBID", 0);

		    game.player1 = player1;
		    game.player2 = player2;
        }

		bp = new ButtonPressed(context, level, game, this);
		// TODO: See if this is required
        boardActivity = this;

        // TODO: Initialize this as required, if necessary
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
		client.connect();
		
		progressBarHolder = findViewById(R.id.progressBarHolder);
		boardLayout = findViewById(R.id.boardLayout);

		// TODO: Why does this have to be done here?
		TextView levelTitle = findViewById(R.id.levelTitle);
		levelTitle.setTextColor(Color.BLACK);
		levelTitle.setGravity(Gravity.CENTER);
		levelTitle.setTextSize(30);

		// TODO: Clean up this code so that it makes more sense!!
        if (onGoingMatch != null) {
            savedGameRecreate(onGoingMatch);
            game = new Game();
            game.data = winCheck;
            game.level = level;

            Player player1 = new Player();
            player1.playerName = getIntent().getStringExtra("Player 1 Name");
            player1.playerFBID = getIntent().getLongExtra("Player 1 FBID", 0);

            Player player2 = new Player();
            player2.playerName = getIntent().getStringExtra("Player 2 Name");
            player2.playerFBID = getIntent().getLongExtra("Player 2 FBID", 0);

            game.player1 = player1;
            game.player2 = player2;
        }

		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		board = findViewById(R.id.board);
        board.init(width, level);
        board.metaRow = 0;
        board.metaColumn = 0;
        BasicBoardView.winCheck = game.data;
		board.configureBoard(width, level, level, this);

		for (Button button : Board.keys.values()) {
		    button.setEnabled(true);
        }

        // TODO: Clean this up!!
		if (game.data[9][2] != null && !game.data[9][2].equals("") && level >= 2) {
            int row = Integer.parseInt(game.data[9][0]);
            int column = Integer.parseInt(game.data[9][1]);
            bp.boardChanger(row, column, level, true);
        }

		// TODO: All this setup should go in a different function if necessary
		switch(level) {
		case 1:
			levelTitle.setText("Tic");
			levelTitle.setBackgroundColor(getColor(R.color.colorGreen));
			boardLayout.setBackgroundColor(getColor(R.color.colorGreen));
			break;
		case 2:
			levelTitle.setText("Tic Tac");
			levelTitle.setBackgroundColor(getColor(R.color.colorGreen));
			boardLayout.setBackgroundColor(getColor(R.color.colorGreen));
			break;
		case 3:
			levelTitle.setText("Tic Tac Toe");
//			levelTitle.setBackgroundColor(Color.rgb(255, 154, 0));
			break;
		case 4:
			levelTitle.setText("T4");
//			levelTitle.setBackgroundColor(Color.CYAN);
		}

		// TODO: Clean up this view and logic
        bottomPanel = findViewById(R.id.bottom_panel);
		playerTurn = findViewById(R.id.player_turn);
		playerTurn.setTextColor(Color.BLACK);
		playerTurn.setText(game.player1.playerName + "'s Turn");

		// TODO: Once again, this setup might be better placed elsewhere
        if (multiplayer) {
            FacebookSdk.sdkInitialize(this.getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            requestDialog = new GameRequestDialog(this);
            requestDialog.registerCallback(callbackManager,
                    new FacebookCallback<GameRequestDialog.Result>() {
                        public void onSuccess(GameRequestDialog.Result result) {
                            // Game Request for making a move
                        	GameRequest deleteRequest = new GameRequest();
                            deleteRequest.createNewGameRequest("/" + game.requestID, null, HttpMethod.DELETE);
                            new GraphRequests().execute(deleteRequest);
                            // TODO: Clean up this logic a bit if necessary
                            if (Board.winOrTie) {
                                String winnerName = turn.equals("X") ? game.player1.playerName : game.player2.playerName;
                                finishActivity(context, true, winnerName);
                            }
                        }
                        public void onCancel() {
                            //TODO: Handle Recreation of Game Before Move
                        }
                        public void onError(FacebookException error) {}
                    }
            );
        }
	}

    @Override
	protected void onResume() {
		super.onResume();
		if (boardLayout.findViewById(R.id.bottomPanel) == null) {
			// TODO: Might wanna take a look at this logic again
			if (game.lastMove.contains("X")) {
				// TODO: Fix text allocation with string formatting
				playerTurn.setText(game.player1.playerName + "'s Turn");
			} else if (game.lastMove.contains("O")) {
				playerTurn.setText(game.player2.playerName + "'s Turn");
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);
		// TODO: See what this is for (the screen being closed?)
		instanceState.putByteArray("On Going Match", ButtonPressed.winCheckerToByteArray(ButtonPressed.winCheck));

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("On Going Match", Base64.encodeToString(ButtonPressed.winCheckerToByteArray(ButtonPressed.winCheck), Base64.DEFAULT));
		editor.commit();
	}
	
	@Override
	public void onRestoreInstanceState(Bundle instanceState) {
		super.onRestoreInstanceState(instanceState);
		// TODO: See what this is for (the screen being awakened?)
		onGoingMatch = instanceState.getByteArray("On Going Match");
	}

	public void savedGameRecreate(byte [] onGoingMatch) {
		String gameArray = new String(onGoingMatch);
		String [] rows = gameArray.split(";");
		String [][] game = new String [rows.length][];
		for(int l = 0; l < rows.length; l++) {
			game[l] = rows[l].split(",");
		}
		winCheck = game;
	}
	
	/*
	 * Purpose: To check whether the latest move was a winning move
	 * Input:
	 *  rowIndex - row index
	 *  columnIndex - column index
	 *  testingLevel - testing level
	 *  actualLevel - actual level
	 *  winChecker - array to check
	 *  turnValue - value of latest turn X or O
	 * Output: true if won, false otherwise
	 */
	public static boolean winChecker(int rowIndex, int columnIndex, int testingLevel, int actualLevel, String[][] winChecker, String turnValue) {
		//TODO: Refactor this function! (or deprecate)
		String value = "";
		String value1 = "";
		String value2 = "";
		String winningTurnValue = "";

		// TODO: Handle the edits on rowIndex, columnIndex based on the testingLevel and actualLevel values
		if (testingLevel == 3 && actualLevel == 4) {
			rowIndex /= 3;
			columnIndex /= 3;
		}
		if (testingLevel == 2 && actualLevel >= 3) {
			rowIndex /= 3;
			columnIndex /= 3;
		}
		if ( testingLevel == 1 && actualLevel >= 2 ) {
			rowIndex /= 3;
			columnIndex /= 3;
		}

		// Checking the columns
		// TODO: Cleanup this approach
		if (rowIndex%3 == 0) {
			value = winChecker[rowIndex][columnIndex];
			value1 = winChecker[rowIndex+1][columnIndex];
			value2 = winChecker[rowIndex+2][columnIndex];
		} else if (rowIndex%3 == 1) {
			value = winChecker[rowIndex-1][columnIndex];
			value1 = winChecker[rowIndex][columnIndex];
			value2 = winChecker[rowIndex+1][columnIndex];
		} else if (rowIndex%3 == 2) {
			value = winChecker[rowIndex-2][columnIndex];
			value1 = winChecker[rowIndex-1][columnIndex];
			value2 = winChecker[rowIndex][columnIndex];
		}

		// Seeing if all cells in the column have the same value
		if (!(value == null || value1 == null || value2 == null) && value.equals(value1) && value1.equals(value2)) {
			winningTurnValue = turnValue;
		}

		// Checking the rows
		// TODO: Cleanup this approach
		if (columnIndex%3 == 0) {
			value = winChecker[rowIndex][columnIndex];
			value1 = winChecker[rowIndex][columnIndex+1];
			value2 = winChecker[rowIndex][columnIndex+2];
		} else if (columnIndex%3 == 1) {
			value = winChecker[rowIndex][columnIndex-1];
			value1 = winChecker[rowIndex][columnIndex];
			value2 = winChecker[rowIndex][columnIndex+1];
		} else if (columnIndex%3 == 2) {
			value = winChecker[rowIndex][columnIndex-2];
			value1 = winChecker[rowIndex][columnIndex-1];
			value2 = winChecker[rowIndex][columnIndex];
		}

		// Seeing if all cells in the row have the same value
		if (!(value == null || value1 == null || value2 == null) && value.equals(value1) && value1.equals(value2)) {
			winningTurnValue = turnValue;
		}

		// Checking the top-left to bottom-right diagonal
		// TODO: Cleanup this approach
		if (rowIndex%3 == 0 && columnIndex%3 == 0) {
			value = winChecker[rowIndex][columnIndex];
			value1 = winChecker[rowIndex+1][columnIndex+1];
			value2 = winChecker[rowIndex+2][columnIndex+2];
		} else if (rowIndex%3 == 1 && columnIndex%3 == 1) {
			value = winChecker[rowIndex-1][columnIndex-1];
			value1 = winChecker[rowIndex][columnIndex];
			value2 = winChecker[rowIndex+1][columnIndex+1];
		} else if (rowIndex%3 == 2 && columnIndex%3 == 2) {
			value = winChecker[rowIndex-2][columnIndex-2];
			value1 = winChecker[rowIndex-1][columnIndex-1];
			value2 = winChecker[rowIndex][columnIndex];
		}

		// Seeing if all cells in the diagonal have the same value
		if (!(value == null || value1 == null || value2 == null) && value.equals(value1) && value1.equals(value2)) {
			winningTurnValue = turnValue;
		}

		// Checking the top-right to bottom-left diagonal
		// TODO: Cleanup this approach
		if (rowIndex%3 == 2 && columnIndex%3 == 0) {
			value = winChecker[rowIndex][columnIndex];
			value1 = winChecker[rowIndex-1][columnIndex+1];
			value2 = winChecker[rowIndex-2][columnIndex+2];
		} else if(rowIndex%3 == 1 && columnIndex%3 == 1) {
			value = winChecker[rowIndex+1][columnIndex-1];
			value1 = winChecker[rowIndex][columnIndex];
			value2 = winChecker[rowIndex-1][columnIndex+1];
		} else if(rowIndex%3 == 0 && columnIndex%3 == 2) {
			value = winChecker[rowIndex+2][columnIndex-2];
			value1 = winChecker[rowIndex+1][columnIndex-1];
			value2 = winChecker[rowIndex][columnIndex];
		}

		// Seeing if all the cells in the diagonal have the same value
		if (!(value == null || value1 == null || value2 == null) && value.equals(value1) && value1.equals(value2)) {
			winningTurnValue = turnValue;
		}

		// If there is a valid winning value then put it in the metaWinCheck array
		// TODO: See if this check is required
		if (winningTurnValue.equals("X") || winningTurnValue.equals("O")) {
			ButtonPressed.metaWinCheck[rowIndex/3][columnIndex/3] = winningTurnValue;
			return true;
		}
		
		return false;
	}

	public void winningBoardChanger(final int rowIndex, final int columnIndex, final int level, int actual, final String x, Context game, String [][] winCheck) {
		// TODO: Why are these global variables required?
		row = rowIndex;
		column = columnIndex;
		bp = new ButtonPressed(game, level, this.game, boardActivity);
		Board.winCheck = winCheck;

		if(actual == 2){
			// TODO: See if there's a better way to do this
			ButtonPressed.metaWinCheck[rowIndex/3][columnIndex/3] = x;
			BasicBoardView miniBoard = BasicBoardView.metaBoard[rowIndex/3][columnIndex/3];
			miniBoard.findViewById(R.id.boardBackground).setAlpha(0);
			miniBoard.findViewById(R.id.boardBackgroundRed).setAlpha(0);
			miniBoard.findViewById(R.id.overlaying_linear_layout).setAlpha(0);

			TextView won = new TextView(game);
			won.setLayoutParams(miniBoard.getLayoutParams());
			won.setText(x);
			won.setTextSize(75);
			won.setTextColor(Color.BLACK);
			won.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			miniBoard.addView(won);

			// Create a timed UI event to handle the reload (as necessary)
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					// If you want to operate UI modifications, you must run ui stuff on UiThread.
					Board.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!Index.facebookGame) {
								if (Index.receiving || NotificationService.receiving) {
									bp.boardChanger(row, column, 2, true);
								} else {
									bp.boardChanger(row, column, 2, !multiplayer);
								}
							}
							if ( bp.tieChecker("Outer", level, row, column) ) {
								if ( multiplayer ) {
									Log.d("tieChecker", "finishGame");
									bp.finishGame(true);
								} else {
									finishActivity(context, true, "Tie");
								}
							}
						}
					});
					Board.winOrTie = bp.winChecker(rowIndex, columnIndex, 1, 2, ButtonPressed.metaWinCheck, x);
				}
			}, 500);
		}
	}
	
	public void finishActivity(Context context, boolean won, String winnerName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        // TODO: Clean this up as necessary
        if (won) {
		    winOrTie = false;
			Intent winner = new Intent(context, Winner.class);
			winner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			winner.putExtra("Multiplayer", multiplayer);
			winner.putExtra("Winner", winnerName);
			winner.putExtra("Request ID", requestID);
			context.startActivity(winner);
		} else {
			keys = new Hashtable<>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			Intent menu = new Intent(context, LevelMenu.class);
			menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(multiplayer) {
				menu.putExtra("Multiplayer", multiplayer);
			}
			menu.putExtra("Caller", "Board");
			menu.putExtra("Instructions", false);
			context.startActivity(menu);
		}

		this.finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Overridden to handle the back keystroke in Android
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			keys = new Hashtable<>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.winCheck = new String [10][9];
			ButtonPressed.metaWinCheck = new String [3][3];
			Board.winCheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	// TODO: Deprecate after redesign (if necessary)
	public void bottomPanelListener(Context context, String pressedButtonText) {
		this.finish();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
		switch(pressedButtonText) {
		case "Menu":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.winCheck = new String [10][9];
			ButtonPressed.metaWinCheck = new String [3][3];
			Board.winCheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent menu = new Intent(context, LevelMenu.class);
			menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(multiplayer) {
				menu.putExtra("Multiplayer", multiplayer);
			}
			menu.putExtra("Caller", "Board");
			menu.putExtra("Instructions", false);
			context.startActivity(menu);
			break;
		case "Main Menu":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.winCheck = new String [10][9];
			ButtonPressed.metaWinCheck = new String [3][3];
			Board.winCheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent mainMenu = new Intent(context, MainMenu.class);
			mainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mainMenu);
			break;
		case "New Game":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.winCheck = new String [10][9];
			ButtonPressed.metaWinCheck = new String [3][3];
			Board.winCheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent newGame = new Intent(context, PlayerNames.class);
			newGame.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newGame);
			break;
		case "New WiFi Game":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.winCheck = new String [10][9];
			ButtonPressed.metaWinCheck = new String [3][3];
			Board.winCheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent newWifiGame = new Intent(context, LevelMenu.class);
			newWifiGame.putExtra("Multiplayer", true);
			newWifiGame.putExtra("Caller", "Board");
			newWifiGame.putExtra("Instructions", false);
			newWifiGame.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newWifiGame);
			break;
		}
	}

	// TODO: Deprecate this if not necessary, else try to clean this networking stuff up
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
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            resolvingError = true;
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (saveCalled) {
			saveCalled = false;
			int maxNumberOfSavedGamesToShow = 5;
		    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(client,
		            "Saved Games", true, true, maxNumberOfSavedGamesToShow);
		    startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
		}
	}

	@Override
	public void onConnectionSuspended(int result) {
		//TODO: Figure out what to do here
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
	    // Adding check to make sure callbackManager is not null (always true if multiplayer is true)
		if (multiplayer) {
	    	callbackManager.onActivityResult(requestCode, resultCode, intent);
		}
		if (intent != null) {
	        if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
	        	// Load a snapshot.
	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
	                    intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
	            final String snapshotNm = snapshotMetadata.getUniqueName();
				//TODO: Find purpose for snapshotNm

	            AlertDialog.Builder builder = new AlertDialog.Builder(Board.this);
				builder.setTitle("Save over?")
					.setMessage("Are you sure you want to save over this game?\n" + snapshotMetadata.getDescription())
					.setPositiveButton("Save Over", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							saveOver(intent);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//TODO: What happens when cancelling
						}
					});
			    AlertDialog dialog = builder.create();
			    dialog.show();
	        } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
	            // Create a new snapshot named with a unique string
	        	final String snapshotNm = game.player1.playerName + "-" + game.player2.playerName + "-" + "Level" + level;
	        	//TODO: Find purpose of unique name
	        }
	    }
	}
	
	private void saveOver(Intent intent) {
    	// Load a snapshot.
        SnapshotMetadata snapshotMetadata = intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
        final String snapshotNm = snapshotMetadata.getUniqueName();
		//TODO: Use name to load snapshot

		// TODO: Eliminiate this AsyncTask if not necessary
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
        	@Override
            protected void onPreExecute() {
                super.onPreExecute();
                inAnimation = new AlphaAnimation(0f, 1f);
                inAnimation.setDuration(200);
                progressBarHolder.setAnimation(inAnimation);
                progressBarHolder.setVisibility(View.VISIBLE);
    		}

        	@Override
            protected Boolean doInBackground(Void... params) {
                // Open the saved game using its name.
				//TODO: Figure out how best to do that
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

        task.execute();
	}
}
