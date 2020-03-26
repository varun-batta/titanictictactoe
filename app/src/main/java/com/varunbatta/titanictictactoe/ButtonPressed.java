package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.GameRequestContent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ButtonPressed implements OnClickListener {
    // TODO: See if all these variables are required (or is there a better way to keep the global state)
	Context context;
    Game game;
	static int level;
	static String turn = "";
	static String winCheck[][] = new String [10][9];
	static String metaWinCheck[][] = new String [3][3];
	private int row = -1;
    private int column = -1;
	static String currentTurn = "";
    private TextView winningLetter;
	Board board;
	TextView playerTurn;

	private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

	ButtonPressed (Context context, int level, Game game, Board board) {
		this.context = context;
		ButtonPressed.level = level;
		this.game = game;
		this.board = board;
        this.playerTurn = board.findViewById(R.id.player_turn);
		winningLetter = new TextView(context);
	}
	
	private int boardSize(double level) {
		return (int) Math.pow(3, level);
	}

	@Override
	public void onClick(View v) {
		// Determine currentTurn based on lastMove
		// TODO: Clean this code up
		currentTurn = game.lastMove.equals("") ? "X" : game.lastMove.equals("X") ? "O" : "X";
		winCheck = game.data;
		Button pressedButton = (Button) v;
		String pressedButtonText = pressedButton.getText().toString();
		board = new Board();
		
		// TODO: Cleanup this code (just make the bottomPanelListener the buttonPressed action for these buttons)
		if ((pressedButtonText.equals("Menu") || pressedButtonText.equals("Save") ||
				pressedButtonText.equals("Main Menu") || pressedButtonText.equals("New Game") || pressedButtonText.equals("New WiFi Game")) ||
				pressedButtonText.equals("Back")) {
			board.bottomPanelListener(context, pressedButtonText);
			return;
		}

		// Depending on the value of currentTurn, set the associated global variable values
		// TODO: Make this cleaner, not based off of currentTurn
		// TODO: The playerTurn text should be set using string formatting instead
		if (currentTurn.equals("X")) {
			turn = "X";
			playerTurn.setText(game.player2.playerName + "'s Turn!");
			currentTurn = "O";
		} else {
			turn = "O";
			playerTurn.setText(game.player1.playerName + "'s Turn!");
			currentTurn = "X";
		}
		pressedButton.setText(turn);
		game.lastMove = turn;
		pressedButton.setEnabled(false);

		// TODO: Clean up the logic to find the row and column of the associated button (perhaps using the button ID?)
		// Handle level 1
		if (level == 1) {
			// Loop through all buttons to find the currently selected button
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Button selectedButton = Board.keys.get(i*3+j);
					if (selectedButton.getId() == pressedButton.getId()) {
						row = i;
						column = j;
					}
				}
			}
		}

		// Handle level 2
		boolean found = false;
		if (level == 2) {
			// Loop through all buttons to find the currently selected button
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					// Only need to check the buttons that are visible
					if (metaWinCheck[i / 3][j / 3] == null) {
						Button selectedButton = Board.keys.get(i * 9 + j);
						if (selectedButton.getId() == pressedButton.getId()) {
							row = i;
							column = j;
							found = true;
							break;
						}
					}
				}
				// Don't keep searching if found
				// TODO: See if there's an easier way to break from a double loop
				if (found) {
					break;
				}
			}
		}

		// TODO: See if this is necessary or if one shared data will make this better
		if (Board.multiplayer) {
			Board.winCheck[row][column] = turn;
		}
		winCheck[row][column] = turn;

		// Last row of winCheck contains the data required for game recreation
		winCheck[winCheck.length-1][0] = Integer.toString(row);
		winCheck[winCheck.length-1][1] = Integer.toString(column);
		winCheck[winCheck.length-1][2] = game.player1.playerName;
		winCheck[winCheck.length-1][3] = game.player2.playerName;
		winCheck[winCheck.length-1][4] = turn;
		winCheck[winCheck.length-1][5] = "" + level;

		// Change board as required
		boardChanger(row, column, Board.level, !Board.multiplayer);

		// See if there is a victory
		boolean winOrTie = winChecker(row, column, Board.level, Board.level, winCheck, "");

		// Handle multiplayer case to send game request via Facebook
		if (Board.multiplayer) {
			// Determine player information
			// TODO: See if this can be better done
			Player toPlayer;
			Player fromPlayer;
			if (turn.equals("X")) {
				toPlayer = game.player2;
				fromPlayer = game.player1;
			} else {
				toPlayer = game.player1;
				fromPlayer = game.player2;
			}

			// Determine message and title text
			String messageText;
			String titleText;
			//TODO: Consider first turn case
			if (winChecker(row, column, 1, Board.level, level == 2 ? metaWinCheck : winCheck, "")) {
				messageText = fromPlayer.playerName + " has won the game!";
				titleText = "Game Over";
				Board.winOrTie = winOrTie;
				Board.turn = turn;
			} else {
				messageText = fromPlayer.playerName + " has played and now it is your turn";
				titleText = "Your Turn";
			}

			// Turn game into passable data and make Facebook turn
			String game = winCheckerToString();
			Map<String, String> params = new HashMap<>();
			params.put("data", game);
			params.put("message", messageText);
			params.put("title", titleText);
			makeTurn(toPlayer.playerFBID, params);
		}
	}

	// TODO: Fix the names!!
	/*
	 * winCheckerToString converts winCheck to a string where each column is separated by a column and each row separated by a semicolon
	 */
	private String winCheckerToString() {
		StringBuilder game = new StringBuilder();
		// Populate the game string with all values from the winCheck array
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (winCheck[i][j] != null) {
                    game.append(winCheck[i][j]);
                }
                game.append(",");
			}
			game.append(";");
		}
		// Add a few more values for the last move made
		// TODO: Does this need to be done separately?
		game.append(winCheck[winCheck.length - 1][0]).append(",");
		game.append(winCheck[winCheck.length - 1][1]).append(",");
		game.append(winCheck[winCheck.length - 1][4]).append(",");
		game.append(level).append(",,,,,,;");
		return game.toString();
	}

	public void boardChanger(int rowIndex, int columnIndex, int level, boolean clickable) {
		// TODO: Refactor this function completely!!!!
		// Determine some base numbers for the boardSize and associated indices
		int miniBoardSize = boardSize(level) / 3;
		int metaRowIndex = rowIndex % miniBoardSize;
        int metaColumnIndex = columnIndex % miniBoardSize;

        if (level == 1) {
            // Handle level 1, which is literally nothing technically
        	for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int key = i * 3 + j;
                    Button button = Board.keys.get(key);
                    button.setClickable(clickable);
                }
            }
        } else if (level == 2) {
        	// Handle level 2
            if (metaWinCheck[metaRowIndex][metaColumnIndex] == null && !tieChecker("Inner", level, metaRowIndex, metaColumnIndex)) {
                // Handle it if this part of the view is accessible and doesn't unlock the entire board
            	// Loop through all the cells of the metaBoard and handle depending on their status
				for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (metaWinCheck[i][j] == null) {
                        	// If this section of the metaBoard has not been won (so should be usable)
                            // TODO: Clean this up!! Make the checks more straightforward rather than repeated nested checks
							if (level == 2) {
                                for (int k = 0; k < 9; k++) {
                                    for (int o = 0; o < 9; o++) {
                                        int key = k * 9 + o;
                                        if (metaWinCheck[k / 3][o / 3] == null) {
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
                                Button button = Board.keys.get((metaRowIndex % 3) * 27 + p * 9 + (metaColumnIndex % 3) * 3 + q);
                                if (button != null && button.getText().toString().equals("")) {
                                    button.setEnabled(true);
                                    ImageView selectedSection = BasicBoardView.metaBoard[metaRowIndex][metaColumnIndex].findViewById(R.id.boardBackgroundRed);
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
                        if (metaWinCheck[i / 3][j / 3] == null) {
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
	
	public boolean tieChecker(String checking, int level, int rowIndex, int columnIndex) {
        // TODO: Refactor this function as necessary
		int filledMetaCellCount = 0;
        int filledCellCount = 0;

        if (level == 1 && checking.equals("Inner")) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Button button = Board.keys.get(i * 3 + j);
                    if (button != null && !button.getText().toString().equals("")) {
                        filledCellCount++;
                    }
                }
            }
        }

        if (level == 2 && checking.equals("Inner") && metaWinCheck[rowIndex % 3][columnIndex % 3] == null) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    Button button = Board.keys.get((rowIndex % 3) * 27 + i * 9 + (columnIndex % 3) * 3 + j);
                    if (button != null && !button.getText().toString().equals("")) {
                        filledCellCount++;
                    }
                }
            }
        }

        if (level == 2 && checking.equals("Outer")) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (metaWinCheck[i][j] == null) {
                        for (int k = 0; k < 3; k++) {
                            for (int o = 0; o < 3; o++) {
                                Button button = Board.keys.get(i * 27 + k * 9 + j * 3 + o);
                                if (button != null && !button.getText().toString().equals("")) {
                                    filledCellCount++;
                                }
                            }
                        }
                    } else {
                        filledMetaCellCount++;
                    }
                }
            }
        }

        return checking.equals("Outer") && (filledMetaCellCount * 9 + filledCellCount) == 81 || checking.equals("Inner") && filledCellCount == 9;
    }

	public boolean winChecker(int rowIndex, int columnIndex, int testLevel, int actualLevel, String[][] winChecker, String turnValue) {
		// TODO: Refactor this function
		boolean winOrTie = false;
		String value = "";
		String value1 = "";
		String value2 = "";
		String x = "";

		if (testLevel == 1 && actualLevel >= 2) {
			rowIndex=rowIndex/3;
			columnIndex=columnIndex/3;
		}

		// Check the column
		// TODO: Clean up this approach
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

		// Seeing if all the cells in the column have the same value
		if(value != null && value1 != null && value2 != null && value.equals(value1) && value1.equals(value2)) {
			x = turnValue.equals("") ? turn : turnValue;
		}

		// Check the row
		// TODO: Clean up this approach
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

		// Seeing if all the cells in the column have the same value
		if (value != null && value1 != null && value2 != null && value.equals(value1) && value1.equals(value2)) {
			x = turnValue.equals("") ? turn : turnValue;
		}

		// Check the top-left to bottom-right diagonal
		// TODO: Clean up this approach
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

		// Seeing if all the cells in the diagonal have the same value
		if (value != null && value1 != null && value2 != null && value.equals(value1) && value1.equals(value2)) {
			x = turnValue.equals("") ? turn : turnValue;
		}

		// Check the top-right to bottom-left diagonal
		// TODO: Clean up this approach
		if (rowIndex%3 == 2 && columnIndex%3 == 0) {
			value = winChecker[rowIndex][columnIndex];
			value1 = winChecker[rowIndex-1][columnIndex+1];
			value2 = winChecker[rowIndex-2][columnIndex+2];
		} else if (rowIndex%3 == 1 && columnIndex%3 == 1) {
			value = winChecker[rowIndex+1][columnIndex-1];
			value1 = winChecker[rowIndex][columnIndex];
			value2 = winChecker[rowIndex-1][columnIndex+1];
		} else if (rowIndex%3 == 0 && columnIndex%3 == 2) {
			value = winChecker[rowIndex+2][columnIndex-2];
			value1 = winChecker[rowIndex+1][columnIndex-1];
			value2 = winChecker[rowIndex][columnIndex];
		}

		// Seeing if all the cells in the diagonal have the same value
		if (value != null && value1 != null && value2 != null && value.equals(value1) && value1.equals(value2)) {
			x = turnValue.equals("") ? turn : turnValue;
		}

		// Setting the winning player name and winning letter
		// TODO: Make this not based on the letter being played (if possible)
		String winningPlayerName = "";
		if (x.equals("X")) {
			winningPlayerName = game.player1.playerName;
			winningLetter.setText("X");
		}
		if (x.equals("O")) {
			winningPlayerName = game.player2.playerName;
			winningLetter.setText("O");
		}

		// Change board as necessary based off if a winner was found
		if (x.equals("O") || x.equals("X")) {
			switch(testLevel){
			case 1:
				if(!Board.multiplayer) {
					board.finishActivity(context, true, winningPlayerName);
				}
                Board.winOrTie = true;
				winOrTie =  true;
				break;
			case 2:
				if(actualLevel == 2){
					Log.d("Change", x + " " + actualLevel);
					board.winningBoardChanger(rowIndex, columnIndex, testLevel, actualLevel, x, context, winCheck);
				}
				break;
			}
		}

		// Check for ties as well
		String tieCheckerString = "";
		switch (Board.level) {
		case 1:
			tieCheckerString = "Inner";
			break;
		case 2:
			tieCheckerString = "Outer";
			break;
		}

		// Handle it in case a tie is found
		if (!winOrTie && (!x.equals("X") && !x.equals("O")) && tieChecker(tieCheckerString, testLevel, rowIndex, columnIndex)) {
			if(Board.multiplayer) {
				finishGame(true);
			} else {
				board.finishActivity(context, true, "Tie");
			}
			winOrTie = true;
		}
		
		return winOrTie;
	}

	public void finishGame(boolean tie) {
		// TODO: See if this AsyncTask is required, and if yes, perhaps create a class to handle these
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
				board.finishActivity(context, true, result);
				currentTurn = "";
			}

		};
		
		finishGameTask.execute(tie);
	}

	/*
	 * Makes the turn for a Facebook game, creating a game request from the provided parameters
	 */
	private void makeTurn(long to, Map<String, String> params) {
		GameRequestContent.Builder requestContent = new GameRequestContent.Builder();
		requestContent.setRecipients(Arrays.asList("" + to));
		requestContent.setMessage(params.get("message"));
		requestContent.setData(params.get("data"));
		requestContent.setTitle(params.get("title"));
		requestContent.setActionType(GameRequestContent.ActionType.TURN);

		Board.requestDialog.show(requestContent.build());
	}

	/*
	 * Creates a string first and then turns the game into a byte array
	 */
	// TODO: Clean up the naming!
	public static byte[] winCheckerToByteArray(String[][] winChecker) {
		StringBuilder gameString = new StringBuilder();
        for (String[] aWinchecker : winChecker) {
            for (int j = 0; j < winChecker[0].length; j++) {
                gameString.append(aWinchecker[j]).append(",");
            }
            gameString.append(";");
        }
		return gameString.toString().getBytes();
	}
}
