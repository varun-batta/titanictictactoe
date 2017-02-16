package com.varunbatta.titanictictactoe;

import com.varunbatta.titanictictactoe.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Instructions extends Activity {
	
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions);
		
		context = getApplicationContext();
		
		LinearLayout instructions = (LinearLayout) findViewById(R.id.instructionsLayout);
		instructions.setBackgroundColor(Color.rgb(0, 153, 153));
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		height /= 3;
		
		LayoutParams imageParams = new LayoutParams(width, height);
		
		TextView level1Title = new TextView(context);
		level1Title.setText("Level 1 - Normal Tic Tac Toe");
		level1Title.setTextSize(25);
		level1Title.setTextColor(Color.BLACK);
		instructions.addView(level1Title);
		
		ImageView level1Unfilled = new ImageView(context);
		level1Unfilled.setImageResource(R.drawable.level1unfilled);
		level1Unfilled.setLayoutParams(imageParams);
		instructions.addView(level1Unfilled);
		
		TextView level1Explanation = new TextView(context);
		level1Explanation.setText("This is basically like normal Tic Tac Toe, a simple 3x3 grid in which you alternatively play X"
				+ " or O until one of the players gets 3 of their symbol in a row, column, or diagonal, leading to victory for that player."
				+ " The opponent will logically try to block the player from attaining this goal while trying to attain it himself.");
		level1Explanation.setTextColor(Color.BLACK);
		instructions.addView(level1Explanation);
		
		ImageView level1Filled = new ImageView(context);
		level1Filled.setImageResource(R.drawable.level1filled);
		level1Filled.setLayoutParams(imageParams);
		instructions.addView(level1Filled);
		
		TextView level1Trial = new TextView(context);
		level1Trial.setText("Try it!");
		level1Trial.setTextColor(Color.BLACK);
		instructions.addView(level1Trial);
		
		Button level1TrialButton = new Button(context);
		level1TrialButton.setBackgroundColor(Color.LTGRAY);
		level1TrialButton.setText("Level 1 - Trial");
		level1TrialButton.setTextColor(Color.BLACK);
		level1TrialButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button trialChoice = (Button) v;
				startTrial(trialChoice.getText().toString());
			}
		});
		instructions.addView(level1TrialButton);
		
		TextView level2Title = new TextView(context);
		level2Title.setText("Level 2 - Next Level Tic Tac Toe");
		level2Title.setTextSize(25);
		level2Title.setTextColor(Color.BLACK);
		instructions.addView(level2Title);
		
		ImageView level2Unfilled = new ImageView(context);
		level2Unfilled.setImageResource(R.drawable.level2unfilled);
		level2Unfilled.setLayoutParams(imageParams);
		instructions.addView(level2Unfilled);
		
		TextView level2Explanation1 = new TextView(context);
		level2Explanation1.setText("Now it is time for Tic Tac Toe to the next level. In this level, the board is laid out like"
				+ " a giant Tic Tac Toe board, called a metaboard, with smaller Tic Tac Toe boards, called miniboards, in each"
				+ " section of the metaboard. Your purpose in this game is to win 3 of the sections of the metaboard in a row,"
				+ " column or diagonal. Hence, it is like Tic Tac Toe, but with a bit more depth in winning the whole game."
				+ " Now, unlike normal Tic Tac Toe, the entire metaboard does not remain active throughout the game. Instead,"
				+ " each move one makes decides in which section of the metaboard the opponent will play.");
		level2Explanation1.setTextColor(Color.BLACK);
		instructions.addView(level2Explanation1);
		
		ImageView level2MakingAMove = new ImageView(context);
		level2MakingAMove.setImageResource(R.drawable.level2makingamove);
		level2MakingAMove.setLayoutParams(imageParams);
		instructions.addView(level2MakingAMove);
		
		TextView level2Explanation2 = new TextView(context);
		level2Explanation2.setText("As you can see above, the player selected the center square in one of the miniboards. This results in"
				+ " the opponent playing in miniboard occupying the center section of the metaboard. This pattern is followed throughout the game"
				+ " with the various opponents deciding where in the board their opponents may play. Now, as the purpose is to win the individual"
				+ " miniboards to eventually win the metaboard you may like to see how this is done. The miniboards are won the same way as they are"
				+ " in regular Tic Tac Toe, via 3 in a row, column, or diagonal. To mark the victory, the miniboard is replaced with a giant representative"
				+ " of your symbol in the game, as can be seen below.");
		level2Explanation2.setTextColor(Color.BLACK);
		instructions.addView(level2Explanation2);
		
		ImageView level2PreMetaWin = new ImageView(context);
		level2PreMetaWin.setImageResource(R.drawable.level2premetawin);
		level2PreMetaWin.setLayoutParams(imageParams);
		instructions.addView(level2PreMetaWin);
		
		TextView level2Explanation3 = new TextView(context);
		level2Explanation3.setText("As you can see above, the player could win the center board if he/she is X. As they are, let's see what happens.");
		level2Explanation3.setTextColor(Color.BLACK);
		instructions.addView(level2Explanation3);
		
		ImageView level2PostMetaWin = new ImageView(context);
		level2PostMetaWin.setImageResource(R.drawable.level2postmetawin);
		level2PostMetaWin.setLayoutParams(imageParams);
		instructions.addView(level2PostMetaWin);
		
		TextView level2Explanation4 = new TextView(context);
		level2Explanation4.setText("Voila! The won board is replaced with a giant of the symbol, which in this case was X. However, the above picture"
				+ " also represents another important aspect of the game. As we could see, the winning play was to playin the center square. However,"
				+ " that would lead to the opponent playing in the center section of the metaboard, which we just won. Basically, when a player selects"
				+ " his/her opponent to play in a section of the metaboard that is already won, as the opponent cannot play there, the entire metaboard"
				+ " is made available to the opponent. He/she can play anywhere on the metaboard. Hence, when that is the only decision left to you, one"
				+ " one must carefully consider as to whether they wish to allow the opponent such power. This is also the pattern that will follow when"
				+ " one chooses a section of the metaboard that is completely full and ended up being a mini-tie.");
		level2Explanation4.setTextColor(Color.BLACK);
		instructions.addView(level2Explanation4);
		
		TextView level2Trial = new TextView(context);
		level2Trial.setText("\nBelow is the option to give level 2 a trial to better see how it works. Enjoy!");
		level2Trial.setTextColor(Color.BLACK);
		instructions.addView(level2Trial);
		
		Button level2TrialButton = new Button(context);
		level2TrialButton.setBackgroundColor(Color.LTGRAY);
		level2TrialButton.setText("Level 2 - Trial");
		level2TrialButton.setTextColor(Color.BLACK);
		level2TrialButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Button trialChoice = (Button) v;
				startTrial(trialChoice.getText().toString());
			}
			
		});
		instructions.addView(level2TrialButton);
		
		TextView level3Title = new TextView(context);
		level3Title.setText("Level 3 - Not Available Yet");
		level3Title.setTextSize(25);
		level3Title.setTextColor(Color.BLACK);
		instructions.addView(level3Title);
		
		TextView level4Title = new TextView(context);
		level4Title.setText("Level 4 - Not Available Yet");
		level4Title.setTextSize(25);
		level4Title.setTextColor(Color.BLACK);
		instructions.addView(level4Title);
		
