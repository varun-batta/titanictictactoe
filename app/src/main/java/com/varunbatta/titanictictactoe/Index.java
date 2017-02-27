package com.varunbatta.titanictictactoe;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.plus.Plus;

public class Index<match> extends Activity implements GoogleApiClient.ConnectionCallbacks, 
GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener {
	public static Context context;
	public static NotificationManager notificationManager;
	public static boolean receiving = false;
	private static NotificationService notificationService = new NotificationService();
	private IRemoteService remoteService;
	private boolean started = false;
	private boolean notificationActivity;
    private boolean googlePlayGamesServicesActivity;
	private byte [] notificationMatch;
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("oSC", "on Service Connected");
			remoteService = IRemoteService.Stub.asInterface((IBinder)service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d("oSD", "on Service Disconnected");
			remoteService = null;
		}
		
	};
	
    public static GoogleApiClient client = null;
	public TurnBasedMatch match;
	boolean resolvingError;
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    ButtonPressed bp;
    Board board;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (getIntent().getFlags() == 335544320) {
            googlePlayGamesServicesActivity = true;
        }

		if (getIntent().getByteArrayExtra("Game") != null) {
			notificationMatch = getIntent().getByteArrayExtra("Game");
			notificationActivity = true;
		}
//		if (savedInstanceState != null) {
//			Log.d("Saved", "Value Found");
//			Log.d("SIS", savedInstanceState.getByteArray("On Going Match").toString());
//			Intent board = new Intent(getApplicationContext(), Board.class);
//			board.putExtra("On Going Match", savedInstanceState.getByteArray("On Going Match"));
//			startActivity(board);
//		} else {
//			Log.d("SIS", "null");
//		}
		setContentView(R.layout.index);
		
		context = getApplicationContext();
		
		notificationManager = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);
		
//		bindService(new Intent(context, NotificationService.class), serviceConnection, BIND_AUTO_CREATE);
		startService(new Intent(context, NotificationService.class));
		
		RelativeLayout indexLayout = (RelativeLayout) findViewById(R.id.layout);
		indexLayout.setBackgroundColor(Color.BLUE);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		height *= 0.125;
		
		GridView game = (GridView) findViewById(R.id.game);
		game.setAdapter(new IndexAdapter(this, width));
		game.setBackgroundColor(Color.BLUE);
		
		Button play = (Button) findViewById(R.id.play_button);
		play.setText("Play!");
		play.setWidth(width);
		play.setHeight(height);
		play.setBackgroundColor(Color.WHITE);
		play.setTextColor(Color.BLACK);
		play.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent mainmenu = new Intent(context, MainMenu.class);
				startActivity(mainmenu);
			}
		});
		
		Log.d("bGAC", "Reached");
		buildGoogleApiClient();

		client.connect();
		Log.d("Client", "Connected");
	}
	
	public void buildGoogleApiClient() {
		client = new GoogleApiClient.Builder(this)
		.addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
        .addScope(Plus.SCOPE_PLUS_PROFILE)
		.addApi(Games.API)
		.addScope(Games.SCOPE_GAMES)
		.addApi(Drive.API)
        .addScope(Drive.SCOPE_APPFOLDER)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();
	}

//	@Override
//	public void onRestoreInstanceState(Bundle instanceState) {
//		super.onRestoreInstanceState(instanceState);
//		Intent board = new Intent(getApplicationContext(), Board.class);
//		board.putExtra("On Going Match", savedInstanceState.getByteArray("On Going Match"));
//		startActivity(board);
//	}
	private String getPlayer(byte [] game, int player) {
        String gameArray = new String(game);
        String [] rows = gameArray.split(";");
        String [][] board = new String [rows.length][];
        for(int i = 0; i < rows.length; i++) {
            board[i] = rows[i].split(",");
        }
        if(player == 1) {
            return board[board.length - 1][2];
        } else {
            return board[board.length - 1][3];
        }
    }

	private String getCurrentParticipantId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
        if(board[board.length - 1][4].equals("X")) {
            return "p_1";
        } else {
            return "p_2";
        }
	}

	private String getPendingParticipantId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
        if(board[board.length - 1][4].equals("X")) {
            return "p_2";
        } else {
            return "p_1";
        }
	}

	private String getMatchId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}

        Log.d("bL", board[board.length - 1].toString());

		return board[board.length - 1][6];
	}

	//	@Override
