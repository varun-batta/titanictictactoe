package com.varunbatta.titanictictactoe

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

// TODO: See if all these variables are required (or is there a better way to keep the global state)
class ButtonPressed(
    private var context: Context,
    l: Int,
    private var game: Game,
    var board: Board,
) : OnClickListener {
    companion object {
        var level: Int = 1
        var turn = ""
        var currentTurn = ""
        var curInstructionsStep = 0
        var winCheck = Array(10) { _ -> Array(9) { _ -> ""} }
        var metaWinCheck = Array(3) { _ -> Array(3) { _ -> ""} }
    }
    private var row = -1
    private var column = -1
    private var winningLetter : TextView
    private var playerTurn : TextView

    init {
        level = l
        this.playerTurn = board.findViewById(R.id.player_turn)
        winningLetter = TextView(context)
    }

    private fun boardSize(level: Int): Int {
        return 3.0.pow(level.toDouble()).toInt()
    }

    override fun onClick(pressedButton: View?) {
        // Determine currentTurn based on lastMove
        // TODO: Clean this code up
        currentTurn = when(game.lastMove) {
            "" -> "X"
            "X" -> "O"
            else -> "X"
        }
        winCheck = game.data
        board = Board()

        // Depending on the value of currentTurn, set the associated global variable values
        // TODO: Make this cleaner, not based off of currentTurn
        val playersTurn : String = context.getString(R.string.playersTurn)
        if (currentTurn == "X") {
            turn = "X"
            playerTurn.text = playersTurn.format(game.player2.playerName)
            currentTurn = "O"
        } else {
            turn = "O"
            playerTurn.text = playersTurn.format(game.player1.playerName)
            currentTurn = "X"
        }
        (pressedButton as Button).text = turn
        game.lastMove = turn
        pressedButton.isEnabled = false

        // TODO: Clean up the logic to find the row and column of the associated button (perhaps using the button ID?)
        // Handle level 1
        if (level == 1) {
            // Loop through all the buttons to find the currently selected button
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val selectedButton = Board.keys[i*3+j]
                    if (selectedButton?.id == pressedButton.id) {
                        row = i
                        column = j
                    }
                }
            }
        }

        // Handle level 2
        var isFound = false
        if (level == 2) {
            // Loop through all buttons to find the currently selected button
            for (i in 0 until 9) {
                for (j in 0 until 9) {
                    // Only need to check the buttons that are visible
                    if (metaWinCheck[i / 3][j / 3] == "") {
                        val selectedButton = Board.keys[i * 9 + j]
                        if (selectedButton?.id == pressedButton.id) {
                            row = i
                            column = j
                            isFound = true
                            break
                        }
                    }
                }
                // Don't keep searching if found
                // TODO: See if there's an easier way to break from a double loop
                if (isFound) {
                    break
                }
            }
        }


        // TODO: See if this is necessary or if one shared data will make this better
        winCheck[row][column] = turn

        // Last row of winCheck contains the data required for game recreation
        winCheck[winCheck.size - 1][0] = row.toString()
        winCheck[winCheck.size - 1][1] = column.toString()
        winCheck[winCheck.size - 1][2] = game.player1.playerName
        winCheck[winCheck.size - 1][3] = game.player2.playerName
        winCheck[winCheck.size - 1][4] = turn
        winCheck[winCheck.size - 1][5] = level.toString()

        // See if there is a victory
        val winningStatus = winChecker(row, column, Board.level, Board.level, winCheck, "")

        // Change board as required
        if (!winningStatus.isBoardChangerCalled) {
            boardChanger(row, column, Board.level, true)
        }

        // TODO: See if this code can be cleaned up a bit
        if (Board.isInstructionalGame) {
            if (currentTurn == "X") {
                // Your turn in instructional game
                if (level == 1) {
                    if (curInstructionsStep == 3) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(R.string.level1Instructions3)
                            .setNegativeButton("Next") { _, _ ->
                                showRows()
                            }
                            .show()
                        curInstructionsStep++
                    }
                    if (curInstructionsStep == 4) {
                        // Check if potential win is there for the opponent
                        val potentialWin = checkForPotentialWin("O", row, column)
                        if (potentialWin.isPotentialWin) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                            builder
                                .setMessage(R.string.level1Instructions8)
                                .setNegativeButton("Next") { _, _ -> }
                                .show()
                            curInstructionsStep++
                        }
                    }
                }
                if (level == 2) {
                    if (curInstructionsStep == 3) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(R.string.level2Instructions3)
                            .setNegativeButton("OK") { _, _ -> }
                            .show()
                        curInstructionsStep++
                    }
                    if (curInstructionsStep == 4) {
                        // Check if potential win is there for you
                        val potentialWin = checkForPotentialWin("X", row, column)
                        if (potentialWin.isPotentialWin) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                            builder
                                .setMessage(R.string.level2Instructions4You)
                                .setNegativeButton("OK") { _, _ -> showButtonToClick(potentialWin.rowIndex, potentialWin.colIndex)}
                                .show()
                        }
                    }
                    if (curInstructionsStep == 5) {
                        val metaBoardSelectionName = getMetaBoardSelectionName("O")
                        val alertText = "${context.getString(R.string.level2Instructions6Opponent, metaBoardSelectionName.selectionName)} ${context.getString(R.string.level2Instructions6, "O")}"
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(alertText)
                            .setNegativeButton("OK") { _, _ -> showWarning(metaBoardSelectionName.rowIndex, metaBoardSelectionName.colIndex, row%3, column%3)}
                            .show()
                    }
                }
            }
            if (currentTurn == "O") {
                // AI's turn in instructional game
                if (level == 1) {
                    if (curInstructionsStep == 2) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(R.string.level1Instructions2)
                            .setNegativeButton("Next") { _, _ ->
                                makeAIMove()
                            }
                            .show()
                        curInstructionsStep++
                    } else if (curInstructionsStep == 4) {
                        // Check if potential win is there for you
                        val potentialWin = checkForPotentialWin("X", row, column)
                        if (potentialWin.isPotentialWin) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                            builder
                                .setMessage(R.string.level1Instructions8)
                                .setNegativeButton("Next") { _, _ ->
                                    onClick(Board.keys[potentialWin.rowIndex*3 + potentialWin.colIndex])
                                }
                                .show()
                            curInstructionsStep++
                        } else {
                            makeAIMove()
                        }
                    } else if (!winningStatus.winOrTie) {
                        makeAIMove()
                    }
                }
                if (level == 2) {
                    if (curInstructionsStep == 2) {
                        val selectionName = getSelectionName(row%3, column%3)
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(context.getString(R.string.level2Instructions2, selectionName, selectionName))
                            .setNegativeButton("OK") { _, _ -> makeAIMove()}
                            .show()
                        curInstructionsStep++
                    } else if (curInstructionsStep == 4) {
                        // Check if potential win is there for the opponent
                        val potentialWin = checkForPotentialWin("O", row, column)
                        if (potentialWin.isPotentialWin) {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                            builder
                                .setMessage(R.string.level2Instructions4Opponent)
                                .setNegativeButton("OK") { _, _ -> showButtonToClick(potentialWin.rowIndex, potentialWin.colIndex)}
                                .show()
                        } else {
                            makeAIMove()
                        }
                    } else if (curInstructionsStep == 5) {
                        val metaBoardSelectionName = getMetaBoardSelectionName("X")
                        val alertText = "${context.getString(R.string.level2Instructions6You, metaBoardSelectionName.selectionName)} ${context.getString(R.string.level2Instructions6, "X")}"
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage(alertText)
                            .setNegativeButton("OK") { _, _ -> showWarning(metaBoardSelectionName.rowIndex, metaBoardSelectionName.colIndex, row%3, column%3)}
                            .show()
                    } else {
                        makeAIMove()
                    }
                }
            }
        }
    }

    // Instructional Helpers
    // TODO: See if we can clean up all these helper functions
    private fun makeAIMove() {
        board.populateEnabledKeys(-1, -1)
        val chosenKey = Board.enabledKeys[Random.nextInt(0, Board.enabledKeys.size)]
        onClick(Board.keys[chosenKey])
    }
    private fun showRows() {
        // Find the rowIndex of the last selection
        var rowIndex: Int = -1
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (winCheck[i][j] == "X") {
                    rowIndex = i
                    break
                }
            }
            if (rowIndex != -1) {
                break
            }
        }
        // Highlight that row of buttons
        for (i in 0 until 3) {
            val selectedButton = Board.keys[rowIndex*3+i]
            selectedButton?.setBackgroundColor(Color.YELLOW)
        }
        // Present the dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level1Instructions4)
            .setNegativeButton("Next") { _, _ -> showColumns()}
            .show()
    }

    private fun showColumns() {
        // Find the colIndex of the last selection
        var colIndex: Int = -1
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (winCheck[i][j] == "X") {
                    colIndex = j
                    break
                }
            }
            if (colIndex != -1) {
                break
            }
        }
        // Reset all the buttons to their original state
        for (i in 0 until 3){
            for (j in 0 until 3 ) {
                val button = Board.keys[i*3 + j]
                button?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        // Highlight that row of buttons
        for (i in 0 until 3) {
            val selectedButton = Board.keys[i*3+colIndex]
            selectedButton?.setBackgroundColor(Color.YELLOW)
        }
        // Present the dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level1Instructions5)
            .setNegativeButton("Next") { _, _ -> showDiagonals()}
            .show()
    }

    private fun showDiagonals() {
        // Find the rowIndex and colIndex of the last selection
        var rowIndex: Int = -1
        var colIndex: Int = -1
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (winCheck[i][j] == "X") {
                    rowIndex = i
                    colIndex = j
                    break
                }
            }
            if (rowIndex != -1) {
                break
            }
        }
        // Reset all the buttons to their original state
        for (i in 0 until 3){
            for (j in 0 until 3 ) {
                val button = Board.keys[i*3 + j]
                button?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        // Determine if the selection is one of the 2 diagonals and highlight accordingly
        if (abs(rowIndex - colIndex) == 2) {
            // Top Right to Bottom Left Diagonal
            var c = 2
            for (r in 0 until 3) {
                val button = Board.keys[r*3 + c]
                button?.setBackgroundColor(Color.YELLOW)
                c--
            }
        } else {
            // Top Left to Bottom Right Diagonal
            for (i in 0 until 3) {
                val button = Board.keys[i*3 + i]
                button?.setBackgroundColor(Color.YELLOW)
            }
        }
        // Present the dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level1Instructions6)
            .setNegativeButton("Got it") { _, _ -> finishGuide()}
            .show()
    }

    private fun finishGuide() {
        // Reset all the buttons to their original state
        for (i in 0 until 3){
            for (j in 0 until 3 ) {
                val button = Board.keys[i*3 + j]
                button?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        // Present the dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level1Instructions7)
            .setNegativeButton("OK") { _, _ -> }
            .show()
    }

    data class PotentialWin(val isPotentialWin: Boolean, val rowIndex: Int, val colIndex: Int)
    private fun checkForPotentialWin(playerTurn: String, row: Int, column: Int): PotentialWin {
        if (level == 1) {
            // Check row option
            var found = -1
            var empty = -1
            for (i in 0 until 3) {
                if (winCheck[row][i] == playerTurn && i != column) {
                    found = i
                }
                if (winCheck[row][i] == "") {
                    empty = i
                }
            }
            if (found != -1 && empty != -1) {
                return PotentialWin(true, row, empty)
            }
            // Check column option
            found = -1
            empty = -1
            for (i in 0 until 3) {
                if (winCheck[i][column] == playerTurn && i != row) {
                    found = i
                }
                if (winCheck[i][column] == "") {
                    empty = i
                }
            }
            if (found != -1 && empty != -1) {
                return PotentialWin(true, empty, column)
            }
            // Check top left to bottom right diagonal
            if (row == column) {
                found = -1
                empty = -1
                for (i in 0 until 3) {
                    if (winCheck[i][i] == playerTurn && i != row) {
                        found = i
                    }
                    if (winCheck[i][i] == "") {
                        empty = i
                    }
                }
                if (found != -1 && empty != -1) {
                    return PotentialWin(true, empty, empty)
                }
            }
            // Check top right to bottom left diagonal
            if (isInTopRightToBottomLeftDiagonal(row, column)) {
                var foundRow = -1
                var foundCol = -1
                var emptyRow = -1
                var emptyCol = -1
                var c = 2
                for (r in 0 until 3) {
                    if (winCheck[r][c] == playerTurn && r != row && c != column) {
                        foundRow = r
                        foundCol = c
                    }
                    if (winCheck[r][c] == "") {
                        emptyRow = r
                        emptyCol = c
                    }
                    c--
                }
                if (foundRow != -1 && foundCol != -1 && emptyRow != -1 && emptyCol != -1) {
                    return PotentialWin(true, emptyRow, emptyCol)
                }
            }
        }
        if (level == 2) {
            val metaRow = row % 3
            val metaColumn = column % 3
            // Check row option
            for (r in metaRow*3+0 until metaRow*3+3) {
                val foundIndices = mutableListOf<Int>()
                val emptyIndices = mutableListOf<Int>()
                for (c in metaColumn*3+0 until metaColumn*3+3) {
                    if (winCheck[r][c] == playerTurn) {
                        foundIndices.add(c)
                    }
                    if (winCheck[r][c] == "") {
                        emptyIndices.add(c)
                    }
                }
                if (foundIndices.size == 2 && emptyIndices.size == 1) {
                    return PotentialWin(true, r, emptyIndices[0])
                }
            }
            // Check column option
            for (c in metaColumn*3+0 until metaColumn*3+3) {
                val foundIndices = mutableListOf<Int>()
                val emptyIndices = mutableListOf<Int>()
                for (r in metaRow*3+0 until metaRow*3+3) {
                    if (winCheck[r][c] == playerTurn) {
                        foundIndices.add(r)
                    }
                    if (winCheck[r][c] == "") {
                        emptyIndices.add(r)
                    }
                }
                if (foundIndices.size == 2 && emptyIndices.size == 1) {
                    return PotentialWin(true, emptyIndices[0], c)
                }
            }
            // Check top left to bottom right diagonal
            val foundRows = mutableListOf<Int>()
            val foundColumns = mutableListOf<Int>()
            val emptyRows = mutableListOf<Int>()
            val emptyColumns = mutableListOf<Int>()
            var c = metaColumn*3
            for (r in metaRow*3+0 until metaRow*3+3) {
                if (winCheck[r][c] == playerTurn) {
                    foundRows.add(r)
                    foundColumns.add(c)
                }
                if (winCheck[r][c] == "") {
                    emptyRows.add(r)
                    emptyColumns.add(c)
                }
                c++
            }
            if (foundRows.size == 2 && foundColumns.size == 2 && emptyRows.size == 1 && emptyColumns.size == 1) {
                return PotentialWin(true, emptyRows[0], emptyColumns[0])
            }
            // Check top right to bottom left diagonal
            foundRows.clear()
            foundColumns.clear()
            emptyRows.clear()
            emptyColumns.clear()
            c = metaColumn*3+2
            for (r in metaRow*3+0 until metaRow*3+3) {
                if (winCheck[r][c] == playerTurn) {
                    foundRows.add(r)
                    foundColumns.add(c)
                }
                if (winCheck[r][c] == "") {
                    emptyRows.add(r)
                    emptyColumns.add(c)
                }
                c--
            }
            if (foundRows.size == 2 && foundColumns.size == 2 && emptyRows.size == 1 && emptyColumns.size == 1) {
                return PotentialWin(true, emptyRows[0], emptyColumns[0])
            }
        }
        return PotentialWin(false, -1, -1)
    }

    private fun isInTopRightToBottomLeftDiagonal(row: Int, column: Int): Boolean {
        val isTopRight = row == 0 && column == 2
        val isCenter = row == 1 && column == 1
        val isBottomLeft = row == 2 && column == 0
        return isTopRight || isCenter || isBottomLeft
    }

    private fun getSelectionName(row: Int, column: Int): String {
        var selectionName = ""
        when(row) {
            0 -> selectionName += "Top"
            1 -> selectionName += "Middle"
            2 -> selectionName += "Bottom"
        }
        when(column) {
            0 -> selectionName += " Left"
            1 -> if (row == 1) {
                selectionName = "Center"
            } else {
                selectionName += " Middle"
            }
            2 -> selectionName += " Right"
        }
        return selectionName
    }

    private fun showButtonToClick(row: Int, column: Int) {
        // Highlight button to click
        val button = Board.keys[row*9 + column]
        button?.setBackgroundColor(Color.YELLOW)

        // Disable all other buttons
        val metaRow = row / 3
        val metaColumn = column / 3
        for (r in metaRow*3 until metaRow*3+3) {
            for (c in metaColumn*3 until metaColumn*3+3) {
                val b = Board.keys[r*9+c]
                if (r != row && c != column) {
                    b?.isEnabled = false
                }
            }
        }

        var alertMessage = context.getString(R.string.level2Instructions5You)
        if (currentTurn == "O") {
            alertMessage = context.getString(R.string.level2Instructions5Opponent)
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(alertMessage)
            .setNegativeButton("OK") { _, _ ->
                curInstructionsStep++
                if (currentTurn == "O") {
                    onClick(button)
                }
            }
            .show()
    }

    data class MetaBoardSelectionName(val selectionName: String, val rowIndex: Int, val colIndex: Int)
    private fun getMetaBoardSelectionName(turn: String): MetaBoardSelectionName {
        for (r in 0 until 3) {
            for (c in 0 until 3 ) {
                if (metaWinCheck[r][c] == turn) {
                    return MetaBoardSelectionName(getSelectionName(r, c), r, c)
                }
            }
        }
        return MetaBoardSelectionName("", -1, -1)
    }

    private fun showWarning(metaRow: Int, metaColumn: Int, selectedMetaRow: Int, selectedMetaColumn: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level2Instructions7Part1)
            .setNegativeButton("Next") { _, _ -> showMetaBoardSelection(metaRow, metaColumn, selectedMetaRow, selectedMetaColumn)}
            .show()
    }

    private fun showMetaBoardSelection(metaRow: Int, metaColumn: Int, selectedMetaRow: Int, selectedMetaColumn: Int) {
        // Highlight all buttons that correspond to warning
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val button = Board.keys[(r*3 + metaRow)*9 + c*3+metaColumn]
                button?.setBackgroundColor(Color.YELLOW)
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level2Instructions7Part2)
            .setNegativeButton("OK") { _, _ -> demoMetaBoardSelection(selectedMetaRow, selectedMetaColumn)}
            .show()
    }

    private fun demoMetaBoardSelection(selectedMetaRow: Int, selectedMetaColumn: Int) {
        // Disable all buttons for now and return them back to not highlighted
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val button = Board.keys[r*9 + c]
                button?.isEnabled = false
                button?.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        // Fake the clicking
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (metaWinCheck[i / 3][j / 3] == "") {
                    val button = Board.keys[i*9 + j]
                    if (button != null && button.text == "") {
                        button.isEnabled = true
                        val section = BasicBoardView.metaBoard[i / 3][j / 3]?.findViewById<ImageView>(R.id.boardBackgroundRed)
                        section?.imageAlpha = 0
                    }
                }
            }
        }

        // Show the prompt
        val endingText = context.getString(R.string.level2Instructions7Part3)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("${context.getString(R.string.level2Instructions7Part2)} $endingText")
            .setNegativeButton("OK") { _, _ ->
                resetBoard(selectedMetaRow, selectedMetaColumn)
            }
            .show()
    }

    private fun resetBoard(selectedMetaRow: Int, selectedMetaColumn: Int) {
        if (metaWinCheck[selectedMetaRow][selectedMetaColumn] == "") {
            for (i in 0 until 9) {
                for (j in 0 until 9) {
                    if (metaWinCheck[i / 3][j / 3] == "") {
                        val button = Board.keys[i * 9 + j]
                        var isEnabled = false
                        var alpha = 150
                        if (i / 3 == selectedMetaRow && j / 3 == selectedMetaColumn) {
                            isEnabled = true
                            alpha = 0
                        }
                        if (button != null && button.text == "") {
                            button.isEnabled = isEnabled
                            val section =
                                BasicBoardView.metaBoard[i / 3][j / 3]?.findViewById<ImageView>(R.id.boardBackgroundRed)
                            section?.imageAlpha = alpha
                        }
                    }
                }
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(R.string.level2Instructions8)
            .setNegativeButton("OK") { _, _ ->
                curInstructionsStep++
                if (currentTurn == "O") {
                    makeAIMove()
                }
            }
            .show()
    }

    private fun changeToNextLevelInstructions() {
        board.finishActivity(context, false, "")
        val boardIntent = Intent(context, Board::class.java)
        boardIntent.putExtra("isInstructionalGame", true)
        boardIntent.putExtra("Player 1 Name", "Your")
        boardIntent.putExtra("Player 2 Name", "AI")
        boardIntent.putExtra("Level", 2)
        context.startActivity(boardIntent)
    }

    private fun showClosingAlertDialog(level: Int, winningCharacter: String) {
        var message = when(winningCharacter) {
            "X" -> context.getString(R.string.instructionsYouWon)
            "O" -> context.getString(R.string.instructionsOpponentWon)
            else -> context.getString(R.string.instructionsTied)
        }
        if (level == 1) {
            message += " ${context.getString(R.string.level1Instructions9)}"
        }
        if (level == 2) {
            message += " ${context.getString(R.string.level2Instructions9)}"
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(message)
        if (level == 1) {
            builder
                .setNegativeButton("Not now") { _, _ ->
                    board.finishActivity(context, false, "")
                }
                .setPositiveButton("Next") { _, _ ->
                    changeToNextLevelInstructions()
                }
        }
        if (level == 2) {
            builder
                .setNegativeButton("Done") { _, _ ->
                    board.finishActivity(context, false, "")                                }
        }
        builder.show()
    }

    // Game Helpers
    fun boardChanger(rowIndex: Int, columnIndex: Int, level: Int, isClickable: Boolean) {
        // TODO: Refactor this function completely!!!!
        // Determine some base numbers for the boardSize and associated indices
        val miniBoardSize = boardSize(level) / 3
        val metaRowIndex = rowIndex % miniBoardSize
        val metaColumnIndex = columnIndex % miniBoardSize

        if (level == 1) {
            // Handle level 1, which is literally nothing technically
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val key = i * 3 + j
                    val button = Board.keys[key]
                    button?.isClickable = isClickable
                }
            }
        } else if (level == 2) {
            // Handle level 2
            if (metaWinCheck[metaRowIndex][metaColumnIndex] == "" && !tieChecker("Inner", level, metaRowIndex, metaColumnIndex)) {
                // Handle if this part of the view is accessible and doesn't unlock the entire board
                // Loop through all the cells of the metaBoard and handle depending on their status
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        if (metaWinCheck[i][j] == "") {
                            // If this section of the metaBoard has not been won (so should be usable)
                            // TODO: Clean this up!! Make the checks more straightforward rather than repeated nested checks
                            for (k in 0 until 9) {
                                for (l in 0 until 9) {
                                    val key = k * 9 + l
                                    if (metaWinCheck[k / 3][l / 3] == "") {
                                        val button = Board.keys[key]
                                        button?.isEnabled = false
                                        val section = BasicBoardView.metaBoard[k / 3][l / 3]?.findViewById<ImageView>(R.id.boardBackgroundRed)
                                        section?.imageAlpha = 150
                                    }
                                }
                            }
                        }
                        for (m in 0 until 3) {
                            for (n in 0 until 3) {
                                val button = Board.keys[(metaRowIndex % 3) * 27 + m * 9 + (metaColumnIndex % 3) * 3 + n]
                                if (button != null && button.text == "") {
                                    button.isEnabled = true
                                    val section = BasicBoardView.metaBoard[metaRowIndex][metaColumnIndex]?.findViewById<ImageView>(R.id.boardBackgroundRed)
                                    section?.imageAlpha = 0
                                }
                            }
                        }
                    }
                }
            } else {
                for (i in 0 until 9) {
                    for (j in 0 until 9) {
                        if (metaWinCheck[i / 3][j / 3] == "") {
                            val button = Board.keys[i*9 + j]
                            if (button != null && button.text == "") {
                                button.isEnabled = true
                                button.isClickable = isClickable
                                val section = BasicBoardView.metaBoard[i / 3][j / 3]?.findViewById<ImageView>(R.id.boardBackgroundRed)
                                section?.imageAlpha = 0
                            }
                        }
                    }
                }
            }
        }
    }

    fun tieChecker(boardLevel: String, level: Int, rowIndex: Int, columnIndex: Int) : Boolean {
        // TODO: Refactor this function as necessary
        var filledMetaCellCount = 0
        var filledCellCount = 0

        if (level == 1 && boardLevel == "Inner") {
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val button = Board.keys[i * 3 + j]
                    if (button != null && button.text != "") {
                        filledCellCount++
                    }
                }
            }
        }

        if (level == 2 && boardLevel == "Inner" && metaWinCheck[rowIndex % 3][columnIndex % 3] == "") {
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val button = Board.keys[(rowIndex % 3) * 27 + i * 9 + (columnIndex % 3) * 3 + j]
                    if (button != null && button.text != "") {
                        filledCellCount++
                    }
                }
            }
        }

        if (level == 2 && boardLevel == "Outer") {
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (metaWinCheck[i][j] == "") {
                        for (k in 0 until 3) {
                            for (l in 0 until 3) {
                                val button = Board.keys[i*27 + k*9 + j *3 + l]
                                if (button != null && button.text != "") {
                                    filledCellCount++
                                }
                            }
                        }
                    } else {
                        filledMetaCellCount++
                    }
                }
            }
        }

        val isInnerTied = filledCellCount == 9
        val isOuterTied = filledMetaCellCount * 9 + filledCellCount == 81

        return boardLevel == "Outer" && isOuterTied || boardLevel == "Inner" && isInnerTied
    }

    data class WinningStatus(val winOrTie: Boolean, val isBoardChangerCalled: Boolean)
    fun winChecker(rowIndex: Int, columnIndex: Int, testLevel: Int, actualLevel: Int, winChecker: Array<Array<String>>, turnValue: String): WinningStatus {
        // TODO: Refactor this function
        var winOrTie = false
        var isBoardChangerCalled = false
        var value = ""
        var value1 = ""
        var value2 = ""
        var x = ""
        var r = rowIndex
        var c = columnIndex

        if (testLevel == 1 && actualLevel >= 2) {
            r /= 3
            c /= 3
        }

        // Check the column
        // TODO: Clean up this approach - possible solution is just use rowIndex/3 as the base
        when (r % 3) {
            0 -> {
                value = winChecker[r][c]
                value1 = winChecker[r + 1][c]
                value2 = winChecker[r + 2][c]
            }
            1 -> {
                value = winChecker[r - 1][c]
                value1 = winChecker[r][c]
                value2 = winChecker[r + 1][c]
            }
            2 -> {
                value = winChecker[r - 2][c]
                value1 = winChecker[r - 1][c]
                value2 = winChecker[r][c]
            }
        }

        // Seeing if all the cells in the column have the same value
        if (value != "" && value1 != "" && value2 != "" && value == value1 && value1 == value2) {
            x = if (turnValue == "") {
                turn
            } else {
                turnValue
            }
        }

        // Check the row
        // TODO: Clean up this approach - possible solution is just use columnIndex/3 as the base
        when (c % 3) {
            0 -> {
                value = winChecker[r][c]
                value1 = winChecker[r][c + 1]
                value2 = winChecker[r][c + 2]
            }
            1 -> {
                value = winChecker[r][c - 1]
                value1 = winChecker[r][c]
                value2 = winChecker[r][c + 1]
            }
            2 -> {
                value = winChecker[r][c - 2]
                value1 = winChecker[r][c - 1]
                value2 = winChecker[r][c]
            }
        }

        // Seeing if all the cells in the row have the same value
        if (value != "" && value1 != "" && value2 != "" && value == value1 && value1 == value2) {
            x = if (turnValue == "") {
                turn
            } else {
                turnValue
            }
        }

        // Check the top-left to bottom-right diagonal
        // TODO: Clean up this approach - possible solution is just use columnIndex/3 & rowIndex/3 as the base
        if (r % 3 == 0 && c % 3 == 0) {
            value = winChecker[r][c]
            value1 = winChecker[r + 1][c + 1]
            value2 = winChecker[r + 2][c + 2]
        } else if (r % 3 == 1 && c % 3 == 1) {
            value = winChecker[r - 1][c - 1]
            value1 = winChecker[r][c]
            value2 = winChecker[r + 1][c + 1]
        } else if (r % 3 == 2 && c %3 == 2) {
            value = winChecker[r - 2][c - 2]
            value1 = winChecker[r - 1][c - 1]
            value2 = winChecker[r][c]
        }

        // Seeing if all the cells in the row have the same value
        if (value != "" && value1 != "" && value2 != "" && value == value1 && value1 == value2) {
            x = if (turnValue == "") {
                turn
            } else {
                turnValue
            }
        }

        // Check the top-right to bottom-left diagonal
        // TODO: Clean up this approach - possible solution is just use columnIndex/3 & rowIndex/3 as the base
        if (r % 3 == 2 && c % 3 == 0) {
            value = winChecker[r][c]
            value1 = winChecker[r - 1][c + 1]
            value2 = winChecker[r - 2][c + 2]
        } else if (r % 3 == 1 && c % 3 == 1) {
            value = winChecker[r + 1][c - 1]
            value1 = winChecker[r][c]
            value2 = winChecker[r - 1][c + 1]
        } else if (r % 3 == 0 && c %3 == 2) {
            value = winChecker[r + 2][c - 2]
            value1 = winChecker[r + 1][c - 1]
            value2 = winChecker[r][c]
        }

        // Seeing if all the cells in the row have the same value
        if (value != "" && value1 != "" && value2 != "" && value == value1 && value1 == value2) {
            x = if (turnValue == "") {
                turn
            } else {
                turnValue
            }
        }

        // Setting the winning player name and winning letter
        // TODO: Make this not based on the letter being played (if possible)
        var winningPlayerName = ""
        if (x == "X") {
            winningPlayerName = game.player1.playerName
            winningLetter.text = "X"
        }
        if (x == "O") {
            winningPlayerName = game.player2.playerName
            winningLetter.text = "O"
        }

        // Change board as necessary based off if a winner was found
        if (x != "") {
            when(testLevel) {
                1 -> {
                    if (Board.isInstructionalGame) {
                        showClosingAlertDialog(actualLevel, x)
                    } else {
                        board.finishActivity(context, true, winningPlayerName)
                        Board.isWinOrTie = true
                    }
                    winOrTie = true
                }
                2 -> {
                    if (actualLevel == 2) {
                        board.winningBoardChanger(r, c, testLevel, actualLevel, x, context, winCheck)
                        isBoardChangerCalled = true
                    }
                }
            }
        }

        // Check for ties as well
        val tieCheckerString = when(Board.level) {
            1 -> "Inner"
            2 -> "Outer"
            else -> ""
        }

        // Handle it in case a tie is found
        if (!winOrTie && x == "" && tieChecker(tieCheckerString, testLevel, r, c)) {
            if (Board.isInstructionalGame) {
                showClosingAlertDialog(actualLevel, "")
            } else {
                board.finishActivity(context, true, "Tie")
            }
            winOrTie = true
        }

        return WinningStatus(winOrTie, isBoardChangerCalled)
    }

    /*
	 * Creates a string first and then turns the game into a byte array
	 */
    fun winCheckToByteArray(winCheck: Array<Array<String>>): ByteArray {
        val gameString = StringBuilder()
        for (a in winCheck) {
            for (j in winCheck[0].indices) {
                gameString.append(a[j]).append(",")
            }
            gameString.append(";")
        }
        return gameString.toString().toByteArray()
    }
}