//		TextView instructionsText = new TextView(getApplicationContext());
//		instructionsText.setText("Welcome! The game you will be partaking in is an enhanced version of Tic-Tac-Toe. " +
//								"It is important that you understand that the board for this game changes depending " +
//								"at what level the game is played. For example, Level 1 has a regular Tic-Tac-Toe board " +
//								"layout. Level 2, however, has Tic-Tac-Toe boards within each square of a larger " +
//								"Tic-Tac-Toe board. Level 3, as you can imagine just becomes more complicated and " +
//								"Level 4 even further complicated. Now, it is important to note that each square you " +
//								"play in will determine where the next person can go and may end up determining your own " +
//								"moves in the future. For example, on Level 2, each square pressed within a mini-Tic-Tac-Toe " +
//								"board chooses which board within the larger board one can then play in. Now, each move decides " +
//								"where the next person can play, and the board changes accordingly. Now, it is important to note " + 
//								"that when a level that has been won is picked, the entire board that it goes into is allowed to " + 
//								"be played on. Hope you enjoy the game!!!");
//		instructionsText.setTextColor(Color.BLACK);
//		instructions.addView(instructionsText);
	}
	
	private void startTrial(String trialChoiceLabel) {
		Intent trial = new Intent(context, Board.class);
		trial.putExtra("Multiplayer", false);
		trial.putExtra("Test", true);
		trial.putExtra("Player 1 Name", "Player 1");
		trial.putExtra("Player 2 Name", "Player 2");
		trial.putExtra("Caller", "Instructions");
		switch(trialChoiceLabel) {
		case "Level 1 - Trial":
			trial.putExtra("Level", 1);
			startActivity(trial);
			break;
		case "Level 2 - Trial":
			trial.putExtra("Level", 2);
			startActivity(trial);
			break;
		}
	}

}
