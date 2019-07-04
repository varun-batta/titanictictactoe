package com.varunbatta.titanictictactoe;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.HttpMethod;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.widget.GameRequestDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.varunbatta.titanictictactoe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Board extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    static Game game;
	TextView playerTurn;
	long requestID;
//	Button save;
//	public static Player player1Player = new Player();
//	public static Player player2Player = new Player();
//	public static String player1 = "";
//	public static String player2 = "";
	static String turn;
//	public static long pendingPlayerId;
	String caller;
    static int level;
	int row;
	int column;
	Button menu;
	public static Hashtable<Integer, Button> keys = new Hashtable<Integer, Button>(81);
	byte [] onGoingMatch = null;
	boolean useIndex = false;
	static String [][] wincheck = new String [10][9];
	String matchId;
//	public static BoardAdapter boardAdapter;
    ButtonPressed bp;
	Context context;
	LinearLayout bottomPanel;
	LinearLayout boardLayout;
	boolean myTurn;
	boolean savedGame;
	boolean finished;
	boolean canRematch;
	static boolean multiplayer;
//	long currentPlayerId;
	boolean test;
//	boolean done = false;
	boolean boardVisible;
	boolean saveCalled = false;
	boolean onCreateCalled = false;
	static boolean winOrTie = false;
	
	public static GoogleApiClient client;
	
	LinearLayout playGameLayout;
	
	private static final int RC_SAVED_GAMES = 9009;
	
	boolean resolvingError;
	
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
//    private byte [] gameData = null;
//    private boolean success = false;
    
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    static FrameLayout progressBarHolder;
    
    boolean cancel;
    
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
		Log.d("sPI", "Shared Preferences Instantiated");
		level = getIntent().getIntExtra("Level", 0);
		onGoingMatch = getIntent().getByteArrayExtra("On Going Match");
//		matchId = getIntent().getStringExtra("Match ID");
//		pendingPlayerId = getIntent().getLongExtra("Pending Player", 0);
//		currentPlayerId = getIntent().getLongExtra("Current Player", 0);
//		String player1Name = getIntent().getStringExtra("Player 1 Name");
//		if(player1Name != null) {
//			player1 = player1Name;
//		}
//		String player2Name = getIntent().getStringExtra("Player 2 Name");
//		if(player2Name != null) {
//			player2 = player2Name;
//		}
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
		Log.d("savedGame", "" + savedGame);
        Log.d("test", "" + test);
		bp = new ButtonPressed(context, level, game, this);
//		if(onGoingMatch != null) {
//			useIndex = true;
//			Log.d("oGM", "not null");
//			savedGameRecreate(onGoingMatch, context);
//		}
		boardActivity = this;

//		Log.d("oC", "Board.onCreate()");
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
		
		progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
		
		boardLayout = (LinearLayout) findViewById(R.id.boardLayout);

		TextView levelTitle = (TextView) findViewById(R.id.levelTitle);
		levelTitle.setTextColor(Color.BLACK);
		levelTitle.setGravity(Gravity.CENTER);
		levelTitle.setTextSize(30);

//		Display display = getWindowManager().getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//		int width = size.x;
//		int height = size.y;
//		height *= 0.75;
		//			height -= 500;
		//			LayoutParams params = new LayoutParams(width, width);
		
		int key = -1;
		
//		if(onGoingMatch != null) {
//			int row = Integer.parseInt(wincheck[wincheck.length - 1][0]);
//			int column = Integer.parseInt(wincheck[wincheck.length - 1][1]);
//
//			switch(n) {
//			case 1:
//				key = row*3 + column;
//				break;
//			case 2:
//				key = row*9 + column;
//				break;
//			}
//		}
		
		
//		board = (GridView) findViewById(R.id.board);
////		if(caller.equals("CurrentGames")) {
////			boardAdapter.notifyDataSetInvalidated();
////			board.setAdapter(null);
////		}
//		if ( board.getChildCount() == 0 ) {
//			board.setNumColumns(3);
//			board.setGravity(Gravity.CENTER);
//			Log.d("Calling", "BoardAdapter");
//			boardAdapter = new BoardAdapter(this, n, n, 0, 0, height, width, onGoingMatch, myTurn, key, savedGame);
//			board.setAdapter(boardAdapter);
//			board.setBackgroundColor(Color.LTGRAY);
//		}

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
		
