package com.varunbatta.titanictictactoe

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainMenu : ComponentActivity() {
    // TODO: Redraw this using recycler views and perhaps just generally (see how it is more popularly handled)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        val instructionsButton = findViewById<Button>(R.id.instructionsButton)
        instructionsButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setMessage(R.string.instructionsPrompt)
                .setPositiveButton("Yes") { dialog, which ->
                    instructionalGame(2)
                }
                .setNegativeButton("No") { dialog, which ->
                    instructionalGame(1)
                }
                .show()
        }

        val level1 = findViewById<Button>(R.id.level1Button)
        level1.setOnClickListener { playerSelector(1) }

        val level2 = findViewById<Button>(R.id.level2Button)
        level2.setOnClickListener { playerSelector(2) }

        val level3 = findViewById<Button>(R.id.level3Button)
        level3.setOnClickListener { playerSelector(3) }

        val level4 = findViewById<Button>(R.id.level4Button)
        level4.setOnClickListener { playerSelector(4) }
    }

    private fun playerSelector(level: Int) {
        if (level == 3 || level == 4) {
            Toast.makeText(this, getString(R.string.unavailableLevel, level), Toast.LENGTH_SHORT).show()
            return
        }

        val playerSelectorIntent = Intent(this, PlayerSelector::class.java)
        playerSelectorIntent.putExtra("Level", level)
        startActivity(playerSelectorIntent)
        this.finish()
    }

    private fun instructionalGame(level: Int) {
        val boardIntent = Intent(this, Board::class.java)
        boardIntent.putExtra("isInstructionalGame", true)
        boardIntent.putExtra("Player 1 Name", "Your")
        boardIntent.putExtra("Player 2 Name", "AI")
        boardIntent.putExtra("Level", level)
        startActivity(boardIntent)
        this.finish()
    }
}