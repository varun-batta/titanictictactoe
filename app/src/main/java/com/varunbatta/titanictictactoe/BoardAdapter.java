package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AbsListView.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class BoardAdapter extends BaseAdapter {
	private boolean savedGame = false;
	private Context context;
	private int level;
	private int actual;
	private int metaRow;
	private int metaColumn;
	private int height;
	private int width;
	public static int numberOfTimesPositionIsZero = 0;
	public static boolean positionIsEight = false;
	byte[] onGoingMatch;
	boolean myTurn;
	int key;
	public static ButtonPressed bp;
	
	public BoardAdapter(Context context, int level, int actual, int metaRow, int metaColumn, int height, int width,
			byte[] onGoingMatch, boolean myTurn, int key, boolean savedGame) {
		this.context = context;
		this.level = level;
		this.actual = actual;
		this.metaRow = metaRow;
		this.metaColumn = metaColumn;
		this.height = height;
		this.width = width;
		this.onGoingMatch = onGoingMatch;
		this.myTurn = myTurn;
		this.key = key;
		this.savedGame = savedGame;
//		Log.d("BA", "BoardAdapter");
		bp = new ButtonPressed(context, actual);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 9;
	}

	@Override
	public View getItem(int position) {
		return null;
//		switch(level) {
//		case 1:
//			return Board.keys.get(position);
//		case 2:
//			return Board.metakeys.get(position);
//		default:
//			return null;
//		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int row = -1;
		int column = -1;
//		View view = null;
		
		Log.d("gV", "getView Called - " + position);
		
		switch(position) {
		case 0:
			row = 0;
			column = 0;
//			if(positionIsEight) {
//				numberOfTimesPositionIsZero++;
//			}
			break;
		case 1:
			row = 0;
			column = 1;
			break;
		case 2:
			row = 0;
			column = 2;
			break;
		case 3:
			row = 1;
			column = 0;
			break;
		case 4:
			row = 1;
			column = 1;
			break;
		case 5:
			row = 1;
			column = 2;
			break;
		case 6:
			row = 2;
			column = 0;
			break;
		case 7:
			row = 2;
			column = 1;
			break;
		case 8:
			row = 2;
			column = 2;
			break;
		}
		
		switch(level) {
		case 1:
			int buttonHeight = height/3 - 10;
			int buttonWidth = width/3 - 5;
			
//			Log.d("Height", "" + height);
//			Log.d("ButtonHeight", "" + buttonHeight);
			
			LayoutParams buttonDimensions = new LayoutParams(buttonWidth, buttonHeight);
			
			Button button = new Button(context, null, R.style.button);
			button.setLayoutParams(buttonDimensions);
			button.setOnClickListener(bp);
			button.setTextColor(Color.BLACK);
			button.setGravity(Gravity.CENTER);
			int buttonBackgroundDrawableId = context.getResources().getIdentifier("board_btn", "drawable", context.getPackageName());
			button.setBackground(context.getResources().getDrawable(buttonBackgroundDrawableId));
			if( !myTurn || ( Board.wincheck[metaRow*3 + row][metaColumn*3 + column] != null && !Board.wincheck[metaRow*3 + row][metaColumn*3 + column].equals("") ) ) {
				button.setText(Board.wincheck[metaRow*3 + row][metaColumn*3 + column]);
				button.setEnabled(false);
			}
			int key = -1;
			if(actual == 2) {
				key = metaRow*27 + row*9 + metaColumn*3 + column;
			}
			if(actual == 1) {
				key = row*3 + column;
			}
			if( ( !myTurn || savedGame ) && key == this.key) {
//				Log.d("Key", "" + this.key);
//				Log.d("Row", "" + row);
//				Log.d("Column", "" + column);
				button.setBackgroundColor(Color.rgb(0, 173, 173));
			}
			button.setId(key);
			if(Board.keys.get(key) == null) {
				Log.d("nBID", "" + key);
				Board.keys.put(key, button);
				return button;
			} else {
				Log.d("reBID", "" + myTurn + " " + key);
				button = Board.keys.get(key);
				if(!myTurn) {
					button.setEnabled(false);
				}
				return button;
			}
//			Log.d("Inputting", " " + numberOfTimesPositionIsZero);
//			if(numberOfTimesPositionIsZero <= 23) {
//				Log.d("Input key", "" + key);
//				Board.keys.put(key, button);
//			}
//			return button;
		case 2:
			int boardWidth = width/3 - 15;
			int boardHeight = height/3 - 15;
			LayoutParams params = new LayoutParams(boardWidth, boardHeight);
			Log.d("boardHeight", "" + boardHeight);
			View board = null;
			if(ButtonPressed.metawincheck[row][column] == null) {
				board = new GridView(context);
				((GridView) board).setNumColumns(3);
				board.setLayoutParams(params);
				((GridView) board).setVerticalSpacing(5);
				((GridView) board).setAdapter(new BoardAdapter(context, 1, actual, row, column, boardHeight, boardWidth, onGoingMatch, myTurn, this.key, savedGame));
			} else if(ButtonPressed.metawincheck[row][column] != null) {
				board = new TextView(context);
				((TextView) board).setText(ButtonPressed.metawincheck[row][column]);
				((TextView) board).setTextSize(TypedValue.COMPLEX_UNIT_PX, boardHeight - 120);
				((TextView) board).setLayoutParams(params);
				((TextView) board).setGravity(Gravity.CENTER);
			}
//			if(!positionIsEight) {
//				positionIsEight = position == 8;
//			} else {
//				numberOfTimesPositionIsZero++;
//			}
//			Toast.makeText(context, "positionIsEight:" + positionIsEight, Toast.LENGTH_SHORT).show();
//			Toast.makeText(context, "numberOfTimesPositionIsZero:" + numberOfTimesPositionIsZero, Toast.LENGTH_SHORT).show();
//			Log.d("positionIsEight", "" + positionIsEight);
//			Log.d("numberOfTimesPositionIsZero", "" + numberOfTimesPositionIsZero);
//			if(numberOfTimesPositionIsZero == 2) {
//				if(onGoingMatch != null) {
//					ButtonPressed bp = new ButtonPressed(context, actual);
//					bp.boardChanger(Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][0]), Integer.parseInt(Board.wincheck[Board.wincheck.length - 1][1]), level);
//				}
//			}
			return board;
		default:
			Toast notAvailable = Toast.makeText(context, "Sorry, but this level is unavailable", Toast.LENGTH_SHORT);
			notAvailable.show();
			return null;
		}
	}
	
	public void replace () {
		Log.d("replace", "called");
		notifyDataSetChanged();
	}

}
