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

    static Game game;
	TextView playerTurn;
	long requestID;
	static String turn;
	String caller;
    static int level;
	int row;
	int column;
	public static Hashtable<Integer, Button> keys = new Hashtable<Integer, Button>(81);
	byte [] onGoingMatch = null;
	static String [][] wincheck = new String [10][9];
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
		BoardAdapter.numberOfTimesPositionIsZero = 0;
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
        } else {
		    game = null;
        }

        if (game == null) {
		    game = new Game();
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

		bp = new ButtonPressed(context, level, game, this);
		boardActivity = this;

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
		TextView levelTitle = findViewById(R.id.levelTitle);
		levelTitle.setTextColor(Color.BLACK);
		levelTitle.setGravity(Gravity.CENTER);
		levelTitle.setTextSize(30);

        if (onGoingMatch != null) {
            savedGameRecreate(onGoingMatch);
            game = new Game();
            game.data = wincheck;
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
        BasicBoardView.wincheck = game.data;
		board.configureBoard(width, level, level, this);

		for (Button button : Board.keys.values()) {
		    button.setEnabled(true);
        }

        if (game.data[9][2] != null && !game.data[9][2].equals("") && level >= 2) {
            int row = Integer.parseInt(game.data[9][0]);
            int column = Integer.parseInt(game.data[9][1]);
            bp.boardChanger(row, column, level, true);
        }

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
		
        bottomPanel = findViewById(R.id.bottom_panel);
		playerTurn = findViewById(R.id.player_turn);
		playerTurn.setTextColor(Color.BLACK);
		playerTurn.setText(game.player1.playerName + "'s Turn");

        if (multiplayer) {
            FacebookSdk.sdkInitialize(this.getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            requestDialog = new GameRequestDialog(this);
            requestDialog.registerCallback(callbackManager,
                    new FacebookCallback<GameRequestDialog.Result>() {
                        public void onSuccess(GameRequestDialog.Result result) {
                            GameRequest deleteRequest = new GameRequest();
                            deleteRequest.createNewGameRequest("/" + game.requestID, null, HttpMethod.DELETE);
                            new GraphRequests().execute(deleteRequest);
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
        if(boardLayout.findViewById(R.id.bottomPanel) == null) {
            if(game.lastMove.contains("X")) {
                playerTurn.setText(game.player1.playerName + "'s Turn");
            } else if (game.lastMove.contains("O")) {
                playerTurn.setText(game.player2.playerName + "'s Turn");
            }
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

    @Override
    protected void onStop() {
        super.onStop();
    }
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);
		instanceState.putByteArray("On Going Match", ButtonPressed.wincheckerToByteArray(ButtonPressed.wincheck));

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("On Going Match", Base64.encodeToString(ButtonPressed.wincheckerToByteArray(ButtonPressed.wincheck), Base64.DEFAULT));
		editor.commit();
	}
	
	@Override
	public void onRestoreInstanceState(Bundle instanceState) {
		super.onRestoreInstanceState(instanceState);
		onGoingMatch = instanceState.getByteArray("On Going Match");
	}

	public void savedGameRecreate(byte [] onGoingMatch) {
		String gameArray = new String(onGoingMatch);
		String [] rows = gameArray.split(";");
		String [][] game = new String [rows.length][];
		for(int l = 0; l < rows.length; l++) {
			game[l] = rows[l].split(",");
		}
		wincheck = game;
	}
	
	/*
	 * Purpose: To check whether the latest move was a winning move
	 * Input:
	 *  f - row index
	 *  g - column index
	 *  n - testing level
	 *  actual - actual level
	 *  winchecker - array to check
	 *  turnValue - value of latest turn X or O
	 * Output: true if won, false otherwise
	 */
	public static boolean winChecker(int f, int g, int n, int actual, String[][] winchecker, String turnValue) {
		//TODO: Refactor this function!
		String value = "";
		String value1 = "";
		String value2 = "";
		String x = "";
		int q = -1;
		int r = -1;
		int length = winchecker.length;
		int width = winchecker[0].length;
		
		if(n==3&&actual==4){ 
			q = f;
			r = g;
			f=f/3;
			g=g/3;}
		if(n==2&&actual>=3){
			q=f;
			r=g;
			f=f/3;
			g=g/3;}
		if(n==1&&actual>=2){
			q=f;
			r=g;
			f=f/3;
			g=g/3;}
		
		if(f%3==0){
			value = winchecker[f][g];
			value1 = winchecker[f+1][g];
			value2 = winchecker[f+2][g];
		}
		else{
			if(f%3==1){
				value = winchecker[f-1][g];
				value1 = winchecker[f][g];
				value2 = winchecker[f+1][g];
				}
			else{
				if(f%3==2){
					value = winchecker[f-2][g];
					value1 = winchecker[f-1][g];
					value2 = winchecker[f][g];
				}
			}
		}
		if(value == null||value1 == null||value2==null){
		}
		else{
			if(value.equals(value1) && value1.equals(value2)){
				x = turnValue;
			}
		}
		
		if(g%3==0){
			value = winchecker[f][g];
			value1 = winchecker[f][g+1];
			value2 = winchecker[f][g+2];
			}
		else{
			if(g%3==1){
				value = winchecker[f][g-1];
				value1 = winchecker[f][g];
				value2 = winchecker[f][g+1];
			}
			else{
				if(g%3==2){
					value = winchecker[f][g-2];
					value1 = winchecker[f][g-1];
					value2 = winchecker[f][g];
				}
			}}
		
		
		if(value == null||value1 == null||value2==null){}
		else{
			if(value.equals(value1) && value1.equals(value2)){
				x = turnValue;
			}
		}
		
		if(f%3==0&&g%3==0){
			value = winchecker[f][g];
			value1 = winchecker[f+1][g+1];
			value2 = winchecker[f+2][g+2];}
		else{
			if(f%3==1&&g%3==1){
				value = winchecker[f-1][g-1];
				value1 = winchecker[f][g];
				value2 = winchecker[f+1][g+1];
			}			
			else{
				if(f%3==2&&g%3==2){
					value = winchecker[f-2][g-2];
					value1 = winchecker[f-1][g-1];
					value2 = winchecker[f][g];
				}
			}
		}

		if(value == null||value1 == null||value2==null){}
		else{
			if(value.equals(value1) && value1.equals(value2)){
				x = turnValue;
			}
		}
		if(f%3==2&&g%3==0){
			value = winchecker[f][g];
			value1 = winchecker[f-1][g+1];
			value2 = winchecker[f-2][g+2];}
		else{
			if(f%3==1&&g%3==1){
				value = winchecker[f+1][g-1];
				value1 = winchecker[f][g];
				value2 = winchecker[f-1][g+1];
			}			
			else{
				if(f%3==0&&g%3==2){
					value = winchecker[f+2][g-2];
					value1 = winchecker[f+1][g-1];
					value2 = winchecker[f][g];
				}
			}
		}
			
		if(value == null||value1 == null||value2==null){}
		else{
			if(value.equals(value1) && value1.equals(value2)){
				x=turnValue;
			}
		}
		if(x.equals("X") || x.equals("O")) {
			ButtonPressed.metawincheck[f/3][g/3] = x;
			return true;
		}
		
		return false;
	}

	public void WinningBoardChanger(final int f, final int g, final int level, int actual, final String x, Context game, String [][] wincheck) {
		row = f;
		column = g;
		bp = new ButtonPressed(game, level, this.game, boardActivity);
		Board.wincheck = wincheck;
		
		switch (level){
		case 1:
			break;
		case 2:
			if(actual == 2){
				ButtonPressed.metawincheck[f/3][g/3] = x;
                BasicBoardView miniBoard = BasicBoardView.metaBoard[f/3][g/3];
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
                        Board.winOrTie = bp.winChecker(f, g, 1, 2, ButtonPressed.metawincheck, x);
					}
				}, 500);
			}
		}
	}
	
	public void finishActivity(Context context, boolean won, String winnerName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
		if(won){
		    winOrTie = false;
			Intent winner = new Intent(context, Winner.class);
			winner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			winner.putExtra("Multiplayer", multiplayer);
			winner.putExtra("Winner", winnerName);
			winner.putExtra("Request ID", requestID);
			context.startActivity(winner);
			this.finish();
		} else {
			keys = new Hashtable<Integer, Button>(6561);
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
			this.finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

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
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
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
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent mainMenu = new Intent(context, MainMenu.class);
			mainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mainMenu);
			break;
		case "New Game":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
			Intent newGame = new Intent(context, PlayerNames.class);
			newGame.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newGame);
			break;
		case "New WiFi Game":
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
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
//            showErrorDialog(result.getErrorCode());
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
	
	private byte[] wincheckerToByteArray(String[][] winchecker) {
		String gameString = "";
		for(int i = 0; i < winchecker.length; i++) {
			for(int j = 0; j < winchecker[0].length; j++) {
				gameString = gameString + winchecker[i][j] + ",";
			}
			gameString = gameString + ";";
		}
		return gameString.getBytes();
	}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
