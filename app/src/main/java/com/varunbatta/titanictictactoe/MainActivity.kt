package com.varunbatta.titanictictactoe

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.GridView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_screen)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var width = displayMetrics.widthPixels

        val introScreenBoard = findViewById<GridView>(R.id.introScreenBoard)
        introScreenBoard.adapter = LaunchScreenAdapter(this, width)

        val playButton = findViewById<Button>(R.id.play_button)
        playButton.setOnClickListener {
            val mainMenuIntent = Intent(this, MainMenu::class.java)
            startActivity(mainMenuIntent)
            this.finish()
        }
    }
}