//	public void onResult(InitiateMatchResult arg0) {
//		// TODO Auto-generated method stub
//		Log.d("TBMP", "Turn Based Match Received");
//		Toast display = Toast.makeText(getApplicationContext(), "Turn Based Match Received", Toast.LENGTH_SHORT);
//		display.show();
//	}
//	@Override
//	public void onTurnBasedMatchReceived(TurnBasedMatch match) {
//		byte [] game = match.getData();
//		
//		Toast display = Toast.makeText(getApplicationContext(), "Turn Based Match Received", Toast.LENGTH_SHORT);
//		display.show();
//		
//		Intent board = new Intent(this, Board.class);
//		board.putExtra("Level", 2);
//		board.putExtra("On Going Match", game);
//		startActivity(board);
//	}
//
//	@Override
//	public void onTurnBasedMatchRemoved(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
	public void registerTurnBasedClient(GoogleApiClient currentClient) {
		Log.d("rTBC", "Registering...");
		Games.TurnBasedMultiplayer.registerMatchUpdateListener(currentClient, notificationService.getContext());	
	}
//
//	@Override
//	public void onInvitationReceived(Invitation arg0) {
//		Toast.makeText(getApplication(), "Invitation Received", Toast.LENGTH_SHORT).show();
//		
//	}
//
//	@Override
//	public void onInvitationRemoved(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onTurnBasedMatchReceived(TurnBasedMatch arg0) {
//		// TODO Auto-generated method stub
//		Log.d("TBMR", "on Turn Based Match Received");
//	}
//
//	@Override
//	public void onTurnBasedMatchRemoved(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onInvitationReceived(Invitation arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onInvitationRemoved(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onConnectionFailed(ConnectionResult arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onConnected(Bundle arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onConnectionSuspended(int arg0) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void onTurnBasedMatchReceived(TurnBasedMatch match) {
		receiving = true;
		Log.d("TBMR", "onTurnBasedMatchReceived");
		board = new Board();
		String opponentName = match.getParticipant(getPendingParticipantId(match)).getDisplayName();
		ParticipantResult currentPlayerParticipantResult = match.getParticipant(match.getParticipantId(Games.Players.getCurrentPlayerId(client))).getResult();
		if ( currentPlayerParticipantResult != null && currentPlayerParticipantResult.getResult() == ParticipantResult.MATCH_RESULT_LOSS ) {
			Intent lossIntent = new Intent(context, Winner.class);
			lossIntent.putExtra("Multiplayer", true);
			lossIntent.putExtra("Match ID", match.getMatchId());
			lossIntent.putExtra("Caller", "Index");
			lossIntent.putExtra("Can Rematch", match.canRematch());
			lossIntent.putExtra("Winner", opponentName);

			Games.TurnBasedMultiplayer.finishMatch(client, match.getMatchId());
			Board.savedGameRecreate(match.getData(), context);
			
			Board.keys = new Hashtable<Integer, Button>(6561);
			Board.bottomPanel.removeAllViews();
			Board.boardLayout.removeAllViews();
			ButtonPressed.currentTurn = "";
			board.finishActivity(ButtonPressed.context, true, opponentName);
		} else if ( currentPlayerParticipantResult != null && currentPlayerParticipantResult.getResult() == ParticipantResult.MATCH_RESULT_TIE) {
			Intent tieIntent = new Intent(context, Winner.class);
			tieIntent.putExtra("Match ID", match.getMatchId());
			tieIntent.putExtra("Caller", "Index");
			tieIntent.putExtra("Can Rematch", match.canRematch());
			tieIntent.putExtra("Winner", "Tie");

			Games.TurnBasedMultiplayer.finishMatch(client, match.getMatchId());
			Board.savedGameRecreate(match.getData(), context);
			
			Board.keys = new Hashtable<Integer, Button>(6561);
			Board.bottomPanel.removeAllViews();
			Board.boardLayout.removeAllViews();
			ButtonPressed.currentTurn = "";
			board.finishActivity(ButtonPressed.context, true, "Tie");
		} else if ( match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
			byte [] game = match.getData();
			Intent turnIntent = new Intent(context, Board.class);
			turnIntent.putExtra("On Going Match", game);
			turnIntent.putExtra("Level", getLevel(game));
			turnIntent.putExtra("Match ID", getMatchId(game));
			turnIntent.putExtra("Pending Player", getPendingParticipantId(game));
			turnIntent.putExtra("Current Player", getCurrentParticipantId(game));
			turnIntent.putExtra("Caller", "Index");
			turnIntent.putExtra("My Turn", true);
			turnIntent.putExtra("Finished", false);
			turnIntent.putExtra("Can Rematch", true);
			turnIntent.putExtra("Multiplayer", true);

			Board.savedGameRecreate(game, context);
			
			int level = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][5]);
			int row = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][0]);
			int column = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][1]);
			int multiplier = (int) Math.pow(3, (double) level);
			int key = row * multiplier + column;
			
			String turn = "";
			
			if ( (match.getParticipant(getCurrentParticipantId(game)).getDisplayName()).equals(Board.wincheck[Board.wincheck.length - 1][2]) ) {
				Board.wincheck[Board.wincheck.length - 1][4] = "X";
				turn = "O";
				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][2] + "'s Turn");
			}
			else if ( (match.getParticipant(getCurrentParticipantId(game)).getDisplayName()).equals(Board.wincheck[Board.wincheck.length - 1][3]) ) {
				Board.wincheck[Board.wincheck.length - 1][4] = "O";
				turn = "X";
				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][3] + "'s Turn");
			}
			
			ButtonPressed.currentTurn = Board.wincheck[Board.wincheck.length - 1][4];
			
