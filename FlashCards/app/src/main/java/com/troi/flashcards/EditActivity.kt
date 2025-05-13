package com.troi.flashcards

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editlayout)

        //get deck index and display on text
        val deckIndex = intent.getIntExtra("deckIndex", -1)
        val tempText = findViewById<TextView>(R.id.tempText)
        tempText.text = "Edit deck: " + deckIndex.toString()
    }
}