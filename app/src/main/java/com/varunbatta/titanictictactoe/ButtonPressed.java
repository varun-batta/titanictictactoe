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
//	TextView playerturn;// = Board.playerTurn;
	static String turn = "";
	static String wincheck[][] = new String [10][9];
	static String metawincheck[][] = new String [3][3];
//	static String metametawincheck[][] = new String [3][3];
//	static String metametaminiwincheck[][] =  new String[9][9];
//	static String metametametaminiminiwincheck[][] = new String[27][27];
//	static String metametametaminiwincheck[][] =  new String[9][9];
//	static String metametametawincheck[][] =  new String[3][3];
	private int row = -1;
    private int column = -1;
//	String currentplayer = "";
//	String otherplayer = "";
	static String currentTurn = "";
//	String saved = "";
//	static int won = 0;
    private TextView winningletter;
	Board board;
	TextView playerturn;
//    private String matchId;
//    private String alternatePlayer = "";
//	static boolean savedMultiplayer = false;

//    private boolean saveGameCalled = false;
	
	private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

//    private FrameLayout progressBarHolder;
    
//    private Index index;

//    static String methodCaller;

	ButtonPressed (Context context, int level, Game game, Board board) {
		this.context = context;
		ButtonPressed.level = level;
		this.game = game;
		this.board = board;
        this.playerturn = board.findViewById(R.id.player_turn);
//		ButtonPressed.wincheck = game.data;
//		Log.d("currentTurn", "currentTurn = " + currentTurn);
//		if(currentTurn.equals("") && Board.currentTurn != null) {
//			this.currentTurn = Board.currentTurn;
//			Toast.makeText(context, "currentTurn: " + currentTurn, Toast.LENGTH_SHORT).show();
//		}
//		this.matchId = Board.matchId;
//		this.alternatePlayer = ""+Board.pendingPlayerId;
		winningletter = new TextView(context);
	}
	
	private int boardSize(double level) {
		return (int)Math.pow(3, level);
	}

	@Override
	public void onClick(View v) {
//		saveGameCalled = false;
//		String player = (String) Board.playerTurn.getText();
		
		currentTurn = game.lastMove.equals("") ? "X" : game.lastMove.equals("X") ? "O" : "X";
		wincheck = game.data;
//			ButtonPressed.currentTurn = Board.currentTurn;
//			Toast.makeText(context, "currentTurn: " + currentTurn, Toast.LENGTH_SHORT).show();

//        Log.d("tCT", ButtonPressed.currentTurn);
//        Log.d("BCT", Board.currentTurn);
//		if(com.example.titanictictactoe.GameSaver.save.equals("Saved"))
//			wincheck = newDossier.GameSaver.completegame;
		
//		index = new Index();
//
		Button pressedButton = (Button) v;
		String pressedButtonText = pressedButton.getText().toString();
//		Log.d("pBT", pressedButtonText);
		board = new Board();
		
		if((pressedButtonText.equals("Menu") || pressedButtonText.equals("Save") ||
				pressedButtonText.equals("Main Menu") || pressedButtonText.equals("New Game") || pressedButtonText.equals("New WiFi Game")) || 
				pressedButtonText.equals("Back")) {
			board.bottomPanelListener(context, pressedButtonText);
			return;
		}

//		if(pressedButton == com.example.titanictictactoe.Board.quit){
//			if(saved.equals(""))
//			{
//				Toast warning = Toast.makeText(context, "Exit without saving?", Toast.LENGTH_SHORT);
//				warning.show();
////				if(n == 0)
////				{
////					String fileNm = newDossier.Board.player1 + " vs. " + newDossier.Board.player2 + " - Level " + newDossier.Board.n+".txt";
////					File fileDelete = new File(fileNm);
////					fileDelete.delete();
////					System.exit(0);
////				}
//			}
//			else
//			{
//				System.exit(0);
//			}
//		}
//		else{
//			if(pressedButton == com.example.titanictictactoe.Board.save){
//				saved = "Saved";
//				SaveButton(wincheck);
//			}
//			else{
//				if(pressedButton == com.example.titanictictactoe.Board.levelmenu){
//					
////					newDossier.Board.game.setVisible(false);
////					new Menu();
////					//newDossier.Index.begin.setVisible(false);
////					Index main = new Index();
////					main.levelmenu();
//					return;
//				}
//				else{
                if(currentTurn.equals("X")) {
                    turn = "X";
                    //System.out.println(playerturn.getText());
                    playerturn.setText(game.player2.playerName + "'s Turn!");
//						currentplayer = Board.player2;
//						otherplayer = Board.player1;
//						alternatePlayer = Board.player2;
                    currentTurn = "O";
                }
                else {
                    turn = "O";
//						Log.d("PlayerTurn", (String) playerturn.getText());
                    playerturn.setText(game.player1.playerName + "'s Turn!");
//						currentplayer = Board.player1;
//						otherplayer = Board.player2;
//						alternatePlayer = Board.player1;
                    currentTurn = "X";
                }
				pressedButton.setText(turn);
		        game.lastMove = turn;
//				if(Board.level==4) {
//					if(turn.equals("X"))
//						pressedButton.setBackgroundColor(Color.RED);
//					if(turn.equals("O"))
//						pressedButton.setBackgroundColor(Color.BLUE);
//				}
				pressedButton.setEnabled(false);
		
		
//				String value;

//                for(int i=0; i<3; i++){
//                    for(int j=0; j<3; j++){
//                        value = metawincheck[i][j];
//                        if(value != null) {
//                            metawincheck[i][j]=value;
//                        }
//                        else
//                            metawincheck[i][j] = "";
//                    }
//                }
//				if(/*newDossier.Index.n>0 &&*/ Menu.n == 1){
//					for(int i=0; i<wincheck.length; i++){
//						for(int j=0; j<wincheck[0].length; j++){
//							wincheck[i][j]="";
//						}
//					}
//					wincheck = new String[boardSize(Board.n)+1][boardSize(Board.n)];
//				}
//				for(int i = 0; i < boardSize(level)+1; i++){
//					for(int j = 0; j < boardSize(level); j++){
//						value = wincheck[i][j];
//						if(!value.equals("")){
//							wincheck[i][j] = value;
//						}
//						else
//							wincheck[i][j] = "";
//					}
//				}

				if(level == 1){
					for(int i = 0; i<3; i++){
						for(int j = 0; j<3; j++){
							Button selectedButton = Board.keys.get(i*3+j);
							if(selectedButton.getId() == pressedButton.getId()){
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
				
//				if(Board.level == 3){
//					for(int i=0; i<27; i++){
//						for(int j=0; j<27; j++){
//							if(Board.keys.get(i*27+j) == pressedButton){
//								row = i;
//                                column = j;
//							}}}}
//				if(Board.level == 4){
//					for(int i=0; i<81; i++){
//						for(int j=0; j<81; j++){
//							if(Board.keys.get(i*81+j) == pressedButton){
//								row = i;
//                                column = j;
//							}}}}
				
				if(Board.multiplayer) {
					Board.wincheck[row][column] = turn;
				}
				wincheck[row][column] = turn;

//				for(int i=0; i<3; i++){
//					for(int j=0; j<3; j++){
//						value = metametawincheck[i][j];
//						if(value != ""){
//							metametawincheck[i][j]=value;
//						}
//						else
//							metametawincheck[i][j]="";
//					}
//				}
				
//				Log.d("Player 1", Board.player1);
//				Log.d("Player 2", Board.player2);
//				Log.d("pPId", Board.pendingPlayerId);
//                Log.d("cPId", Board.currentPlayerId);

				wincheck[wincheck.length-1][0] = Integer.toString(row);
				wincheck[wincheck.length-1][1] = Integer.toString(column);
				wincheck[wincheck.length-1][2] = game.player1.playerName;
				wincheck[wincheck.length-1][3] = game.player2.playerName;
				wincheck[wincheck.length-1][4] = turn;
				wincheck[wincheck.length-1][5] = "" + level;
//				wincheck[wincheck.length-1][6] = matchId;
//				wincheck[wincheck.length-1][7] = ""+Board.pendingPlayerId;
//				wincheck[wincheck.length-1][8] = ""+Board.currentPlayerId;
			
				boardChanger(row, column, Board.level, !Board.multiplayer);
				
				boolean winOrTie = winChecker(row, column, Board.level, Board.level, wincheck, "");
				
//				Log.d("winOrTie", "" + winOrTie);
//				Log.d("Continue", "ButtonPressed");
				if(Board.multiplayer) {
//					Log.d("multiplayer", "" + Board.multiplayer);
//					saveGame(wincheck, matchId, alternatePlayer);
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
//				Log.d("Done", "ButtonPressed");
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
        int m = f;
        int h = g;
        int l = boardSize(n) / 3;
        int r = l / 3;

        Log.d("BoardChanger", "starting");

//		String abwincheck[][]=new String[3][3];
//		String cdwincheck[][]=new String[9][9];
        String bcwincheck[][] = new String[l][l];
        if (n == 2) {
            bcwincheck = metawincheck;
        }

//		if(n==3){
//			abwincheck = metametawincheck;
//			bcwincheck = metametaminiwincheck;}
//
//		if(n==4){
//			abwincheck = metametametawincheck;
//			cdwincheck = metametametaminiwincheck;
//			bcwincheck = metametametaminiminiwincheck;}

        m = m % l;
        h = h % l;

//		if(abwincheck[m/r][h/r] == null){
//			if(cdwincheck[m/3][h/3] == null){
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
                            //for(int k=0; k<9; k++){
                            if (n == 2) {
                                for (int k = 0; k < 9; k++) {
                                    for (int o = 0; o < 9; o++) {
                                        int key = k * 9 + o;
                                        if (bcwincheck[k / 3][o / 3] == null) {
//												Log.d("Button Disabled", "" + key);
                                            Button button = Board.keys.get(key);
                                            //											Log.d("key", k + ", " + o);
                                            button.setEnabled(false);
                                            ImageView section = BasicBoardView.metaBoard[k / 3][o / 3].findViewById(R.id.boardBackgroundRed);
                                            section.setImageAlpha(150);
                                        }
                                    }
                                }
                                //for(Component comp : newDossier.Board.metaboard[i][j].getComponents())
                                //if(comp.isVisible())
                                //((JPanel)comp).getComponent(k).setEnabled(false);
                                //newDossier.Board.metaboard[i][j].getComponent(k).setEnabled(false);
                            }
                            //}
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
//						Log.d("co-ords", i + ", " + j);
                        for (int k = 0; k < 3; k++) {
                            for (int o = 0; o < 3; o++) {
//								Log.d("ico-ords", k + ", " + o);
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
//			for(int k=0; k<9; k++){
//				for(Component comp : newDossier.Board.metaboard[m][h].getComponents()) {
//					if(comp.isVisible())
//					{
//						Component [] components = ((JPanel)comp).getComponents();
//						if(components.length>1){
//							if(!((JButton)((JPanel)comp).getComponent(k)).getText().equals(""))
//								u++;
//						}
//					}
//				}
//			}
//		}
//		if(n==4&&checking.equals("Inner")){
//			for(int k=0; k<3; k++){
//				for(int j=0; j<3; j++){
//					if(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).isVisible()){
//						if(!(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).getText().equals(""))){
//							u++;
//						}
//					}
//				}
//			}
			/*for(int k=0; k<9; k++){
				Component[] components = ((JPanel)((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(((m%9)/3)*3+(h%9)/3)).getComponent(((m%9)%3)*3+(h%9)%3)).getComponents();
				if(components.length>1){
					if(!(((JButton)((JPanel)((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(((m%9)/3)*3+(h%9)/3)).getComponent(((m%9)%3)*3+(h%9)%3)).getComponent(k)).getText().equals("")))
						u++;}}*///}
//		if(n==4&&checking.equals("Outer")){
//			for(int k=0; k<3; k++){
//				for(int j=0; j<3; j++){
//					//Component [] components = ((JPanel)((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(((m%9)/3)*3+(h%9)/3)).getComponent(k)).getComponents();
//					//!(metametametaminiminiwincheck[(m/9)*9+((m%9)/3)*3+k][(h/9)+((h%9)/3)*3+j].equals(""))||
//					if(!(metametametaminiminiwincheck[(m/9)*9+((m%9)/3)*3+k][(h/9)+((h%9)/3)*3+j]==null))//components.length<=1)
//						q++;
//					//for(int a=0; a<9; a++){
//					if(metametametaminiminiwincheck[(m/9)*9+((m%9)/3)*3+k][(h/9)+((h%9)/3)*3+j]==null){
//						if(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).isVisible()){
//							if(!(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).getText().equals(""))){
//								u++;
//							}
//						}
//					}
//				}
//			}
//		}
//		if(n==4&&checking.equals("MetaOuter")){
//			for(int k=0; k<3; k++){
//				for(int j=0; j<3; j++){
//					//for(int k=0; k<9; k++){
//					//Component [] componentsx3 = ((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(k)).getComponents();
//					if(!(metametametaminiwincheck[(m/9)*3+k][(h/9)*3+j]==null)){
//						q++;
//					}
//					for(int a=0; a<3; a++){
//						for(int b=0; b<3; b++){
//							//for(int a=0; a<9; a++){
//							if(metametametaminiwincheck[(m/9)*3+k][(h/9)*3+j]==null){
//								//Component [] componentsx9 = ((JPanel)((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(k)).getComponent(a)).getComponents();
//								if(!(metametametaminiminiwincheck[(m/9)*9+k*3+a][(h/9)*9+j*3+b]==null)){
//									u++;}
//								if(metametametaminiminiwincheck[(m/9)*9+k*3+a][(h/9)*9+j*3+b]==null){
//									for(int c=0; c<3; c++){
//										for(int d=0; d<3; d++){
//											//for(int p=0; p<9; p++){
//											if(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).isVisible()){
//												if(!(newDossier.Board.keys.get(((m/9)*27+((m%9)/3)*9+((m%9)%3)*3+k)*81+(h/9)*27+((h%9)/3)*9+((h%9)%3)*3+j).getText().equals(""))){
//													//if(!(((JButton)((JPanel)((JPanel)newDossier.Board.metametametaboard[m/9][h/9].getComponent(k)).getComponent(a)).getComponent(p)).getText().equals("")))
//													t++;}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}

        Log.d("q", "" + q);
        Log.d("u", "" + u);

        return checking.equals("MetaOuter") && (q * 81 + u * 9 + t) == 729 || checking.equals("Outer") && (q * 9 + u) == 81 || checking.equals("Inner") && u == 9;

    }
	
	
	public boolean winChecker(int f, int g, int n, int actual, String[][] winchecker, String turnValue) {
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
				//                button.setEnabled(false);
				inAnimation = new AlphaAnimation(0f, 1f);
				inAnimation.setDuration(200);
				Board.progressBarHolder.setAnimation(inAnimation);
				Board.progressBarHolder.setVisibility(View.VISIBLE);
			}

			@Override
			protected String doInBackground(Boolean... params) {
				ParticipantResult currentPlayer;
				ParticipantResult pendingPlayer;
				String tieString = null;
				Log.d("tie", "" + params[0]);
				if(params[0]) {
					tieString = "Tie";
					Log.d("ST", "Setting Tie Results");
//					currentPlayer = new ParticipantResult(""+Board.currentPlayerId, ParticipantResult.MATCH_RESULT_TIE, ParticipantResult.PLACING_UNINITIALIZED);
//					pendingPlayer = new ParticipantResult(""+Board.pendingPlayerId, ParticipantResult.MATCH_RESULT_TIE, ParticipantResult.PLACING_UNINITIALIZED);
				} else {
					Log.d("SW", "Setting Win Results");
//					currentPlayer = new ParticipantResult(""+Board.currentPlayerId, ParticipantResult.MATCH_RESULT_WIN, ParticipantResult.PLACING_UNINITIALIZED);
//					pendingPlayer = new ParticipantResult(""+Board.pendingPlayerId, ParticipantResult.MATCH_RESULT_LOSS, ParticipantResult.PLACING_UNINITIALIZED);
				}
				List<ParticipantResult> results = new ArrayList<ParticipantResult>();
//				results.add(currentPlayer);
//				results.add(pendingPlayer);
				GoogleApiClient client = Index.client;
//				Games.TurnBasedMultiplayer.finishMatch(client, matchId, wincheckerToByteArray(Board.wincheck), results);
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

//	private void saveGame(String[][] winchecker, final String matchId, final String alternatePlayer) {
//		AsyncTask<String, Void, Boolean> updateTask = new AsyncTask<String, Void, Boolean>() {
//
//			@Override
//			protected void onPreExecute() {
//				super.onPreExecute();
//				int dimensions = boardSize(level);
//				for ( int i = 0; i < dimensions; i++ ) {
//					for ( int j = 0; j < dimensions; j++ ) {
//						int key = i * dimensions + j;
//						Button button = Board.keys.get(key);
////						Log.d("sCF", "Button: "+ key);
//						if (button != null) {
//							button.setClickable(false);
//						}
//					}
//				}
//				inAnimation = new AlphaAnimation(0f, 1f);
//				inAnimation.setDuration(200);
//				Board.progressBarHolder.setAnimation(inAnimation);
//				Board.progressBarHolder.setVisibility(View.VISIBLE);
//			}
//
//			@Override
//			protected Boolean doInBackground(String... params) {
//				GoogleApiClient client = Index.client;
////				if(Board.useIndex) {
////					client = Index.client;
////				} else if (Board.caller.equals("LevelMenu")){
////					client = LevelMenu.client;
////				} else if (Board.caller.equals("CurrentGames")) {
////					client = CurrentGames.client;
////				}
//				if(client.isConnected()){
//					Log.d("Params[0]", params[0]);
//                    Log.d("MatchID", matchId);
//                    Log.d("Params[1]", params[1]);
//                    Log.d("AlternatePlayer", alternatePlayer);
//					index.registerTurnBasedClient(client);
//					Games.TurnBasedMultiplayer.takeTurn(client, params[0], wincheckerToByteArray(wincheck), params[1]);
//					savedMultiplayer = true;
////					new Timer().schedule(new TimerTask() {
////						@Override
////						public void run() {
////							//board.finishActivity(context, false, null);
////					        //If you want to operate UI modifications, you must run ui stuff on UiThread.
//////					        board.runOnUiThread(new Runnable() {
//////					            @Override
//////					        	public void run () {
////////					            	Log.d("Caller", methodCaller);
////////					            	int dimensions = 0;
////////					            	switch (level) {
////////					            	case 1:
////////					            		dimensions = 3;
////////					            		break;
////////					            	case 2:
////////					            		dimensions = 9;
////////					            		break;
////////					            	}
////////					            	for ( int i = 0; i < dimensions; i++ ) {
////////					            		for ( int j = 0; j < dimensions; j++ ) {
////////					            			int key = i * dimensions + j;
////////											if(metawincheck[i/3][j/3] == null) {
////////												Button button = Board.keys.get(key);
////////												button.setClickable(false);
////////											}
////////					            		}
////////					            	}
////////					            	Board.keys.get(m * dimensions + f).setBackgroundColor(Color.rgb(0, 173, 173));
//////					    			savedMultiplayer = true;
//////					            }
//////					        });
////						}
////					}, 2000);
//					return true;
//				}
//				return false;
//			}
//
//			@Override
//			protected void onPostExecute(Boolean result) {
//				super.onPostExecute(result);
//				int dimensions = boardSize(level);
//				for ( int i = 0; i < dimensions; i++ ) {
//					for ( int j = 0; j < dimensions; j++ ) {
//						int key = i * dimensions + j;
//						Button button = Board.keys.get(key);
////						Log.d("sCF", "Button: "+ key);
//						if (button != null) {
//							button.setClickable(false);
//						}
//					}
//				}
//				outAnimation = new AlphaAnimation(1f, 0f);
//				outAnimation.setDuration(200);
//				Board.progressBarHolder.setAnimation(outAnimation);
//				Board.progressBarHolder.setVisibility(View.GONE);
////				board.finishActivity(context, false, null);
////				currentTurn = "";
//			}
//
//		};
//
//		StackTraceElement [] stackTraceElements = Thread.currentThread().getStackTrace();
//		methodCaller = stackTraceElements[4].toString();
//
//		updateTask.execute(matchId, alternatePlayer);
//
////		board.finishActivity(context, false, null);
////		Log.d("Caller", board.caller);
////		if((board.caller).contains("Index")) {
////			Log.d("Finishing", "Board");
////			board.finish();
////		}
////		board.finishActivity(context, false, null);
//		//		GameSaver save = new GameSaver();
//		//		save.FileCreator(winchecker, com.example.titanictictactoe.Board.player1, com.example.titanictictactoe.Board.player2, com.example.titanictictactoe.Board.n);
//	}
//
//
//
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
