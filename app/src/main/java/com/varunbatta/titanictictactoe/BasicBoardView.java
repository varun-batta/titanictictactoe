package com.varunbatta.titanictictactoe;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static com.varunbatta.titanictictactoe.Board.game;

/**
 * BasicBoardView - The classic 3x3 Tic Tac Toe board that is used for miniboards as well as metaboards
 */
public class BasicBoardView extends RelativeLayout {
    public int metaRow = -1;
    public int metaColumn = -1;
    // TODO: Find a way to make it such that each instance of BasicBoardView does not require the board variable
    private Board board;
    public static String [][] winCheck = new String[10][9];
    public static String [][] metaWinCheck = new String[3][3];
    public static BasicBoardView [][] metaBoard = new BasicBoardView[3][3];

    ButtonPressed bp;

    public BasicBoardView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
    }

    public BasicBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
    }

    public BasicBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
    }

    public void configureBoard(float width, int level, int metaLevel, Board board) {
        bp = new ButtonPressed(getContext(), metaLevel, game, board);

        // Gaps is simply a float to make sure the cells are spaced apart correctly for both the miniboard and the metaboard
        float gaps = metaLevel == 2 ? 50 : 20;
        this.board = board;

        // This is the same board background but with red to replace the white when it shouldn't show
        ImageView boardBackgroundRed = findViewById(R.id.boardBackgroundRed);
        boardBackgroundRed.setImageAlpha(0);

        // Each column is a separate linear layout
        // TODO: See if recyclerview or some other view management system can handle this better
        LinearLayout verticalLeftColumn = findViewById(R.id.verticalLeft);
        LinearLayout verticalMiddleColumn = findViewById(R.id.verticalMiddle);
        LinearLayout verticalRightColumn = findViewById(R.id.verticalRight);

        // Initialize dimension to be used when setting the parameters of each BasicBoardView
        int dimension = (int)((width - gaps)/3.0);

        // TODO: Find a better way to separate the different levels
        if (level == 1) {
            for (int index = 0; index < 9; index++) {
                int row = index/3;
                int column = index%3;

                // Create a button and populate it with the existing winCheck values (after which check if the miniboard is won)
                // TODO: See if there's a cleaner way to handle all this
                Button button = new Button(getContext());
                button.setLayoutParams(new LayoutParams(dimension, dimension));
                button.setText(winCheck[metaRow*3 + row][metaColumn*3 + column]);
                if (!(winCheck[metaRow*3 + row][metaColumn*3 + column] == null)) {
                    button.setEnabled(false);
                    if (metaLevel >= 2) {
                        bp.winChecker(metaRow*3 + row, metaColumn*3 + column, metaLevel, metaLevel, winCheck, winCheck[metaRow*3 + row][metaColumn*3 + column]);
                    }
                }

                // Set various button visual attributes to make sure it still looks nice
                button.setTextColor(Color.BLACK);
                button.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                button.setBackgroundColor(Color.alpha(0));
                // Button bottom padding should be 10 if metaLevel is not 1, else 50 for the last row and 15 for the other two
                button.setPadding(10, 10, 10, metaLevel == 1 ? (row == 2 ? 50 : 15) : 10);
                // Button textSize should be bigger if metaLevel is 1
                button.setTextSize(metaLevel == 1 ? 75 : 15);
                // TODO: Find a way to handle this key button link better (not as complicated a calculation if possible)
                int key = metaLevel == 2 ? (metaRow*27 + row*9 + metaColumn*3 + column) : (row*3 + column);
                Board.keys.put(key, button);
                button.setId(key);

                button.setOnClickListener(bp);

                // TODO: Find a way to avoid this
                if (column == 0) {
                    verticalLeftColumn.addView(button);
                } else if (column == 1) {
                    verticalMiddleColumn.addView(button);
                } else if (column == 2) {
                    verticalRightColumn.addView(button);
                }
            }
        } else if (level == 2) {
            for (int index = 0; index < 9; index++) {
                int row = index/3;
                int column = index%3;

                BasicBoardView miniBoard = new BasicBoardView(getContext());
                miniBoard.metaRow = row;
                miniBoard.metaColumn = column;
                // TODO: Find a way to avoid having to pass the winChecks all over the place
                miniBoard.winCheck = winCheck;
                miniBoard.metaWinCheck = metaWinCheck;
                miniBoard.init(dimension, level);

                // TODO: Find a way for the board layout params to be better handled or stored in some global manner (avoid magic numbers)
                LayoutParams miniBoardLayoutParams = new LayoutParams(dimension, dimension);
                miniBoardLayoutParams.setMargins(5, 10, 0, 3);
                miniBoard.findViewById(R.id.overlaying_linear_layout).setLayoutParams(miniBoardLayoutParams);
                miniBoard.setLayoutParams(miniBoardLayoutParams);

                BasicBoardView.metaBoard[row][column] = miniBoard;

                miniBoard.configureBoard(dimension, 1, 2, board);

                // TODO: Find a way to avoid this
                if (column == 0) {
                    verticalLeftColumn.addView(miniBoard);
                } else if (column == 1) {
                    verticalMiddleColumn.addView(miniBoard);
                } else if (column == 2) {
                    verticalRightColumn.addView(miniBoard);
                }
            }
        }
    }

    public void init(int width, int level) {
        // TODO: Try to simplify the architecture of this view
        ImageView boardBackground = findViewById(R.id.boardBackground);
        ImageView boardBackgroundRed = findViewById(R.id.boardBackgroundRed);
        LinearLayout overlaying = findViewById(R.id.overlaying_linear_layout);
        // Each column is a separate linear layout
        // TODO: See if recyclerview or some other view management system can handle this better
        LinearLayout verticalLeft = findViewById(R.id.verticalLeft);
        LinearLayout verticalMiddle = findViewById(R.id.verticalMiddle);
        LinearLayout verticalRight = findViewById(R.id.verticalRight);

        // Setting width to height to make sure the image remains square
        // TODO: See if there's a way to avoid this
        boardBackground.getLayoutParams().height = width;
        boardBackgroundRed.getLayoutParams().height = width;

        if (level == 2) {
            // This is initialized to ensure padding is provided, always on the left only
            int leftPadding = metaColumn == 1 ? 20 : 5;

            // TODO: See if there's a nice way to handle all of this without having to use all the layout params
            LayoutParams overlayingLayoutParams = new LayoutParams(width - 40, width - 40);
            overlayingLayoutParams.setMargins(leftPadding, 20, 0, 0);
            overlaying.setLayoutParams(overlayingLayoutParams);

            LinearLayout.LayoutParams verticalLeftLayoutParams = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            verticalLeftLayoutParams.setMargins(leftPadding, 20, 4, 0);
            verticalLeft.setLayoutParams(verticalLeftLayoutParams);

            LinearLayout.LayoutParams verticalMiddleLayoutParams = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            verticalMiddleLayoutParams.setMargins(0, 20, 3, 0);
            verticalMiddle.setLayoutParams(verticalMiddleLayoutParams);

            LinearLayout.LayoutParams verticalRightLayoutParams = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            verticalRightLayoutParams.setMargins(0, 20, 0, 0);
            verticalRight.setLayoutParams(verticalRightLayoutParams);
        } else {
            // Setting width to height to make sure the image remains square (only a concern for level 1 due to the lack of left padding required)
            // TODO: See if there's a way to avoid this
            overlaying.getLayoutParams().height = width;
            verticalLeft.getLayoutParams().height = width;
            verticalMiddle.getLayoutParams().height = width;
            verticalRight.getLayoutParams().height = width;
        }
    }
}
