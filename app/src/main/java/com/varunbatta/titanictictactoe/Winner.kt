package com.varunbatta.titanictictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class Winner : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.winner)

        Board.keys = HashMap(6561)
        ButtonPressed.currentTurn = ""

        val winnerName = intent.getStringExtra("Winner")!!

        val congrats = findViewById<TextView>(R.id.congrats)

        congrats.text = if (winnerName == "Tie") {
            getString(R.string.tieGame)
        } else {
            getString(R.string.wonGame, winnerName)
        }

        val bottomPanel = findViewById<LinearLayout>(R.id.bottomPanel)
        val bottomPanelListener = BottomPanelListener(this, bottomPanel, null)
        val mainMenu = findViewById<Button>(R.id.mainMenu)
        mainMenu.setOnClickListener(bottomPanelListener)
        val viewGame = findViewById<Button>(R.id.viewGame)
        viewGame.setOnClickListener(bottomPanelListener)
        val rematch = findViewById<Button>(R.id.rematch)
        rematch.setOnClickListener(bottomPanelListener)
    }
}