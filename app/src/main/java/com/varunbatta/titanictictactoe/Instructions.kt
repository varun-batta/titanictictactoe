package com.varunbatta.titanictictactoe

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.activity.ComponentActivity

class Instructions: ComponentActivity() {
    // TODO: Seriously, make the instructions proper not just giant blocks of text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val level1Height = displayMetrics.widthPixels*706/1156
        val level2Height = displayMetrics.widthPixels*1695/1075
        val level1ImageParams = LayoutParams(width, level1Height)
        val level2ImageParams = LayoutParams(width, level2Height)

        val level1UnfilledImage = findViewById<ImageView>(R.id.level1UnfilledImage)
        level1UnfilledImage.layoutParams = level1ImageParams

        val level1FilledImage = findViewById<ImageView>(R.id.level1FilledImage)
        level1FilledImage.layoutParams = level1ImageParams

        val level2UnfilledImage = findViewById<ImageView>(R.id.level2UnfilledImage)
        level2UnfilledImage.layoutParams = level2ImageParams

        val level2MakingAMoveImage = findViewById<ImageView>(R.id.level2MakingAMoveImage)
        level2MakingAMoveImage.layoutParams = level2ImageParams

        val level2PreMetaWinImage = findViewById<ImageView>(R.id.level2PreMetaWinImage)
        level2PreMetaWinImage.layoutParams = level2ImageParams

        val level2PostMetaWinImage = findViewById<ImageView>(R.id.level2PostMetaWinImage)
        level2PostMetaWinImage.layoutParams = level2ImageParams

        val level1TrialButton = findViewById<Button>(R.id.level1TrialButton)
        level1TrialButton.setOnClickListener { view -> startTrial((view as Button).text.toString()) }

        val level2TrialButton = findViewById<Button>(R.id.level2TrialButton)
        level2TrialButton.setOnClickListener { view -> startTrial((view as Button).text.toString()) }
    }

    private fun startTrial(trialChoiceLabel: String) {
        val trial = Intent(this, Board::class.java)
        trial.putExtra("Player 1 Name", "Player 1")
        trial.putExtra("Player 2 Name", "Player 2")
        when (trialChoiceLabel) {
            "Level 1 - Trial" -> {
                trial.putExtra("Level", 1)
            }
            "Level 2 - Trial" -> {
                trial.putExtra("Level", 2)
            }
        }
        startActivity(trial)
        this.finish()
    }
}