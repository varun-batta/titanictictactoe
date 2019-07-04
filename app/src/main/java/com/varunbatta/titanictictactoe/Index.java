package com.varunbatta.titanictictactoe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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

import bolts.AppLinks;

//import static com.varunbatta.titanictictactoe.Board.player2;

public class Index<match> extends Activity implements GoogleApiClient.ConnectionCallbacks, 
GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener {
	Context context;
    public static NotificationManager notificationManager;
	public static boolean receiving = false;
	public static boolean facebookGame = false;
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

	public static Map<Long, Game> availableGames = new HashMap<Long, Game>();
	
    public static GoogleApiClient client = null;
	public TurnBasedMatch match;
	boolean resolvingError;
	// Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    
    ButtonPressed bp;
    Board board;
    Index index;

	CallbackManager loginCallbackManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		index = this;
		if (!FacebookSdk.isInitialized()) {
			FacebookSdk.sdkInitialize(getApplicationContext(), 2809);
		}

		loginCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(loginCallbackManager,
				new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						Log.d("LoginSuccess", loginResult.toString());
					}

					@Override
					public void onCancel() {
						Log.d("LoginCancel", "FB Login Cancelled");
					}

					@Override
					public void onError(FacebookException exception) {
						Log.d("LoginError", exception.toString());
					}
				});

		LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));


		Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
            facebookGame = true;
            String requestIdsWithKey = targetUrl.toString().split("&")[1];
            String requestIds = requestIdsWithKey.split("=")[1];
            String [] requestIdsList = requestIds.replace("%2C", ",").split(",");

            if (requestIdsList.length > 1) {
                getListOfOpponents(requestIdsList);
            } else {
                try {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("fields", "ids, action_type, application, created_time, date, from, messages, object, to");
                    GameRequest opponentRequest = new GameRequest();
                    opponentRequest.createNewGameRequest("/" + requestIdsList[0], parameters, null);
                    availableGames.put(Long.parseLong(requestIdsList[0]), (Game) new GraphRequests().execute(opponentRequest).get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Intent board = new Intent(getApplicationContext(), Board.class);
                board.putExtra("Level", Integer.parseInt(availableGames.get(Long.parseLong(requestIdsList[0])).data[9][3]));
                board.putExtra("Player 1 Name", availableGames.get(availableGames.keySet().toArray()[0]).player1.playerName);
                board.putExtra("Player 2 Name", availableGames.get(availableGames.keySet().toArray()[0]).player2.playerName);
                if (availableGames.get(availableGames.keySet().toArray()[0]).lastMove.equals("X")) {
                    board.putExtra("Pending Player", availableGames.get(availableGames.keySet().toArray()[0]).player1.playerFBID);
                    board.putExtra("Current Player", availableGames.get(availableGames.keySet().toArray()[0]).player2.playerFBID);
                } else {
                    board.putExtra("Pending Player", availableGames.get(availableGames.keySet().toArray()[0]).player2.playerFBID);
                    board.putExtra("Current Player", availableGames.get(availableGames.keySet().toArray()[0]).player1.playerFBID);
                }
                board.putExtra("Caller", "Index");
                board.putExtra("My Turn", true);
                board.putExtra("Finished", false);
                board.putExtra("Multiplayer", true);
                board.putExtra("GameRequestID", Long.parseLong(requestIdsList[0]));
                startActivity(board);
//                this.finish();
            }
        }
//        if (getIntent().getFlags() == 335544320) {
//            googlePlayGamesServicesActivity = true;
//        }

//		if (getIntent().getByteArrayExtra("Game") != null) {
//            notificationMatch = getIntent().getByteArrayExtra("Game");
//            notificationActivity = true;
//        }
		setContentView(R.layout.index);
		context = getApplicationContext();

		notificationManager = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);
		
//		bindService(new Intent(context, NotificationService.class), serviceConnection, BIND_AUTO_CREATE);
		startService(new Intent(context, NotificationService.class));

		RelativeLayout indexLayout = (RelativeLayout) findViewById(R.id.indexLayout);
//		indexLayout.setBackgroundColor(Color.BLUE);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		
		GridView game = (GridView) findViewById(R.id.introScreenBoard);
		game.setAdapter(new IndexAdapter(this, width));
		
		Button play = (Button) findViewById(R.id.play_button);
		play.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent mainmenu = new Intent(context, MainMenu.class);
				startActivity(mainmenu);
				index.finish();
			}
		});
		
//		Log.d("bGAC", "Reached");
//		buildGoogleApiClient();
//
//		client.connect();
//		Log.d("Client", "Connected");
	}
	
