/**
 * 
 */
package com.varunbatta.titanictictactoe;

import java.util.ArrayList;
import java.util.Hashtable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.internal.constants.TurnBasedMatchStatus;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.plus.Plus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

/**
 * @author Varun
 *
 */
public class NotificationService extends Service implements OnTurnBasedMatchUpdateReceivedListener, ConnectionCallbacks, 
OnConnectionFailedListener {
	
	public static NotificationManager notificationManager;
    private static PowerManager powerManager;
	Board board;
	public static boolean receiving;
    public static boolean turnMade;
	private static GoogleApiClient client;
	private static Context context;
	
	public static int NOTIFICATION = 20202020;
	
	public NotificationService getContext() {
		return this;
	}
	
	@Override
    public void onCreate() {
		super.onCreate();
		
		Log.d("nSoC", "notificationService onCreate");
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        Log.d("PM", "" + powerManager);

        context = getApplicationContext();
        
        client = new GoogleApiClient.Builder(this)
		.addApi(Plus.API)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
		.addApi(Games.API)
		.addScope(Games.SCOPE_GAMES)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();
        
        client.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
        Log.i("NotificationService", "Received start id " + startId + ": " + intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.i("NotificationService", "Destroyed");
        // Cancel the persistent notification.
        notificationManager.cancel(NOTIFICATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.d("oB", "on Bind");
        return remoteService;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private IRemoteService.Stub remoteService = 
    		new IRemoteService.Stub() {
    	public IRemoteService.Stub getService() {
    		return this;
    	}};

    /**
     * Show a notification while this service is running.
     */
    public void showNotification(Intent intent, Context context, String contentTitle, 
    		String contentText) {
    	notificationManager = (NotificationManager)(this.context).getSystemService(NOTIFICATION_SERVICE);
    	
    	TaskStackBuilder tsBuilder = TaskStackBuilder.create(context);
		tsBuilder.addParentStack(Index.class);
		tsBuilder.addNextIntent(intent);
		
		PendingIntent pIntent = tsBuilder.getPendingIntent(123456, PendingIntent.FLAG_CANCEL_CURRENT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    	NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_stat_titanictictactoenotificationicon)
		.setContentTitle(contentTitle)
		.setContentText(contentText)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setSound(defaultSound)
		.setContentIntent(pIntent)
		.setAutoCancel(true);
    	
        // Send the notification.
        notificationManager.notify(NOTIFICATION, notificationBuilder.build());
    }

    @Override
	public void onTurnBasedMatchReceived(TurnBasedMatch match) {
		receiving = true;
        turnMade = true;
    	Log.d("NotificationService", "onTurnBasedMatchReceived");
        Log.d("PMSO", "" + powerManager.isInteractive());
		board = new Board();
		String opponentName = match.getParticipant(getPendingParticipantId(match)).getDisplayName();
		ParticipantResult currentPlayerParticipantResult = match.getParticipant(match.getParticipantId(Games.Players.getCurrentPlayerId(client))).getResult();
        if ( match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN && Board.boardVisible) {
            byte[] game = match.getData();
            Board.savedGameRecreate(game, context);

            int level = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][5]);
            int row = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][0]);
            int column = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][1]);
            int multiplier = (int) Math.pow(3, (double) level);
            int key = row * multiplier + column;

            String turn = "";

            ButtonPressed.currentTurn = Board.wincheck[Board.wincheck.length - 1][4];

            if (Board.wincheck[Board.wincheck.length - 1][4].equals("X")) {
                turn = "O";
                ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][2] + "'s Turn");
            } else {
                turn = "X";
                ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][3] + "'s Turn");
            }

            Button opponentMove = Board.keys.get(key);
            opponentMove.setText(turn);
            opponentMove.setEnabled(false);

            ButtonPressed bp = new ButtonPressed(ButtonPressed.context, level);

            if (level >= 2) {
                bp.boardChanger(row, column, level, true);
            }

            if (!bp.winChecker(row, column, level, level, Board.wincheck, turn) && ButtonPressed.metawincheck[row / 3][column / 3] == null) {
                for (int i = 0; i < multiplier; i++) {
                    for (int j = 0; j < multiplier; j++) {
                        int buttonKey = i * multiplier + j;
                        String metaValue = ButtonPressed.metawincheck[i / 3][j / 3];
                        if (metaValue == null) {
                            Log.d("buttonKey", "" + buttonKey);
                            Button button = Board.keys.get(buttonKey);
                            button.setClickable(true);
                        }
                    }
                }
            }
        } else {
            Log.d("NMToBV", "Not my Turn or Board Visible");
            if (currentPlayerParticipantResult != null && currentPlayerParticipantResult.getResult() == ParticipantResult.MATCH_RESULT_LOSS) {
                Intent lossIntent = new Intent(context, Winner.class);
                lossIntent.putExtra("Multiplayer", true);
                lossIntent.putExtra("Match ID", match.getMatchId());
                lossIntent.putExtra("Caller", "Index");
                lossIntent.putExtra("Can Rematch", match.canRematch());
                lossIntent.putExtra("Winner", opponentName);
                showNotification(lossIntent, context, "Game Over", opponentName + " Wins!");
                Games.TurnBasedMultiplayer.finishMatch(client, match.getMatchId());
                Board.savedGameRecreate(match.getData(), context);

                Board.keys = new Hashtable<Integer, Button>(6561);
                Board.bottomPanel.removeAllViews();
                Board.boardLayout.removeAllViews();
                ButtonPressed.currentTurn = "";
                board.finishActivity(ButtonPressed.context, true, opponentName);
            } else if (currentPlayerParticipantResult != null && currentPlayerParticipantResult.getResult() == ParticipantResult.MATCH_RESULT_TIE) {
                Intent tieIntent = new Intent(context, Winner.class);
                tieIntent.putExtra("Match ID", match.getMatchId());
                tieIntent.putExtra("Caller", "Index");
                tieIntent.putExtra("Can Rematch", match.canRematch());
                tieIntent.putExtra("Winner", "Tie");
                showNotification(tieIntent, context, "Game Over", "Tie game with " + opponentName);
                Games.TurnBasedMultiplayer.finishMatch(client, match.getMatchId());
                Board.savedGameRecreate(match.getData(), context);

                Board.keys = new Hashtable<Integer, Button>(6561);
                Board.bottomPanel.removeAllViews();
                Board.boardLayout.removeAllViews();
                ButtonPressed.currentTurn = "";
                board.finishActivity(ButtonPressed.context, true, "Tie");
            } else if (!powerManager.isInteractive() && !Board.boardVisible) {
                Log.d("SOff", "Screen Off Notification");
                byte[] game = match.getData();
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
                showNotification(turnIntent, context, "Your Turn", opponentName + " has made a move");
                Board.savedGameRecreate(game, context);

                //		} else if ( match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN && Board.boardVisible) {
                //			byte [] game = match.getData();
                //			Intent turnIntent = new Intent(context, Board.class);
                //			turnIntent.putExtra("On Going Match", game);
                //			turnIntent.putExtra("Level", getLevel(game));
                //			turnIntent.putExtra("Match ID", getMatchId(game));
                //			turnIntent.putExtra("Pending Player", getPendingParticipantId(game));
                //			turnIntent.putExtra("Current Player", getCurrentParticipantId(game));
                //			turnIntent.putExtra("Caller", "Index");
                //			turnIntent.putExtra("My Turn", true);
                //			turnIntent.putExtra("Finished", false);
                //			turnIntent.putExtra("Can Rematch", true);
                //			turnIntent.putExtra("Multiplayer", true);
                //			showNotification(turnIntent, context, "Your Turn", opponentName + " has made a move");
                //			Board.savedGameRecreate(game, context);
                //
                //			int level = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][5]);
                //			int row = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][0]);
                //			int column = Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][1]);
                //			int multiplier = (int) Math.pow(3, (double) level);
                //			int key = row * multiplier + column;
                //
                //			String turn = "";
                //
                ButtonPressed.currentTurn = Board.wincheck[Board.wincheck.length - 1][4];
                //
                //			if(Board.wincheck[Board.wincheck.length - 1][4].equals("X")) {
                //				turn = "O";
                //				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][2] + "'s Turn");
                //			} else {
                //				turn = "X";
                //				ButtonPressed.playerturn.setText(Board.wincheck[Board.wincheck.length - 1][3] + "'s Turn");
                //			}
                //
                //			Button opponentMove = Board.keys.get(key);
                //			opponentMove.setText(turn);
                //			opponentMove.setEnabled(false);
                //
                //			ButtonPressed bp = new ButtonPressed(ButtonPressed.context, level);
                //
                //			if( level >= 2) {
                //				bp.boardChanger(row, column, level, true);
                //			}
                //
                //			if ( !bp.winChecker(row, column, level, level, Board.wincheck, turn) && ButtonPressed.metawincheck[row/3][column/3] == null) {
                //				for ( int i = 0; i < multiplier; i++ ) {
                //					for ( int j = 0; j < multiplier; j++ ) {
                //						int buttonKey = i * multiplier + j;
                //						String metaValue = ButtonPressed.metawincheck[i/3][j/3];
                //						if(metaValue == null) {
                //							Log.d("buttonKey", "" + buttonKey);
                //							Button button = Board.keys.get(buttonKey);
                //							button.setClickable(true);
                //						}
                //					}
                //				}
                //			}
            } else if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN){
                Log.d("IndexN", "Index Notification");
                byte [] game = match.getData();
                Board.savedGameRecreate(game, context);
                int tbms = match.getTurnStatus();
                Log.d("TurnStatus", "" + match.getTurnStatus());
                int ms = match.getStatus();
                Log.d("MatchStatus", "" + match.getStatus());
                Intent turnIntent = new Intent(context, Index.class);
                turnIntent.putExtra("Game", match.getData());
                showNotification(turnIntent, context, "Your Turn", opponentName + " has made a move");
            }
        }
	}

	@Override
	public void onTurnBasedMatchRemoved(String arg0) {
		Log.d("TBMR", "onTurnBasedMatchRemoved " + arg0);
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
		level = Integer.parseInt(board[9][5]);
		return level;
	}
	
	private String getCurrentParticipantId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
		return board[9][8];
	}

	private String getPendingParticipantId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
		return board[9][7];
	}

	private String getMatchId(byte[] game) {
		String gameArray = new String(game);
		String [] rows = gameArray.split(";");
		String [][] board = new String [rows.length][];
		for(int i = 0; i < rows.length; i++) {
			board[i] = rows[i].split(",");
		}
		return board[9][6];
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