//		if(test) {
//			levelTitle.setText("Test");
//		}

		//			bottomPanel = (LinearLayout) findViewById(R.id.bottomPanel);
//		if ( bottomPanel == null ) {
//			bottomPanel = new LinearLayout(context);
//            bottomPanel.setId(R.id.bottomPanel);
//		}
        bottomPanel = findViewById(R.id.bottom_panel);

//        Log.d("bPCC", "" + bottomPanel.getChildCount());

//		if ( bottomPanel.getChildCount() == 0 ) {
//			menu = new Button(context);
//			menu.setText("Menu");
//			menu.setOnClickListener(bp);
//
//			save = new Button(context);
//			save.setText("Save");
//			save.setOnClickListener(bp);

//			if(playerTurn == null) {
//				playerTurn = new TextView(context);
//			}
            playerTurn = findViewById(R.id.player_turn);
			playerTurn.setTextColor(Color.BLACK);
//			if(player1 == null) {
//				//			Log.d("Player 1", PlayerNames.player1name);
//				player1 = LevelMenu.player1Name;
//			}
//			if(player2 == null) {
//				//			Log.d("Player 2", PlayerNames.player2name);
//				player2 = LevelMenu.player2Name;
//			}
//			if(onGoingMatch != null) {
//				player1 = wincheck[wincheck.length - 1][2];
//				player2 = wincheck[wincheck.length - 1][3];
//				game.lastMove = wincheck[wincheck.length - 1][4];
//				String currentPlayer = null;
//				if (Plus.PeopleApi.getCurrentPerson(Index.client) != null) {
//				    Person currentPerson = Plus.PeopleApi.getCurrentPerson(Index.client);
//				    currentPlayer = currentPerson.getDisplayName();
//				}
//				if ( (currentPlayer.equals(player1) && !game.lastMove.contains("X")) ||
//						(currentPlayer.equals(player2) && !game.lastMove.contains("O")) ){
//						myTurn = false;
//				}
////				Toast.makeText(context, "myTurn = " + myTurn, Toast.LENGTH_SHORT).show();
////				Toast.makeText(context, "currentTurn = " + currentTurn, Toast.LENGTH_SHORT).show();
//				//				Toast.makeText(context, "player1: " + player1, Toast.LENGTH_SHORT).show();
//				//				Toast.makeText(context, "player2: " + player2, Toast.LENGTH_SHORT).show();
//				if(game.lastMove.contains("X")) {
//					//					Toast.makeText(context, player1 + "'s Turn", Toast.LENGTH_SHORT).show();
//					playerTurn.setText(player1 + "'s Turn");
//				} else if (game.lastMove.contains("O")) {
//					//					Toast.makeText(context, player2 + "'s Turn", Toast.LENGTH_SHORT).show();
//					playerTurn.setText(player2 + "'s Turn");
//				} else {
//					playerTurn.setText(player1 + "'s Turn");
//					game.lastMove = "X";
//				}
//			} else {
//				Toast.makeText(context, "onGoingMatch == null", Toast.LENGTH_SHORT).show();
				playerTurn.setText(game.player1.playerName + "'s Turn");
//				game.lastMove = "X";
//			}
//			Log.d("Player 1", player1);
//			Log.d("Player 2", player2);
//			if(!caller.equals("Winner")) {
//				bottomPanel.addView(playerTurn);
//				if(!multiplayer) {
//					bottomPanel.addView(save);
//				}
//				bottomPanel.addView(menu);
//			} else {
//				Button mainMenu = new Button(context);
//				mainMenu.setText("Main Menu");
//				mainMenu.setOnClickListener(bp);
//				bottomPanel.addView(mainMenu);

//				Button newGame = new Button(context);
//				newGame.setText("New Game");
//				newGame.setOnClickListener(bp);
//				bottomPanel.addView(newGame);

