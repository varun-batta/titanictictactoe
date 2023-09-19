package com.varunbatta.titanictictactoe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class BottomPanelListener(
    var activity: ComponentActivity,
    var bottomPanel: LinearLayout,
    var boardLayout: LinearLayout?,
) : OnClickListener {
    override fun onClick(pressedButton: View?) {
        activity.finish()
        val editor = Board.sharedPreferences.edit()
        editor.clear()
        editor.commit()
        Board.keys = HashMap(6561)
        bottomPanel.removeAllViews()
        boardLayout?.removeAllViews()
        ButtonPressed.winCheck = Array(10) { _ -> Array(9) { _ -> "" } }
        ButtonPressed.metaWinCheck = Array(3) { _ -> Array(3) { _ -> "" } }
        Board.winCheck = Array(10) { _ -> Array(9) { _ -> "" } }
        ButtonPressed.currentTurn = ""
        val pressedButtonText = (pressedButton as Button).text.toString()
        when(pressedButtonText) {
            "Main Menu" -> {
                val menuIntent = Intent(activity, MainMenu::class.java)
                activity.startActivity(menuIntent)
            }
            "New Game" -> {
                val newGameIntent = Intent(activity, PlayerSelector::class.java)
                activity.startActivity(newGameIntent)
            }
            "View Game" -> {
                activity.finish()
            }
            "Rematch?" -> {
                val boardIntent = Intent(activity, Board::class.java)
                boardIntent.putExtra("Level", Board.level)
                boardIntent.putExtra("Caller", "Winner")
                boardIntent.putExtra("My Turn", true)
                boardIntent.putExtra("IsFinished", false)
                boardIntent.putExtra("Player 1 Name", Board.game.player1.playerName)
                boardIntent.putExtra("Player 2 Name", Board.game.player2.playerName)
                activity.startActivity(boardIntent)
                activity.finish()
            }
        }
    }
}