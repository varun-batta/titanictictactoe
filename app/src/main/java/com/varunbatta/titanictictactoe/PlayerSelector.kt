package com.varunbatta.titanictictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class PlayerSelector : ComponentActivity() {
    private var level = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_selector)

        level = intent.getIntExtra("Level", 1)

        val player1EditText = findViewById<EditText>(R.id.player1Name)
        val player2EditText = findViewById<EditText>(R.id.player2Name)

        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener {
            val player1Name = player1EditText.text.toString()
            val player2Name = player2EditText.text.toString()
            if (player1Name == "" || player2Name == "") {
                Toast.makeText(this, R.string.playerNameRequired, Toast.LENGTH_SHORT).show()
            } else {
                val boardIntent = Intent(applicationContext, Board::class.java)
                boardIntent.putExtra("Level", level)
                boardIntent.putExtra("Player 1 Name", player1Name)
                boardIntent.putExtra("Player 2 Name", player2Name)
                boardIntent.putExtra("Caller", "PlayerSelector")
                boardIntent.putExtra("My Turn", true)
                boardIntent.putExtra("IsFinished", false)
                startActivity(boardIntent)
                this.finish()
            }
        }
    }
}