//				Button newWifiGame = new Button(context);
//				newWifiGame.setText("New WiFi Game");
//				newWifiGame.setOnClickListener(bp);
//				bottomPanel.addView(newWifiGame);

				//			if(multiplayer) {
				//				Button rematch = new Button(context);
				//				rematch.setText("Rematch!");
				//				rematch.setOnClickListener(new ButtonPressed(context, n));
				//				bottomPanel2.addView(rematch);
				//			}
//			}
//            Log.d("abP", "adding bottomPanel");
//			if(!test) {
//                Log.d("bPA", "bottomPanel Added!");
//				boardLayout.addView(bottomPanel);
//			}
//		} else {
//            Log.d("bPP", "" + bottomPanel.getParent().toString());
//            boardLayout.removeView(bottomPanel);
//            boardLayout.addView(bottomPanel);
//        }

//        Log.d("bLFVBID", "" + boardLayout.findViewById(R.id.bottomPanel));

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
//        Log.d("oR", "Board.onResume()");
//        Log.d("bP", "" + bottomPanel.getChildCount());
//		NotificationService.notificationManager.cancel(NotificationService.NOTIFICATION);
//        String turn = "";
//        Log.d("tM", "" + NotificationService.turnMade);
//        Log.d("oCC", "" + onCreateCalled);
        if(boardLayout.findViewById(R.id.bottomPanel) == null) {
//            boardLayout.addView(bottomPanel);
            if(game.lastMove.contains("X")) {
                //					Toast.makeText(context, player1 + "'s Turn", Toast.LENGTH_SHORT).show();
                playerTurn.setText(game.player1.playerName + "'s Turn");
            } else if (game.lastMove.contains("O")) {
                //					Toast.makeText(context, player2 + "'s Turn", Toast.LENGTH_SHORT).show();
                playerTurn.setText(game.player2.playerName + "'s Turn");
            }
//            else {
//                playerTurn.setText(game.player1.playerName + "'s Turn");
//                game.lastMove = "X";
//            }
        }
//        ButtonPressed.currentTurn = Board.currentTurn;
//        if(!onCreateCalled && NotificationService.turnMade) {
//            player1 = wincheck[wincheck.length - 1][2];
//            player2 = wincheck[wincheck.length - 1][3];
//            game.lastMove = wincheck[wincheck.length - 1][4];
//            String currentPlayer = null;
//            if (Plus.PeopleApi.getCurrentPerson(Index.client) != null) {
//                Person currentPerson = Plus.PeopleApi.getCurrentPerson(Index.client);
//                currentPlayer = currentPerson.getDisplayName();
//            }
//            if ( (currentPlayer.equals(player1) && !game.lastMove.contains("X")) ||
//                    (currentPlayer.equals(player2) && !game.lastMove.contains("O")) ){
//                myTurn = false;
//            }
////            Toast.makeText(context, "myTurn = " + myTurn, Toast.LENGTH_SHORT).show();
////            Toast.makeText(context, "currentTurn = " + currentTurn, Toast.LENGTH_SHORT).show();
//            //				Toast.makeText(context, "player1: " + player1, Toast.LENGTH_SHORT).show();
//            //				Toast.makeText(context, "player2: " + player2, Toast.LENGTH_SHORT).show();
//            if(game.lastMove.contains("X")) {
//                //					Toast.makeText(context, player1 + "'s Turn", Toast.LENGTH_SHORT).show();
//                playerTurn.setText(player1 + "'s Turn");
//                turn = "O";
//            } else if (game.lastMove.contains("O")) {
//                //					Toast.makeText(context, player2 + "'s Turn", Toast.LENGTH_SHORT).show();
//                playerTurn.setText(player2 + "'s Turn");
//                turn = "X";
//            } else {
//                playerTurn.setText(player1 + "'s Turn");
//                game.lastMove = "X";
//                turn = "O";
//            }
//            f = Integer.parseInt(wincheck[wincheck.length - 1][0]);
//            g = Integer.parseInt(wincheck[wincheck.length - 1][1]);
//            Log.d("f", "" + f);
//            Log.d("g", "" + g);
//            int key = -1;
//            switch(n){
//                case 1:
//                    key = f*3 + g;
//                    break;
//                case 2:
//                    key = f*9 + g;
//                    break;
//            }
//            keys.get(key).setText(turn);
//            bp.boardChanger(f, g, 2, true);
//            NotificationService.turnMade = false;
//        }
//        onCreateCalled = false;
//		boardVisible = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//        Log.d("oP", "Board.onPause()");
//        boardVisible = false;
	}

    @Override
    protected void onStop() {
        super.onStop();
//        Log.d("oS", "Board.onStop()");
//        boardLayout.removeView(bottomPanel);
//        keys = new Hashtable<Integer, Button>(6561);
//        bottomPanel.removeAllViews();
//        boardLayout.removeAllViews();
//        ButtonPressed.wincheck = new String [10][9];
//        ButtonPressed.metawincheck = new String [3][3];
//        Board.wincheck = new String [10][9];
//        ButtonPressed.currentTurn = "";
//        player1 = null;
//        player2 = null;
//        boardVisible = false;
//        this.finish();
    }
	
