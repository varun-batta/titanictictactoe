package com.varunbatta.titanictictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.GameRequestDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.multiplayer.ParticipantResult;

public class ButtonPressed implements OnClickListener {
    Context context;
    Game game;
	static int level;
	static String turn = "";
	static String wincheck[][] = new String [10][9];
	static String metawincheck[][] = new String [3][3];
	private int row = -1;
    private int column = -1;
	static String currentTurn = "";
    private TextView winningletter;
	Board board;
	TextView playerturn;

	private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

	ButtonPressed (Context context, int level, Game game, Board board) {
		this.context = context;
		ButtonPressed.level = level;
		this.game = game;
		this.board = board;
        this.playerturn = board.findViewById(R.id.player_turn);
		winningletter = new TextView(context);
	}
	
	private int boardSize(double level) {
		return (int)Math.pow(3, level);
	}

	@Override
	public void onClick(View v) {
		currentTurn = game.lastMove.equals("") ? "X" : game.lastMove.equals("X") ? "O" : "X";
		wincheck = game.data;
		Button pressedButton = (Button) v;
		String pressedButtonText = pressedButton.getText().toString();
		board = new Board();
		
		//TODO: Refactor this function!!
		if((pressedButtonText.equals("Menu") || pressedButtonText.equals("Save") ||
				pressedButtonText.equals("Main Menu") || pressedButtonText.equals("New Game") || pressedButtonText.equals("New WiFi Game")) || 
				pressedButtonText.equals("Back")) {
			board.bottomPanelListener(context, pressedButtonText);
			return;
		}
		if(currentTurn.equals("X")) {
			turn = "X";
			playerturn.setText(game.player2.playerName + "'s Turn!");
			currentTurn = "O";
		}
		else {
			turn = "O";
			playerturn.setText(game.player1.playerName + "'s Turn!");
			currentTurn = "X";
		}
		pressedButton.setText(turn);
		game.lastMove = turn;
		pressedButton.setEnabled(false);


		if(level == 1){
			for(int i = 0; i<3; i++){
				for(int j = 0; j<3; j++){
					Button selectedButton = Board.keys.get(i*3+j);
					if (selectedButton.getId() == pressedButton.getId()) {
						row = i;
						column = j;
					}
				}
			}
		}

		boolean found = false;

		if(level == 2){
			for(int i = 0; i<9; i++){
				for(int j = 0; j<9; j++){
					if(metawincheck[i/3][j/3] == null) {
						Button selectedButton = Board.keys.get(i*9+j);
						if(selectedButton.getId() == pressedButton.getId()){
							row = i;
							column = j;
							found = true;
							break;
						}
					}
				}
				if(found) {
					break;
				}
			}
		}

		if(Board.multiplayer) {
			Board.wincheck[row][column] = turn;
		}
		wincheck[row][column] = turn;

		wincheck[wincheck.length-1][0] = Integer.toString(row);
		wincheck[wincheck.length-1][1] = Integer.toString(column);
		wincheck[wincheck.length-1][2] = game.player1.playerName;
		wincheck[wincheck.length-1][3] = game.player2.playerName;
		wincheck[wincheck.length-1][4] = turn;
		wincheck[wincheck.length-1][5] = "" + level;

		boardChanger(row, column, Board.level, !Board.multiplayer);

		boolean winOrTie = winChecker(row, column, Board.level, Board.level, wincheck, "");

		if(Board.multiplayer) {
			Player toPlayer;
			Player fromPlayer;
			if (turn.equals("X")) {
				toPlayer = game.player2;
				fromPlayer = game.player1;
			} else {
				toPlayer = game.player1;
				fromPlayer = game.player2;
			}
			String messageText;
			String titleText;
			//TODO: Consider first turn case
			if (winChecker(row, column, 1, Board.level, level == 2 ? metawincheck : wincheck, "")) {
				messageText = fromPlayer.playerName + " has won the game!";
				titleText = "Game Over";
				Board.winOrTie = winOrTie;
				Board.turn = turn;
			} else {
				messageText = fromPlayer.playerName + " has played and now it is your turn";
				titleText = "Your Turn";
			}
			String game = wincheckerToString();
			Map<String, String> params = new HashMap<String, String>();
			params.put("data", game);
			params.put("message", messageText);
			params.put("title", titleText);
			makeTurn(toPlayer.playerFBID, params);
		}
	}

	private String wincheckerToString() {
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
		game.append(level).append(",,,,,,;");
		return game.toString();
	}

