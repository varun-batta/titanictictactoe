package com.varunbatta.titanictictactoe

import android.app.AlertDialog
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

class Board : ComponentActivity() {
    // TODO: See how many of these variables are required
    companion object {
        lateinit var game : Game
        var level = 0
        var keys = HashMap<Int, Button>()
        var enabledKeys = mutableListOf<Int>()
        var winCheck = Array(10) { _ -> Array(9) { _ -> ""} }
        var isWinOrTie = false
        var RC_SAVED_GAMES = 9009
        var REQUEST_RESOLVE_ERROR = 1001
        var isInstructionalGame = false
        lateinit var progressBarHolder: FrameLayout
        lateinit var boardActivity : Board
        lateinit var sharedPreferences: SharedPreferences
    }
    private lateinit var playerTurn : TextView
    private var isOnCreateCalled = false
    private var isBoardVisible = false
    private var row = -1
    private var column = -1
    private var onGoingMatch : ByteArray? = null
    private lateinit var bp : ButtonPressed
    private lateinit var context: Context
    private lateinit var bottomPanel: LinearLayout
    private lateinit var boardLayout: LinearLayout
    private var isMyTurn = false
    private var isSavedGame = false
    private var isFinished = false
    private var canRematch = false
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
        isInstructionalGame = intent.getBooleanExtra("isInstructionalGame", false)

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
        if (game.player1.playerName == "Your") {
            playerTurn.text = "Your Turn"
        }

        if (isInstructionalGame) {
            ButtonPressed.curInstructionsStep = 2
            var dialogText = when(level) {
                1 -> R.string.level1Instructions1
                2 -> R.string.level2Instructions1
                else -> -1
            }
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage(dialogText)
                .setNegativeButton("OK") { _, _ -> }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (game.lastMove == "X") {
            playerTurn.text = getString(R.string.playersTurn, game.player1.playerName)
            if (game.player1.playerName == "Your") {
                playerTurn.text = "Your Turn"
            }
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
        editor.apply()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // TODO: See what this is for (the screen being awakened?)
        onGoingMatch = savedInstanceState.getByteArray("On Going Match")
    }

    // Instructional Game Helpers
    fun populateEnabledKeys(metaRow: Int, metaColumn: Int) {
        enabledKeys = mutableListOf()
        for ((key, button) in keys) {
            // Check for whether key should not be put into enabledKeys (it's the same as the section, so user's won't be able to see any movement)
            val keyMetaColumn = key % 3
            val keyMetaRow = (key / 9) % 3
            if (button.isEnabled && keyMetaRow != metaRow && keyMetaColumn != metaColumn) {
                enabledKeys.add(key)
            }
        }
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
            // Making sure to disable keys in the won section so that enabled keys are populated correctly
            for (r in (rowIndex/3)*3 until (rowIndex/3)*3 + 3) {
                for (c in (columnIndex/3)*3 until (columnIndex/3)*3 + 3) {
                    val button = keys[r*9+c]
                    button?.isEnabled = false
                }
            }
            val won = TextView(context)
            won.layoutParams = miniBoard?.layoutParams
            won.text = x
            won.textSize = 75f
            won.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
            won.textAlignment = View.TEXT_ALIGNMENT_CENTER
            miniBoard?.addView(won)
            bp.boardChanger(row, column, 2, true)
            if (bp.tieChecker("Outer", level, row, column)) {
                finishActivity(context, true, "Tie")
            }
            val winningStatus = bp.winChecker(rowIndex, columnIndex, 1, 2, ButtonPressed.metaWinCheck, x)
            isWinOrTie = winningStatus.winOrTie
        }
    }

    fun finishActivity(context: Context, isWon: Boolean, winnerName: String) {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // TODO: Clean this up as necessary
        if (isWon) {
            isWinOrTie = false
            val winner = Intent(context, Winner::class.java)
            winner.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            winner.putExtra("Winner", winnerName)
            context.startActivity(winner)
        } else {
            keys = HashMap(6561)
            if (this::bottomPanel.isInitialized) {
                bottomPanel.removeAllViews()
            }
            if (this::boardLayout.isInitialized) {
                boardLayout.removeAllViews()
            }
            val menu = Intent(context, MainMenu::class.java)
            menu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(menu)
        }

        finish()
    }
}