//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		if(myTurn && hasFocus && onGoingMatch != null) {
//			bp = new ButtonPressed(context, n);
//			Log.d("f", wincheck[wincheck.length - 1][0]);
//			Log.d("g", wincheck[wincheck.length - 1][1]);
//			Log.d("n", "" + n);
//			if ( n >= 2 ) {
//				bp.boardChanger(Integer.parseInt(wincheck[wincheck.length - 1][0]), Integer.parseInt(wincheck[wincheck.length - 1][1]), n, true);
//			}
//		}
//	}
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
//		if(!done) {
            super.onSaveInstanceState(instanceState);
//            Log.d("OSIS", "Saved!");
            instanceState.putByteArray("On Going Match", ButtonPressed.wincheckerToByteArray(ButtonPressed.wincheck));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("On Going Match", Base64.encodeToString(ButtonPressed.wincheckerToByteArray(ButtonPressed.wincheck), Base64.DEFAULT));
            editor.commit();
//            Log.d("sPE", "Shared Preferences Edited");
//        }
	}
	
	@Override
	public void onRestoreInstanceState(Bundle instanceState) {
		super.onRestoreInstanceState(instanceState);
//		Log.d("ORIS", "Restored!");
		onGoingMatch = instanceState.getByteArray("On Going Match");
	}

	public void savedGameRecreate(byte [] onGoingMatch) {
		
//		bp = new ButtonPressed(context, level, game, this);
//		BoardAdapter.numberOfTimesPositionIsZero = 0;
//		BoardAdapter.positionIsEight = false;
		String gameArray = new String(onGoingMatch);
		Log.d("gA", gameArray);
		int i = 0;
		int j = 0;
//		for(int k = 0; k < gameArray.length(); k++) {
//			if(gameArray.charAt(k) == 'X' || gameArray.charAt(k) == 'O') {
//				wincheck[i][j] = "" + gameArray.charAt(k);
//				if(i < 9 && winChecker(i, j, level, level, wincheck, "" + gameArray.charAt(k))) {
//					winChecker(i/3, j/3, 1, level, ButtonPressed.metawincheck, "" + gameArray.charAt(k));
//				}
//			} else if(gameArray.charAt(k) == 'n' || gameArray.charAt(k) == 'u' || gameArray.charAt(k) == 'l') {
//				wincheck[i][j] = null;
//			}
//			if(gameArray.charAt(k) == ',') {
//				j++;
//			}
//			if(gameArray.charAt(k) == ';') {
//				i++;
//				j = 0;
//			}
//
//		}
		i = 9;
//		Log.d("gameArray", gameArray);
		String [] rows = gameArray.split(";");
		String [][] game = new String [rows.length][];
		for(int l = 0; l < rows.length; l++) {
			game[l] = rows[l].split(",");
		}
		wincheck = game;
//		wincheck[i][0] = "" + Integer.parseInt(game[i][0]);
//		wincheck[i][1] = "" + Integer.parseInt(game[i][1]);
//		wincheck[i][2] = "" + game[i][2];
//		wincheck[i][3] = "" + game[i][3];
//		wincheck[i][4] = "" + game[i][4];
//		wincheck[i][5] = "" + Integer.parseInt(game[i][5]);
//        wincheck[i][6] = "" + game[i][6];
//        wincheck[i][7] = "" + game[i][7];
//        wincheck[i][8] = "" + game[i][8];

        Log.d("Last Move", "" + wincheck[Integer.parseInt(wincheck[i][0])][Integer.parseInt(wincheck[i][1])]);
//		bp.boardChanger(Integer.parseInt(game[i][0]), Integer.parseInt(game[i][1]), n, !multiplayer);
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
//		keys = new Hashtable<Integer, Button>(6561);
		Board.wincheck = wincheck;
		
//		Log.d("Level", "" + level);
//		Log.d("StackTrace", Log.getStackTraceString(new Exception()));
//		Log.d("WBCx", x);
//		Log.d("multiplayer", "" + multiplayer);
		
		switch (level){
		case 1:
//			ButtonPressed.wincheck = new String [82][81];
//			ButtonPressed.metawincheck = new String [3][3];
//			Board.wincheck = new String [82][81];
//			bottomPanel.removeAllViews();
//			Log.d("winToast", winToast.toString());
//			winToast.show();
//			((Activity) game).finish();
			break;
		case 2:
			if(actual == 2){
//				winningletter.setText(x);
//				winningletter.setTextSize(40);
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
//				Log.d("metawincheck", ButtonPressed.metawincheck[f/3][g/3]);
//				Log.d("f", "" + f);
//				Log.d("g", "" + g);
//				boardAdapter.replace();
//				Log.d("replace", "done");
//				miniwin.add(winningletter);
//				CardLayout cl = (CardLayout) wins.get((f/3)*3+(g/3)).getLayout();
//				cl.show(wins.get((f/3)*3+g/3), winningletter.getText());		
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
				        //If you want to operate UI modifications, you must run ui stuff on UiThread.
				        Board.this.runOnUiThread(new Runnable() {
				            @Override
				        	public void run() {
//				            	if ( multiplayer ) {
//				            		Log.d("UIT", "Called");
//				            		boardAdapter.replace();
//				            	}
//                                Log.d("done", "" + done);
//				            	if (!done) {
					            	Log.d("Level", "" + level);
					            	Log.d("receiving", "" + Index.receiving);
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
//				            	}
				            }
				        });
                        Board.winOrTie = bp.winChecker(f, g, 1, 2, ButtonPressed.metawincheck, x);
					}
				}, 500);
			}