//	public void buildGoogleApiClient() {
//		client = new GoogleApiClient.Builder(this)
//		.addApi(Plus.API)
//		.addScope(Plus.SCOPE_PLUS_LOGIN)
//        .addScope(Plus.SCOPE_PLUS_PROFILE)
//		.addApi(Games.API)
//		.addScope(Games.SCOPE_GAMES)
//		.addApi(Drive.API)
//        .addScope(Drive.SCOPE_APPFOLDER)
//		.addConnectionCallbacks(this)
//		.addOnConnectionFailedListener(this)
//		.build();
//	}

	private void getListOfOpponents(String [] requestIdsList) {
	    int gamesCount = requestIdsList.length;
	    int start = 0;
	    if (requestIdsList[0].equals("user_friends")) {
	        gamesCount -= 1;
	        start += 1;
        }
        if (requestIdsList[0].equals("public_profile") || requestIdsList[1].equals("public_profile")) {
	        gamesCount -= 1;
	        start += 1;
        }
	    for (int i = start; i < requestIdsList.length; i++) {
            try {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("fields", "ids, action_type, application, created_time, date, from, messages, object, to");
                GameRequest opponentRequest = new GameRequest();
                opponentRequest.createNewGameRequest("/" + requestIdsList[i], parameters, null);
                Game game = (Game) new GraphRequests().execute(opponentRequest).get();
                if (!game.lastMove.equals("")) {
                    availableGames.put(Long.parseLong(requestIdsList[i]), game);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        CharSequence[] opponents = new CharSequence[availableGames.size()];
	    for (int i = 0; i < availableGames.size(); i++) {
	        if (availableGames.get(availableGames.keySet().toArray()[i]).lastMove.equals("X")) {
	            opponents[i] = availableGames.get(availableGames.keySet().toArray()[i]).player1.playerName;
            } else if (availableGames.get(availableGames.keySet().toArray()[i]).lastMove.equals("O")) {
	            opponents[i] = availableGames.get(availableGames.keySet().toArray()[i]).player2.playerName;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Choose an Opponent")
                .setItems(opponents, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Intent board = new Intent(context, Board.class);
                        board.putExtra("Level", Integer.parseInt(availableGames.get(availableGames.keySet().toArray()[i]).data[9][3]));
                        board.putExtra("Player 1 Name", availableGames.get(availableGames.keySet().toArray()[i]).player1.playerName);
                        board.putExtra("Player 2 Name", availableGames.get(availableGames.keySet().toArray()[i]).player2.playerName);
                        if (availableGames.get(availableGames.keySet().toArray()[i]).lastMove.equals("X")) {
                            board.putExtra("Pending Player", availableGames.get(availableGames.keySet().toArray()[i]).player1.playerFBID);
                            board.putExtra("Current Player", availableGames.get(availableGames.keySet().toArray()[i]).player2.playerFBID);
                        } else {
                            board.putExtra("Pending Player", availableGames.get(availableGames.keySet().toArray()[i]).player2.playerFBID);
                            board.putExtra("Current Player", availableGames.get(availableGames.keySet().toArray()[i]).player1.playerFBID);
                        }
                        board.putExtra("Caller", "Index");
                        board.putExtra("My Turn", true);
                        board.putExtra("Finished", false);
                        board.putExtra("Multiplayer", true);
                        board.putExtra("GameRequestID", availableGames.get(availableGames.keySet().toArray()[i]).requestID);
                        index.finish();
                        startActivity(board);
                    }
                })
                .create()
                .show();
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

//        Log.d("bL", board[board.length - 1].toString());

		return board[board.length - 1][6];
	}

    public boolean checkMultiplayer(byte [] game) {
        String gameArray = new String(game);
        String [] rows = gameArray.split(";");
        String [][] board = new String [rows.length][];
        for(int i = 0; i < rows.length; i++) {
            board[i] = rows[i].split(",");
        }

        return !board[board.length - 1][6].equals("null");
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
//	public void registerTurnBasedClient(GoogleApiClient currentClient) {
////		Log.d("rTBC", "Registering...");
//		Games.TurnBasedMultiplayer.registerMatchUpdateListener(currentClient, notificationService.getContext());
//	}
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
			board.savedGameRecreate(match.getData());//, context);
			
			Board.keys = new Hashtable<Integer, Button>(6561);
//			Board.bottomPanel.removeAllViews();
//			Board.boardLayout.removeAllViews();
			ButtonPressed.currentTurn = "";
			board.finishActivity(context, true, opponentName);
		} else if ( currentPlayerParticipantResult != null && currentPlayerParticipantResult.getResult() == ParticipantResult.MATCH_RESULT_TIE) {
			Intent tieIntent = new Intent(context, Winner.class);
			tieIntent.putExtra("Match ID", match.getMatchId());
			tieIntent.putExtra("Caller", "Index");
			tieIntent.putExtra("Can Rematch", match.canRematch());
			tieIntent.putExtra("Winner", "Tie");

			Games.TurnBasedMultiplayer.finishMatch(client, match.getMatchId());
			board.savedGameRecreate(match.getData());//, context);
			
			Board.keys = new Hashtable<Integer, Button>(6561);
//			Board.bottomPanel.removeAllViews();
//			Board.boardLayout.removeAllViews();
			ButtonPressed.currentTurn = "";
			board.finishActivity(context, true, "Tie");
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

			board.savedGameRecreate(game);//, context);
			
			int level = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][5]);
			int row = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][0]);
			int column = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][1]);
			int multiplier = (int) Math.pow(3, (double) level);
			int key = row * multiplier + column;
			
			String turn = "";
			TextView playerTurn = board.findViewById(R.id.player_turn);
			if ( (match.getParticipant(getCurrentParticipantId(game)).getDisplayName()).equals(Board.wincheck[Board.wincheck.length - 1][2]) ) {
				Board.wincheck[Board.wincheck.length - 1][4] = "X";
				turn = "O";
				playerTurn.setText(Board.wincheck[Board.wincheck.length - 1][2] + "'s Turn");
			}
			else if ( (match.getParticipant(getCurrentParticipantId(game)).getDisplayName()).equals(Board.wincheck[Board.wincheck.length - 1][3]) ) {
				Board.wincheck[Board.wincheck.length - 1][4] = "O";
				turn = "X";
				playerTurn.setText(Board.wincheck[Board.wincheck.length - 1][3] + "'s Turn");
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
			
			bp = new ButtonPressed(context, level, availableGames.get(availableGames.keySet().toArray()[0]), board);
			
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
//		Log.d("TBMR", "onTurnBasedMatchRemoved " + arg0);
	}

	@Override
	public void onInvitationReceived(Invitation arg0) {
//		Log.d("IR", "onInvitationReceived");
	}

	@Override
	public void onInvitationRemoved(String arg0) {
//		Log.d("IR", "onInvitationRemoved");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
//		Log.d("CF", "onConnectionFailed");
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
//		Log.d("C", "" + connectionHint);
		SharedPreferences savedGame = PreferenceManager.getDefaultSharedPreferences(context);
		if(connectionHint != null) {
//            Log.d("cH", "not null");
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
				
//				Toast display = Toast.makeText(context, "Turn Based Match Received", Toast.LENGTH_SHORT);
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
			if ( checkMultiplayer(game) && (getCurrentTurn(game).equals("X") && !Games.Players.getCurrentPlayer(client).getDisplayName().equals(getPlayer(game, 1))) ||
                    (getCurrentTurn(game).equals("O") && !Games.Players.getCurrentPlayer(client).getDisplayName().equals(getPlayer(game, 2))) ) {
				myTurn = false;
			}
			Log.d("gS", gameString);
			if ( gameString.contains("X") || gameString.contains("O") ) {
				Intent board = new Intent(context, Board.class);
//				board.putExtra("On Going Match", game);
				board.putExtra("Level", getLevel(game));
                Log.d("MatchID", getMatchId(game));
				board.putExtra("Match ID", getMatchId(game));
				board.putExtra("Pending Player", getPendingParticipantId(game));
				board.putExtra("Current Player", getCurrentParticipantId(game));
				board.putExtra("Caller", "Index");
				board.putExtra("My Turn", myTurn);
				board.putExtra("Finished", false);
				board.putExtra("Can Rematch", true);
				board.putExtra("Multiplayer", checkMultiplayer(game));
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
        super.onActivityResult(request, response, data);

        if (request == REQUEST_RESOLVE_ERROR) {
        	client.connect();
        } else {
			loginCallbackManager.onActivityResult(request, response, data);
		}
    }
	
	private String getPendingParticipantId(TurnBasedMatch match) {
		ArrayList<String> participantIds = match.getParticipantIds();
		String pendingParticipantId = "";
		String currentParticipantId = match.getParticipantId(Games.Players.getCurrentPlayerId(client));
//		Log.d("cPId", currentParticipantId);
//        Log.d("pIds", "" + participantIds);
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
		level = Integer.parseInt(board[rows.length - 1][5]);
		return level;
	}
}

