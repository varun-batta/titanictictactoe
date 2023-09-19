package com.varunbatta.titanictictactoe

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat

class BasicBoardView(private var context: Context, private var attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    var metaRow = -1
    var metaColumn = -1
    // TODO: Find a way to make it such that each instance of BasicBoardView does not require the board variable
    lateinit var board : Board
    lateinit var bp : ButtonPressed
    var winCheck: Array<Array<String>> = Array(10) { _ -> Array(9) { _ -> ""} }
    var metaWinCheck: Array<Array<String>> = Array(3) { _ -> Array(3) { _ -> ""} }

    companion object {
        var metaBoard: Array<Array<BasicBoardView?>> = Array(3) { _ -> Array(3) { _ -> null} }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true)
    }

    // TODO: See if we can get rid of these extra constructors
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : this(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.basic_board_view, this, true)
    }

    fun configureBoard(width: Int, level: Int, metaLevel: Int, board: Board) {
        bp = ButtonPressed(context, metaLevel, Board.game, board)

        // Gaps is simply a float to make sure the cells are spaced apart correctly for both the miniboard and the metaboard
        val gaps = if (metaLevel == 2) {
            50
        } else {
            20
        }
        this.board = board

        // This is the same board background but with red to replace the white when it shouldn't show
        val boardBackgroundRed = findViewById<ImageView>(R.id.boardBackgroundRed)
        boardBackgroundRed.imageAlpha = 0

        // Each column is a separate linear layout
        // TODO: See if recyclerview or some other view management system can handle this better
        val verticalLeftColumn = findViewById<LinearLayout>(R.id.verticalLeft)
        val verticalMiddleColumn = findViewById<LinearLayout>(R.id.verticalMiddle)
        val verticalRightColumn = findViewById<LinearLayout>(R.id.verticalRight)

        // Initialize dimension to be used when setting the parameters of each BasicBoardView
        val dimension = (width - gaps)/3

        // TODO: Find a better way to separate the different levels
        if (level == 1) {
            for (index in 0 until 9) {
                val row = index / 3
                val column = index % 3

                // Create a button and populate it with the existing winCheck values (after which check if the miniboard is won)
                // TODO: See if there's a cleaner way to handle all this
                val button = Button(context)
                button.layoutParams = LayoutParams(dimension, dimension)
                val text = winCheck[metaRow * 3 + row][metaColumn * 3 + column]
                button.text = text
                if (text != "") {
                    button.isEnabled = false
                    if (metaLevel >= 2) {
                        bp.winChecker(metaRow * 3 + row, metaColumn * 3 + column, metaLevel, metaLevel, winCheck, text)
                    }
                }

                // Set various button visual attributes to make sure it still looks nice
                button.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                button.textAlignment = TEXT_ALIGNMENT_CENTER
                button.setBackgroundColor(Color.alpha(0))
                // Button bottom padding should be 10 if metaLevel is not 1, else 50 for the last row and 15 for the other two
                val bottomPadding = if (metaLevel == 1) {
                    if (row == 2) {
                        50
                    } else {
                        15
                    }
                } else {
                     10
                }
                button.setPadding(10, 10, 10, bottomPadding)
                // Button textSize should be bigger if metaLevel is 1
                button.textSize = if (metaLevel == 1) {
                    75.toFloat()
                } else {
                    15.toFloat()
                }
                // TODO: Find a way to handle this key button link better (not as complicated a calculation if possible)
                val key = if (metaLevel == 2) {
                    metaRow * 27 + row * 9 + metaColumn * 3 + column
                } else {
                    row * 3 + column
                }
                Board.keys[key] = button
                button.id = key

                button.setOnClickListener(bp)

                // TODO: Find a way to avoid this
                when(column) {
                    0 -> verticalLeftColumn.addView(button)
                    1 -> verticalMiddleColumn.addView(button)
                    2 -> verticalRightColumn.addView(button)
                }
            }
        } else if (level == 2) {
            for (index in 0 until 9) {
                val row = index / 3
                val column = index % 3

                val miniBoard = BasicBoardView(context, attrs)
                miniBoard.metaRow = row
                miniBoard.metaColumn = column
                // TODO: Find a way to avoid having to pass the winChecks all over the place
                miniBoard.winCheck = winCheck
                miniBoard.metaWinCheck = metaWinCheck
                miniBoard.init(dimension, level)

                // TODO: Find a way for the board layout params to be better handled or stored in some global manner (avoid magic numbers)
                val miniBoardLayoutParams = LayoutParams(dimension, dimension)
                miniBoardLayoutParams.setMargins(5, 10, 0 ,3)
                miniBoard.findViewById<LinearLayout>(R.id.overlaying_linear_layout).layoutParams = miniBoardLayoutParams
                miniBoard.layoutParams = miniBoardLayoutParams

                metaBoard[row][column] = miniBoard

                miniBoard.configureBoard(dimension, 1, 2, board)

                // TODO: Find a way to avoid this
                when(column) {
                    0 -> verticalLeftColumn.addView(miniBoard)
                    1 -> verticalMiddleColumn.addView(miniBoard)
                    2 -> verticalRightColumn.addView(miniBoard)
                }
            }
        }
    }

    fun init(width: Int, level: Int) {
        // TODO: Try to simplify the architecture of this view
        val boardBackground = findViewById<ImageView>(R.id.boardBackground)
        val boardBackgroundRed = findViewById<ImageView>(R.id.boardBackgroundRed)
        val overlaying = findViewById<LinearLayout>(R.id.overlaying_linear_layout)

        // Each column is a separate linear layout
        // TODO: See if recyclerview or some other view management system can handle this better
        val verticalLeft = findViewById<LinearLayout>(R.id.verticalLeft)
        val verticalMiddle = findViewById<LinearLayout>(R.id.verticalMiddle)
        val verticalRight = findViewById<LinearLayout>(R.id.verticalRight)

        // Setting width to height to make sure the image remains square
        // TODO: See if there's a way to avoid this
        boardBackground.layoutParams.height = width
        boardBackgroundRed.layoutParams.height = width

        if (level == 2) {
            // This is initialized to ensure padding is provided, always on the left only
            val leftPadding = if (metaColumn == 1) {
                20
            } else {
                5
            }

            // TODO: See if there's a nice way to handle all of this without having to use all the layout params
            val overlayingLayoutParams = LayoutParams(width - 40, width - 40)
            overlayingLayoutParams.setMargins(leftPadding, 20, 0, 0)
            overlaying.layoutParams = overlayingLayoutParams

            val verticalLeftLayoutParams = LinearLayout.LayoutParams((width - 40) / 3, width - 40)
            verticalLeftLayoutParams.setMargins(leftPadding, 20, 4, 0)
            verticalLeft.layoutParams = verticalLeftLayoutParams

            val verticalMiddleLayoutParams = LinearLayout.LayoutParams((width - 40) / 3, width - 40)
            verticalMiddleLayoutParams.setMargins(0, 20, 3, 0)
            verticalMiddle.layoutParams = verticalMiddleLayoutParams

            val verticalRightLayoutParams = LinearLayout.LayoutParams((width - 40) / 3, width - 40)
            verticalRightLayoutParams.setMargins(0, 20, 0, 0)
            verticalRight.layoutParams = verticalRightLayoutParams
        } else {
            // Setting width to height to make sure the image remains square (only a concern for level 1 due to the lack of left padding required)
            // TODO: See if there's a way to avoid this
            overlaying.layoutParams.height = width
            verticalLeft.layoutParams.height = width
            verticalMiddle.layoutParams.height = width
            verticalRight.layoutParams.height = width
        }
    }
}