//			if(Board.wincheck[Board.wincheck.length - 1][4].equals("X")) {
//				turn = "O";
//				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][2] + "'s Turn");
//			} else {
//				turn = "X";
//				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][3] + "'s Turn");
//			}
			
			Button opponentMove = Board.keys.get(key);
			opponentMove.setText(turn);
			opponentMove.setEnabled(false);
			
			bp = new ButtonPressed(ButtonPressed.context, level);
			
			if( level >= 2) {
				bp.boardChanger(row, column, level, true);
			}
			
			if ( !bp.winChecker(row, column, level, level, Board.wincheck, turn) && ButtonPressed.metawincheck[row/3][column/3] == null) {
				for ( int i = 0; i < multiplier; i++ ) {
					for ( int j = 0; j < multiplier; j++ ) {
						int buttonKey = i * multiplier + j;
						String metaValue = ButtonPressed.metawincheck[i/3][j/3];
						if(metaValue == null) {
							Log.d("buttonKey", "" + buttonKey);
							Button button = Board.keys.get(buttonKey);
							button.setClickable(true);
						}
					}
				}
			}
		}
	}

	@Override
	public void onTurnBasedMatchRemoved(String arg0) {
		Log.d("TBMR", "onTurnBasedMatchRemoved " + arg0);
	}

	@Override
	public void onInvitationReceived(Invitation arg0) {
		Log.d("IR", "onInvitationReceived");
	}

	@Override
	public void onInvitationRemoved(String arg0) {
		Log.d("IR", "onInvitationRemoved");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("CF", "onConnectionFailed");
		if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                Log.d("EC", "" + result.getErrorCode());
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
		Log.d("C", "" + connectionHint);
		SharedPreferences savedGame = PreferenceManager.getDefaultSharedPreferences(context);
		if(connectionHint != null) {
            Log.d("cH", "not null");
			match = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
//			Log.d("Match", "Received!");
			
			if(match != null) {
				byte [] game = match.getData();
				String matchId = match.getMatchId();
				String pendingParticipantId = getPendingParticipantId(match);
				int level = getLevel(game);
				boolean finished = match.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE;
				boolean myTurn = false;
				if(!finished) {
					myTurn = true;
				}
				
//				Toast display = Toast.makeText(getApplicationContext(), "Turn Based Match Received", Toast.LENGTH_SHORT);
//				display.show();
				
				Intent board = new Intent(this, Board.class);
				board.putExtra("Level", level);
				board.putExtra("On Going Match", game);
                Log.d("MatchID", matchId);
				board.putExtra("Match ID", matchId);
				board.putExtra("Pending Player", pendingParticipantId);
				board.putExtra("Current Player", match.getParticipantId(Games.Players.getCurrentPlayerId(client)));
				board.putExtra("Caller", "Index");
				board.putExtra("My Turn", myTurn);
				board.putExtra("Finished", finished);
				board.putExtra("Can Rematch", match.canRematch());
				board.putExtra("Multiplayer", true);
				startActivity(board);
				return;
			}
		} else if (googlePlayGamesServicesActivity) {
            Intent currentGames = new Intent(context, CurrentGames.class);
            currentGames.putExtra("Multiplayer", true);
            startActivity(currentGames);
        } else if (notificationActivity) {
            Log.d("notificationActivity", "" + notificationActivity);
			Intent board = new Intent(context, Board.class);
			board.putExtra("On Going Match", notificationMatch);
			board.putExtra("Level", getLevel(notificationMatch));
			board.putExtra("Match ID", getMatchId(notificationMatch));
			board.putExtra("Pending Player", getPendingParticipantId(notificationMatch));
            Log.d("PP", getPendingParticipantId(notificationMatch));
			board.putExtra("Current Player", getCurrentParticipantId(notificationMatch));
			board.putExtra("Caller", "Index");
			board.putExtra("My Turn", true);
			board.putExtra("Finished", false);
			board.putExtra("Can Rematch", true);
			board.putExtra("Multiplayer", true);
			startActivity(board);
		} else if ( savedGame.contains("On Going Match") ) {
			Log.d("Saved Game", "Opening...");
			byte [] game = Base64.decode(savedGame.getString("On Going Match", ""), Base64.DEFAULT);
			String gameString = new String(game);
			boolean myTurn = true;
			Log.d("CPId", getCurrentParticipantId(game));
			Log.d("CT", getCurrentTurn(game));
			if ( (getCurrentTurn(game).equals("X") && !Games.Players.getCurrentPlayer(client).getDisplayName().equals(getPlayer(game, 1))) ||
                    (getCurrentTurn(game).equals("O") && !Games.Players.getCurrentPlayer(client).getDisplayName().equals(getPlayer(game, 2))) ) {
				myTurn = false;
			}
			Log.d("gS", gameString);
			if ( gameString.contains("X") || gameString.contains("O") ) {
				Intent board = new Intent(getApplicationContext(), Board.class);
				board.putExtra("On Going Match", game);
				board.putExtra("Level", getLevel(game));
                Log.d("MatchID", getMatchId(game));
				board.putExtra("Match ID", getMatchId(game));
				board.putExtra("Pending Player", getPendingParticipantId(game));
				board.putExtra("Current Player", getCurrentParticipantId(game));
				board.putExtra("Caller", "Index");
				board.putExtra("My Turn", myTurn);
				board.putExtra("Finished", false);
				board.putExtra("Can Rematch", true);
				board.putExtra("Multiplayer", true);
				board.putExtra("Saved Game", true);
				startActivity(board);
			}
		}
	}

	private String getCurrentTurn(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
		return board[9][4];
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.d("CS", "onConnectionSuspended");
	}
	
	@Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        
        if (request == REQUEST_RESOLVE_ERROR) {
        	client.connect();
        }
    }
	
	private String getPendingParticipantId(TurnBasedMatch match) {
		ArrayList<String> participantIds = match.getParticipantIds();
		String pendingParticipantId = "";
		String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
		Log.d("cPId", currentParticipantId);
        Log.d("pIds", "" + participantIds);
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
		level = Integer.parseInt(board[9][5]);
		return level;
	}
}

