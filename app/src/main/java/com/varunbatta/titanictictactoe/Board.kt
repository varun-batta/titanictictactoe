package com.varunbatta.titanictictactoe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import java.util.Timer
import java.util.TimerTask

class Board : ComponentActivity() {
    // TODO: See how many of these variables are required
    companion object {
        lateinit var game : Game
        var level = 0
        var keys = HashMap<Int, Button>()
        var winCheck = Array(10) { _ -> Array(9) { _ -> ""} }
        var isWinOrTie = false
        var RC_SAVED_GAMES = 9009
        var REQUEST_RESOLVE_ERROR = 1001
        lateinit var progressBarHolder: FrameLayout
        lateinit var boardActivity : Board
        lateinit var sharedPreferences: SharedPreferences
    }
    lateinit var playerTurn : TextView
    var isOnCreateCalled = false
    var isBoardVisible = false
    var row = -1
    var column = -1
    var onGoingMatch : ByteArray? = null
    lateinit var bp : ButtonPressed
    lateinit var context: Context
    lateinit var bottomPanel: LinearLayout
    lateinit var boardLayout: LinearLayout
    var isMyTurn = false
    var isSavedGame = false
    var isFinished = false
    var canRematch = false
    var isSaveCalled = false
    var isResolvingError = false
    lateinit var board : BasicBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOnCreateCalled = true
        isBoardVisible = true
        setContentView(R.layout.board)
        context = applicationContext
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        level = intent.getIntExtra("Level", 0)
        onGoingMatch = intent.getByteArrayExtra("On Going Match")
        isMyTurn = intent.getBooleanExtra("My Turn", true)
        isFinished = intent.getBooleanExtra("IsFinished", false)
        canRematch = intent.getBooleanExtra("Can Rematch", true)
        isSavedGame = intent.getBooleanExtra("Saved Game", false)

        // TODO: Handle game creation/recreation once saving is implemented
        val player1 = Player(intent.getStringExtra("Player 1 Name")!!, "X")
        val player2 = Player(intent.getStringExtra("Player 2 Name")!!, "O")
        game = Game(level, player1, player2)

        bp = ButtonPressed(context, level, game, this)

        // TODO: See if this is required
        boardActivity = this

        boardLayout = findViewById(R.id.boardLayout)

        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        board = findViewById(R.id.board)
        board.init(width, level)
        board.metaRow = 0
        board.metaColumn = 0
        board.winCheck = game.data
        board.configureBoard(width, level, level, this)

        for (button in keys.values) {
            button.isEnabled = true
        }

        val levelTitle = findViewById<TextView>(R.id.levelTitle)
        when(level) {
            1 -> {
                levelTitle.text = getString(R.string.level1TitleGame)
            }
            2 -> {
                levelTitle.text = getString(R.string.level2TitleGame)
            }
            3 -> {
                levelTitle.text = getString(R.string.level3TitleGame)
            }
            4 -> {
                levelTitle.text = getString(R.string.level4TitleGame)
            }
        }

        // TODO: Fix bottom panel (looks like it's missing things)
        bottomPanel = findViewById(R.id.bottom_panel)
        playerTurn = findViewById(R.id.player_turn)
        playerTurn.text = getString(R.string.playersTurn, game.player1.playerName)
    }

    override fun onResume() {
        super.onResume()
        if (game.lastMove == "X") {
            playerTurn.text = getString(R.string.playersTurn, game.player1.playerName)
        } else if (game.lastMove.contains("O")) {
            playerTurn.text = getString(R.string.playersTurn, game.player2.playerName)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO: See what this is for (the screen being closed?)
        outState.putByteArray(
            "On Going Match",
            bp.winCheckToByteArray(ButtonPressed.winCheck)
        )

        val editor = sharedPreferences.edit()
        editor.putString(
            "On Going Match",
            Base64.encodeToString(
                bp.winCheckToByteArray(ButtonPressed.winCheck),
                Base64.DEFAULT
            )
        )
        editor.commit()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // TODO: See what this is for (the screen being awakened?)
        onGoingMatch = savedInstanceState.getByteArray("On Going Match")
    }

    fun savedGameRecreate(onGoingMatch: ByteArray) {
        val gameArray = onGoingMatch.toString()
        val rows = gameArray.split(";".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val firstRow = rows[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val game: Array<Array<String>> = Array(rows.size) { Array(firstRow.size) { "" } }
        for (l in rows.indices) {
            game[l] = rows[l].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        winCheck = game
    }

    fun winningBoardChanger(rowIndex: Int, columnIndex: Int, level: Int, actual: Int, x: String, context: Context, winCheck : Array<Array<String>>) {
        // TODO: Why are these global variables required?
        row = rowIndex
        column = columnIndex
        bp = ButtonPressed(context, level, game, boardActivity)
        Board.winCheck = winCheck

        if (actual == 2) {
            // TODO: See if there's a better way to do this
            ButtonPressed.metaWinCheck[rowIndex / 3][columnIndex / 3] = x
            val miniBoard = BasicBoardView.metaBoard[rowIndex / 3][columnIndex / 3]
            miniBoard?.findViewById<ImageView>(R.id.boardBackground)?.alpha = 0f
            miniBoard?.findViewById<ImageView>(R.id.boardBackgroundRed)?.alpha = 0f
            miniBoard?.findViewById<LinearLayout>(R.id.overlaying_linear_layout)?.alpha = 0f
            val won = TextView(context)
            won.layoutParams = miniBoard?.layoutParams
            won.text = x
            won.textSize = 75f
            won.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            won.textAlignment = View.TEXT_ALIGNMENT_CENTER
            miniBoard?.addView(won)

            // Create a timed UI event to handle the reload (as necessary)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    // If you want to operate UI modifications, you must run ui stuff on UiThread.
                    runOnUiThread {
                        bp.boardChanger(row, column, 2, true)
                        if (bp.tieChecker("Outer", level, row, column)) {
                            finishActivity(context, true, "Tie")
                        }
                    }
                    isWinOrTie =
                        bp.winChecker(rowIndex, columnIndex, 1, 2, ButtonPressed.metaWinCheck, x)
                }
            }, 500)
        }
    }

    fun finishActivity(context: Context, isWon: Boolean, winnerName: String) {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.commit()

        // TODO: Clean this up as necessary
        if (isWon) {
            isWinOrTie = false
            val winner = Intent(context, Winner::class.java)
            winner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            winner.putExtra("Winner", winnerName)
            context.startActivity(winner)
        } else {
            keys = HashMap(6561)
            bottomPanel.removeAllViews()
            boardLayout.removeAllViews()
            val menu = Intent(context, MainMenu::class.java)
            menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(menu)
        }

        finish()
    }
}