//			if(actual ==3){
//				winningletter.setFont(new Font("Times New Roman", Font.BOLD, 200));
//				miniwin.add(winningletter);
//				//metametaboard[f/3][g/3].removeAll();
//				//metametaboard[f/3][g/3].setLayout(new GridLayout(1,1));
//				//metametaboard[f/3][g/3].add(miniwin);
//				//metametaboard[f/3][g/3].repaint();
//				CardLayout cl = (CardLayout) metametawins.get((f/9)*3+(g/9)).getLayout();
//				cl.show(metametawins.get((f/9)*3+(g/9)), winningletter.getText());
//				newDossier.ButtonPressed.metametawincheck[f/9][g/9]=winningletter.getText();
//				//System.out.println(f+", "+g);
//				bp.boardChanger(f, g, 3);
//				bp.WinChecker(s,f,g,1,2,newDossier.ButtonPressed.metametawincheck,x);}
//			if(actual==4){
//				winningletter.setFont(new Font("Times New Roman", Font.BOLD, 200));
//				miniwin.add(winningletter);
//				//metametametaboard[f/3][g/3].removeAll();
//				//metametametaboard[f/3][g/3].setLayout(new GridLayout(1,1));
//				//metametametaboard[f/3][g/3].add(miniwin);
//				//metametametaboard[f/3][g/3].repaint();
//				CardLayout cl = (CardLayout) metametametawins.get((f/27)*3+(g/27)).getLayout();
//				cl.show(metametametawins.get((f/27)*3+(g/27)), winningletter.getText());
//				newDossier.ButtonPressed.metametametawincheck[f/27][g/27]=winningletter.getText();
//				bp.boardChanger(f, g, 4);
//				bp.WinChecker(s,f,g,1,4,newDossier.ButtonPressed.metametametawincheck,x);}
//			break;
//		case 3:
//			if(actual==3){
//			int a = f%9;
//			int b = g%9;
//			winningletter.setFont(new Font("Times New Roman", Font.BOLD, 60));
//			miniwin.add(winningletter);
//			CardLayout cl = (CardLayout) metametaminiwins.get((f/9)*27+(g/9)*3+((a/3)*9+(b/3))).getLayout();
//			cl.show(metametaminiwins.get((f/9)*27+(g/9)*3+((a/3)*9+(b/3))), winningletter.getText());
//			//((JPanel)metametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).removeAll();
//			//((JPanel)metametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).setLayout(new GridLayout(1,1));
//			//((JPanel)metametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).add(miniwin);
//			//((JPanel)metametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).repaint();
//			newDossier.ButtonPressed.metametaminiwincheck[f/3][g/3]=winningletter.getText();
//			//System.out.println(f+", "+g);
//			bp.boardChanger(f, g, 3);
//			bp.WinChecker(s,f,g,2,3, newDossier.ButtonPressed.metametaminiwincheck,x);}
//			if(actual==4){
//				int a=f%27;
//				int b=g%27;
//				winningletter.setFont(new Font("Times New Roman", Font.BOLD, 60));
//				miniwin.add(winningletter);
//				CardLayout cl = (CardLayout) metametametaminiwins.get((f/27)*27+(g/27)*3+((a/9)*9+b/9)).getLayout();
//				cl.show(metametametaminiwins.get((f/27)*27+(g/27)*3+((a/9)*3+b/9)), winningletter.getText());
//				//((JPanel)metametametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).removeAll();
//				//((JPanel)metametametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).setLayout(new GridLayout(1,1));
//				//((JPanel)metametametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).add(miniwin);
//				//((JPanel)metametametaboard[f/9][g/9].getComponent((a/3)*3+b/3)).repaint();
//				newDossier.ButtonPressed.metametametaminiwincheck[f/9][g/9]=winningletter.getText();
//				bp.boardChanger(f, g, 4);
//				bp.WinChecker(s, f, g, 2, 4, newDossier.ButtonPressed.metametametaminiwincheck,x);
//			}
//			break;
//		case 4:
//			//System.out.println(f+", "+g);
//			int a = f%27;
//			int b = g%27;
//			int c = a%9;
//			int d = b%9;
//			winningletter.setFont(new Font("Times New Roman", Font.BOLD, 17));
//			miniwin.add(winningletter);
//			/*LayoutManager layout = game.getLayout();
//			JFrame test = new JFrame();
//			test.add(((BorderLayout) layout).getLayoutComponent(BorderLayout.CENTER));
//			test.setVisible(true);*/
//			//((JPanel)((JPanel)metametametaboard[f/27][g/27].getComponent((a/9)*3+b/9)).getComponent((c/3)*3+d/3)).removeAll();
//			//((JPanel)((JPanel)metametametaboard[f/27][g/27].getComponent((a/9)*3+b/9)).getComponent((c/3)*3+d/3)).setLayout(new GridLayout(1,1));
//			//((JPanel)((JPanel)metametametaboard[f/27][g/27].getComponent((a/9)*3+b/9)).getComponent((c/3)*3+d/3)).add(winningletter);
//			//((JPanel)((JPanel)metametametaboard[f/27][g/27].getComponent((a/9)*3+b/9)).getComponent((c/3)*3+d/3)).repaint();
//			//(f/27)*81+(g/27)*9+(a/9)*27+(b/9)*3+(c/3)*9+d/3
//			CardLayout cl = (CardLayout) metametametaminiminiwins.get((f/27)*243+(g/27)*9+(a/9)*81+(b/9)*3+(c/3)*27+d/3).getLayout();
//			cl.show(metametametaminiminiwins.get((f/27)*243+(g/27)*9+(a/9)*81+(b/9)*3+(c/3)*27+d/3), winningletter.getText());
//			newDossier.ButtonPressed.metametametaminiminiwincheck[f/3][g/3]=winningletter.getText();
//			bp.boardChanger(f, g, 4);
//			bp.WinChecker(s, f,g,3,4,newDossier.ButtonPressed.metametametaminiminiwincheck,x);
//		}
//	if(win.isVisible()==false&&s==Math.pow(newDossier.ButtonPressed.boardSize(newDossier.Board.n), 2)){
//		tie.add(tieText);
//		tie.setBounds(200,75,200,75);
//		tie.setVisible(true);
//		game.setVisible(false);
//		Index levelmenu = new Index();
//		levelmenu.levelmenu();}
		}
	}
	
	public void finishActivity(Context context, boolean won, String winnerName) {
//		if(caller.equals("Index")) {
//			
//		}
//		StackTraceElement [] stackTraceElements = Thread.currentThread().getStackTrace();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
//        done = true;
//		boardVisible = false;
//		Log.d("fACaller", stackTraceElements[3].toString());
//		Log.d("Method", "finishActivity");
		if(won){
		    winOrTie = false;
			Intent winner = new Intent(context, Winner.class);
			winner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			winner.putExtra("Multiplayer", multiplayer);
//			winner.putExtra("Match ID", matchId);
//			winner.putExtra("Client", caller);
//			winner.putExtra("Can Rematch", canRematch);
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
		
//		if(!caller.equals("Index")) {
//		this.finish();
//		} //else {
//			this.finish();
//		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("oKD", "keyCode: " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("keyCode", "KEYCODE_BACK");
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
//			player1 = null;
//			player2 = null;
//			boardVisible = false;
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
//			boardVisible = false;
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
//			this.currentTurn = null;
			ButtonPressed.currentTurn = "";
//			player1 = null;
//			player2 = null;
			Intent menu = new Intent(context, LevelMenu.class);
			menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(multiplayer) {
				menu.putExtra("Multiplayer", multiplayer);
			}
			menu.putExtra("Caller", "Board");
			menu.putExtra("Instructions", false);
			context.startActivity(menu);
			break;
//		case "Save":
//			if(client.isConnected()) {
//				client.disconnect();
//			}
//			saveCalled = true;
//			client.connect();
//			break;
		case "Main Menu":
//			boardVisible = false;
			keys = new Hashtable<Integer, Button>(6561);
			bottomPanel.removeAllViews();
			boardLayout.removeAllViews();
			ButtonPressed.wincheck = new String [10][9];
			ButtonPressed.metawincheck = new String [3][3];
			Board.wincheck = new String [10][9];
			ButtonPressed.currentTurn = "";
//			player1 = null;
//			player2 = null;
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
//			player1 = null;
//			player2 = null;
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
//			player1 = null;
//			player2 = null;
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
		if (saveCalled) {
			saveCalled = false;
			Log.d("SG", "Calling Saved Games UI");
			int maxNumberOfSavedGamesToShow = 5;
		    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(client,
		            "Saved Games", true, true, maxNumberOfSavedGamesToShow);
		    startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
		} else {
			Log.d("BC", "Board Connected");
		}
	}

	@Override
	public void onConnectionSuspended(int result) {
		Log.d("CS", "Connection Suspended");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                final Intent intent) {
	    Log.d("oAR", "onActivityResult");
	    callbackManager.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
	        if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
	            Log.d("LG", "Load Game");
	        	// Load a snapshot.
	            SnapshotMetadata snapshotMetadata = (SnapshotMetadata)
	                    intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
	            final String snapshotNm = snapshotMetadata.getUniqueName();
	            Log.d("snapshotNm", snapshotNm);
	            
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
//							client.disconnect();
//			            	client.connect();
						}
					});
			    AlertDialog dialog = builder.create();
			    dialog.show();
	            
