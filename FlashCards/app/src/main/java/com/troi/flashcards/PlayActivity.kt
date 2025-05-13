package com.troi.flashcards

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlayout)

        //get deck index and display on text
        val deckIndex = intent.getIntExtra("deckIndex", -1)
        val tempText = findViewById<TextView>(R.id.tempText)
        tempText.text = "Play deck: " + deckIndex.toString()
    }
}