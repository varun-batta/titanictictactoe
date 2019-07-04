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
 * TODO: document your custom view class.
 */
public class BasicBoardView extends RelativeLayout {
    private int level;
    private int metaLevel;
    private float dimension;
    public int metaRow = -1;
    public int metaColumn = -1;
    public static String currentTurn = "X";
    private String turn;
    private Board board;
    public static String [][] wincheck = new String[10][9];
    public static String [][] metawincheck = new String[3][3];
    public static boolean firstTurn = true;
    public static BasicBoardView [][] metaBoard = new BasicBoardView[3][3];
    private boolean winOrTie = false;
    public static int lastMoveRow = -1;
    public static int lastMoveColumn = -1;

    ButtonPressed bp;

    public BasicBoardView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
//        init();
    }

    public BasicBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
//        init();
    }

    public BasicBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true);
//        init();
    }

    public void configureBoard(float width, int level, int metaLevel, Board board) {
        bp = new ButtonPressed(getContext(), metaLevel, game, board);

        this.level = level;
        this.metaLevel = metaLevel;
        float gaps = 20;
        if (metaLevel == 2) {
            gaps = 50;
        }
        this.board = board;

        ImageView boardBackgroundRed = (ImageView) findViewById(R.id.boardBackgroundRed);
        boardBackgroundRed.setImageAlpha(0);

        LinearLayout verticalLeftColumn = (LinearLayout) findViewById(R.id.verticalLeft);
        LinearLayout verticalMiddleColumn = (LinearLayout) findViewById(R.id.verticalMiddle);
        LinearLayout verticalRightColumn = (LinearLayout) findViewById(R.id.verticalRight);

        int dimension = (int)((width - gaps)/3.0);

        if (level == 1) {
            for (int index = 0; index < 9; index++) {
                int row = index/3;
                int column = index%3;
                Button button = new Button(getContext());
//                LayoutParams buttonLayoutParams = new LayoutParams(dimension, dimension);
//                buttonLayoutParams.setMargins(0, 0, 00, 2);
                button.setLayoutParams(new LayoutParams(dimension, dimension));
                button.setText(wincheck[metaRow*3 + row][metaColumn*3 + column]);
                if (!(wincheck[metaRow*3 + row][metaColumn*3 + column] == null)) {
                    button.setEnabled(false);
                    if (metaLevel >= 2) {
                        bp.winChecker(metaRow*3 + row, metaColumn*3 + column, metaLevel, metaLevel, wincheck, wincheck[metaRow*3 + row][metaColumn*3 + column]);
                    }
                }
                button.setTextColor(Color.BLACK);
                button.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                button.setBackgroundColor(Color.alpha(0));
                if (metaLevel == 1) {
                    int bottomPadding = 15;
                    if (row == 2) {
                        bottomPadding = 50;
                    }
                    button.setPadding(10, 10, 10, bottomPadding);
                } else {
                    button.setPadding(10, 10, 10, 10);
                }
                if (metaLevel == 1) {
                    button.setTextSize(75);
                } else {
                    button.setTextSize(15);
                }
                int key = -1;
                if (metaLevel == 2) {
                    key = metaRow*27 + row*9 + metaColumn*3 + column;
                } else if (metaLevel == 1) {
                    key = row*3 + column;
                }
                Board.keys.put(key, button);
                button.setId(key);

                button.setOnClickListener(bp);

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
                miniBoard.wincheck = wincheck;
                miniBoard.metawincheck = metawincheck;
                miniBoard.init(dimension, level);

                LayoutParams miniBoardLayoutParams = new LayoutParams(dimension, dimension);
                miniBoardLayoutParams.setMargins(5, 10, 0, 3);
                miniBoard.findViewById(R.id.overlaying_linear_layout).setLayoutParams(miniBoardLayoutParams);
                miniBoard.setLayoutParams(miniBoardLayoutParams);

                BasicBoardView.metaBoard[row][column] = miniBoard;

                miniBoard.configureBoard(dimension, 1, 2, board);

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
        ImageView boardBackground = (ImageView) findViewById(R.id.boardBackground);
        ImageView boardBackgroundRed = (ImageView) findViewById(R.id.boardBackgroundRed);
        LinearLayout overlaying = (LinearLayout) findViewById(R.id.overlaying_linear_layout);
        LinearLayout verticalLeft = (LinearLayout) findViewById(R.id.verticalLeft);
        LinearLayout verticalMiddle = (LinearLayout) findViewById(R.id.verticalMiddle);
        LinearLayout verticalRight = (LinearLayout) findViewById(R.id.verticalRight);

        boardBackground.getLayoutParams().height = width;
        boardBackgroundRed.getLayoutParams().height = width;

        if (level == 2) {
            int leftPadding = 5;
            if (metaColumn == 1) {
                leftPadding = 20;
            }

            LayoutParams oLP = new LayoutParams(width - 40, width - 40);
            oLP.setMargins(leftPadding, 20, 0, 0);
            overlaying.setLayoutParams(oLP);

            LinearLayout.LayoutParams vLLP = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            vLLP.setMargins(leftPadding, 20, 4, 0);
            verticalLeft.setLayoutParams(vLLP);

            LinearLayout.LayoutParams vMLP = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            vMLP.setMargins(0, 20, 3, 0);
            verticalMiddle.setLayoutParams(vMLP);

            LinearLayout.LayoutParams vRLP = new LinearLayout.LayoutParams((width - 40) / 3, width - 40);
            vRLP.setMargins(0, 20, 0, 0);
            verticalRight.setLayoutParams(vRLP);
        } else {
            overlaying.getLayoutParams().height = width;
            verticalLeft.getLayoutParams().height = width;
            verticalMiddle.getLayoutParams().height = width;
            verticalRight.getLayoutParams().height = width;
        }


//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 9; j++) {
//                wincheck[i][j] = "";
//            }
//        }
//
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                metawincheck[i][j] = "";
//            }
//        }
    }
}