//	            while(!success) {
//	            	Log.d("success", "" + success);
//	            }
//	            
//	            String [] playerNames = snapshotNm.split("-");
//	            int level = Integer.parseInt(playerNames[2].substring(5, playerNames[2].length()));
//	            
//	            Intent board = new Intent(context, Board.class);
//	            board.putExtra("Multiplayer", false);
//	            board.putExtra("Caller", "CurrentGames");
//	            board.putExtra("Level", level);
//	            board.putExtra("On Going Match", gameData);
//	            startActivity(board);
	            
	        } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
	            // Create a new snapshot named with a unique string
	        	final String snapshotNm = game.player1.playerName + "-" + game.player2.playerName + "-" + "Level" + level;
	        	Log.d("snapshotNm", snapshotNm);

//	        	AsyncTask<Void, Void, Boolean> updateTask = new AsyncTask<Void, Void, Boolean>() {
//
//	        		@Override
//	                protected void onPreExecute() {
//	                    super.onPreExecute();
////	                    button.setEnabled(false);
//	                    inAnimation = new AlphaAnimation(0f, 1f);
//	                    inAnimation.setDuration(200);
//	                    progressBarHolder.setAnimation(inAnimation);
//	                    progressBarHolder.setVisibility(View.VISIBLE);
//	        		}
//
//	        		@Override
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
//	                    SnapshotMetadataChange snapshotMetaData = SnapshotMetadataChange.EMPTY_CHANGE;
//	                    if(snapshot.getMetadata().getDescription() == null) {
//	                    	Log.d("nsMD", "new");
//	                    	snapshotMetaData = new SnapshotMetadataChange.Builder()
//	                    	.setDescription(player1 + " vs. " + player2  + " - Level " + n)
//	                    	.build();
//	                    }
//
//	                    Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
//	                            client, snapshot, snapshotMetaData).await();
//
//	                    if (!commit.getStatus().isSuccess()) {
//	                        Log.w("!success", "Failed to commit Snapshot.");
//	                        return false;
//	                    }
//
//	                    // No failures
//	                    return true;
//	                }
//
//	                @Override
//	                protected void onPostExecute(Boolean result) {
//	                    super.onPostExecute(result);
//	                    outAnimation = new AlphaAnimation(1f, 0f);
//	                    outAnimation.setDuration(200);
//	                    progressBarHolder.setAnimation(outAnimation);
//	                    progressBarHolder.setVisibility(View.GONE);
////	                    if(result) {
////	                    	Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
////	                    } else {
////	                    	Toast.makeText(context, "Cannot save", Toast.LENGTH_SHORT).show();
////	                    }
//	                }
//	            };
//	            updateTask.execute();
	        }
	    }
	}
	
	private void saveOver(Intent intent) {
    	// Load a snapshot.
        SnapshotMetadata snapshotMetadata = intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
        final String snapshotNm = snapshotMetadata.getUniqueName();
        Log.d("snapshotNm", snapshotNm);

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
        	@Override
            protected void onPreExecute() {
                super.onPreExecute();
//	                    button.setEnabled(false);
                inAnimation = new AlphaAnimation(0f, 1f);
                inAnimation.setDuration(200);
                progressBarHolder.setAnimation(inAnimation);
                progressBarHolder.setVisibility(View.VISIBLE);
    		}

        	@Override
            protected Boolean doInBackground(Void... params) {
                // Open the saved game using its name.
//                Snapshots.OpenSnapshotResult result = Games.Snapshots.open(client,
//                        snapshotNm, false).await();

                // Check the result of the open operation
//                if (result.getStatus().isSuccess()) {
//                	Log.d("status", "success");
//                    Snapshot snapshot = result.getSnapshot();
//
//                    snapshot.getSnapshotContents().writeBytes(wincheckerToByteArray(ButtonPressed.wincheck));
//
////                    Snapshots.CommitSnapshotResult commit = Games.Snapshots.commitAndClose(
////                            client, snapshot, SnapshotMetadataChange.EMPTY_CHANGE).await();
//
//                    if (!commit.getStatus().isSuccess()) {
//                        Log.w("Commit Status", "Failed to commit Snapshot.");
//                        return false;
//                    }
//
////	                         No failures
//                    return true;
////	                        // Read the byte content of the saved game.
////	                        try {
////	                            gameData = snapshot.getSnapshotContents().readFully();
////	                            success = true;
////	                        } catch (IOException e) {
////	                            Log.e("Error", "Error while reading Snapshot.", e);
////	                        }
//                } else{
//                    Log.e("Error", "Error while loading: " + result.getStatus().getStatusCode());
//                    return false;
//                }
////	                    return result.getStatus().getStatusCode();
				return true;
            }

        	@Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                progressBarHolder.setAnimation(outAnimation);
                progressBarHolder.setVisibility(View.GONE);
//                if(result) {
//                	Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
//                } else {
//                	Toast.makeText(context, "Cannot save", Toast.LENGTH_SHORT).show();
//                }
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