	public void boardChanger(int f, int g, int n, boolean clickable) {
		//TODO: Refactor this function completely!!!
        int m = f;
        int h = g;
        int l = boardSize(n) / 3;
        int r = l / 3;

        String bcwincheck[][] = new String[l][l];
        if (n == 2) {
            bcwincheck = metawincheck;
        }

        m = m % l;
        h = h % l;

        if (n == 1) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int key = i * 3 + j;
                    Button button = Board.keys.get(key);
                    button.setClickable(clickable);
                }
            }
        } else {
            if (bcwincheck[m][h] == null && !tieChecker("Inner", n, m, h)) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (bcwincheck[i][j] == null) {
                            if (n == 2) {
                                for (int k = 0; k < 9; k++) {
                                    for (int o = 0; o < 9; o++) {
                                        int key = k * 9 + o;
                                        if (bcwincheck[k / 3][o / 3] == null) {
                                            Button button = Board.keys.get(key);
                                            button.setEnabled(false);
                                            ImageView section = BasicBoardView.metaBoard[k / 3][o / 3].findViewById(R.id.boardBackgroundRed);
                                            section.setImageAlpha(150);
                                        }
                                    }
                                }
                            }
                        }
                        for (int p = 0; p < 3; p++) {
                            for (int q = 0; q < 3; q++) {
                                Button button = Board.keys.get((m % 3) * 27 + p * 9 + (h % 3) * 3 + q);
                                if (button != null && button.getText().toString().equals("")) {
                                    button.setEnabled(true);
                                    ImageView selectedSection = BasicBoardView.metaBoard[m][h].findViewById(R.id.boardBackgroundRed);
                                    selectedSection.setImageAlpha(0);
                                    if (Board.multiplayer) {
                                        button.setClickable(clickable);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (bcwincheck[i / 3][j / 3] == null) {
                            Button button = Board.keys.get(i * 9 + j);
                            if (button != null && (button.getText() == null || button.getText().toString().equals(""))) {
                                button.setEnabled(true);
                                button.setClickable(clickable);
                                ImageView section = BasicBoardView.metaBoard[i / 3][j / 3].findViewById(R.id.boardBackgroundRed);
                                section.setImageAlpha(0);
                            }
                        }
                    }
                }
            }
        }
    }
	
	public boolean tieChecker(String checking, int n, int m, int h) {
        int q = 0;
        int u = 0;
        int t = 0;

        if (n == 1 && checking.equals("Inner")) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Button button = Board.keys.get(i * 3 + j);
                    if (button != null && !button.getText().toString().equals("")) {
                        u++;
                    }
                }
            }
        }
        if (n == 2 && checking.equals("Inner") && metawincheck[m % 3][h % 3] == null) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Button button = Board.keys.get((m % 3) * 27 + i * 9 + (h % 3) * 3 + j);
                    if (button != null && !button.getText().toString().equals("")) {
                        u++;
                    }
                }
            }
        }
        if (n == 2 && checking.equals("Outer")) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (metawincheck[i][j] == null) {
                        for (int k = 0; k < 3; k++) {
                            for (int o = 0; o < 3; o++) {
                                Button button = Board.keys.get(i * 27 + k * 9 + j * 3 + o);
                                if (button != null && !button.getText().toString().equals("")) {
                                    u++;
                                }
                            }
                        }
                    } else {
                        q++;
                    }
                }
            }
        }

        return checking.equals("MetaOuter") && (q * 81 + u * 9 + t) == 729 || checking.equals("Outer") && (q * 9 + u) == 81 || checking.equals("Inner") && u == 9;
    }

	public boolean winChecker(int f, int g, int n, int actual, String[][] winchecker, String turnValue) {
		//TODO: Refactor this function
		boolean winOrTie = false;
		String value = "";
		String value1 = "";
		String value2 = "";
		String x = "";
		int q = -1;
		int r = -1;
//		int length = winchecker.length;
//		int width = winchecker[0].length;
		
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
		if(value != null && value1 != null && value2 != null) {
			if(value.equals(value1) && value1.equals(value2)){
				if(turnValue.equals(""))
					x = turn;
				else
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
		
		
		if(value != null && value1 != null && value2 != null){
			if(value.equals(value1) && value1.equals(value2)){
				if(turnValue.equals(""))
					x = turn;
				else
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

		if(value != null && value1 != null && value2 != null){
			if(value.equals(value1) && value1.equals(value2)){
				if(turnValue.equals(""))
					x = turn;
				else
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
			
		if(value != null && value1 != null && value2 != null){
			if(value.equals(value1) && value1.equals(value2)){
				if(turnValue.equals(""))
					x = turn;
				else
					x = turnValue;
			}
		}
		String playerwin = "";
		if(x.equals("X")){
			playerwin = game.player1.playerName;
			winningletter.setText("X");
		}
		if (x.equals("O")){
			playerwin = game.player2.playerName;
			winningletter.setText("O");
		}
		
//		Log.d("x", x);
//		Log.d("n", "" + n);
//		Log.d("actual", "" + actual);
		
		if(x.equals("O") || x.equals("X")){
//			Toast winToast = Toast.makeText(context, playerwin+" wins!", Toast.LENGTH_SHORT);
//			board = new Board();
			switch(n){
			case 1:
//				Log.d("Winning Board Changer", "Called");
//				board.WinningBoardChanger(f, g, n, actual, winToast, winningletter, x, context, wincheck);
				if(!Board.multiplayer) {
//					Log.d("winchecker", "finishGame");
//					finishGame(false);
//				}
//				else {
					board.finishActivity(context, true, playerwin);
				}
                Board.winOrTie = true;
				winOrTie =  true;
				break;
			case 2:
				if(actual == 2){
					Log.d("Change", x + " " + actual);
					board.WinningBoardChanger(f, g, n, actual, x, context, wincheck);
				}
//				if(actual == 3){
//					board.WinningBoardChanger(q, r, n, actual, x, context, wincheck);
//				}
//				if(actual == 4){
//					board.WinningBoardChanger(q, r, n, actual, x, context, wincheck);
//				}
				break;
//			case 3:
//				if(actual == 3){
//					board.WinningBoardChanger(f, g, n, actual, x, context, wincheck);
//				}
//				if(actual == 4){
//					board.WinningBoardChanger(q, r, n, actual, x, context, wincheck);
//				}
//				break;
//			case 4:
//				if(actual == 4){
//					board.WinningBoardChanger(f, g, n, actual, x, context, wincheck);
//				}
			}
		}
		
		String tieCheckerString = "";
		switch(Board.level) {
		case 1:
			tieCheckerString = "Inner";
			break;
		case 2:
			tieCheckerString = "Outer";
			break;
		case 3:
			tieCheckerString = "MetaOuter";
			break;
		case 4:
			tieCheckerString = "MetaMetaOuter";
			break;
		}
		
		Log.d("CT", "Checking Tie" + Board.level + x);
		if(!winOrTie && (!x.equals("X") && !x.equals("O")) && tieChecker(tieCheckerString, n, f, g)) {
			if(Board.multiplayer) {
				Log.d("tieChecker", "finishGame");
				finishGame(true);
			} else {
				board.finishActivity(context, true, "Tie");
			}
			winOrTie = true;
		}
		
		return winOrTie;
	}

	public void finishGame(boolean tie) {
		AsyncTask<Boolean, Void, String> finishGameTask = new AsyncTask<Boolean, Void, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				inAnimation = new AlphaAnimation(0f, 1f);
				inAnimation.setDuration(200);
				Board.progressBarHolder.setAnimation(inAnimation);
				Board.progressBarHolder.setVisibility(View.VISIBLE);
			}

			@Override
			protected String doInBackground(Boolean... params) {
				String tieString = null;
				if(params[0]) {
					tieString = "Tie";
				}
				currentTurn = "";
				return tieString;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				outAnimation = new AlphaAnimation(1f, 0f);
				outAnimation.setDuration(200);
				Board.progressBarHolder.setAnimation(outAnimation);
				Board.progressBarHolder.setVisibility(View.GONE);
				Log.d("Won", "" + true);
				board.finishActivity(context, true, result);
				currentTurn = "";
			}

		};
		
		finishGameTask.execute(tie);
	}

	private void makeTurn(long to, Map<String, String> params) {
		GameRequestContent.Builder requestContent = new GameRequestContent.Builder();
		requestContent.setRecipients(Arrays.asList("" + to));
		requestContent.setMessage(params.get("message"));
		requestContent.setData(params.get("data"));
		requestContent.setTitle(params.get("title"));
		requestContent.setActionType(GameRequestContent.ActionType.TURN);

		Board.requestDialog.show(requestContent.build());
	}

	public static byte[] wincheckerToByteArray(String[][] winchecker) {
		StringBuilder gameString = new StringBuilder();
        for (String[] aWinchecker : winchecker) {
            for (int j = 0; j < winchecker[0].length; j++) {
                gameString.append(aWinchecker[j]).append(",");
            }
            gameString.append(";");
        }
		return gameString.toString().getBytes();
	}
}
