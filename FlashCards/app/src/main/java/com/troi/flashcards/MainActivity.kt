package com.troi.flashcards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.troi.flashcards.ui.theme.FlashCardsTheme

class MainActivity : ComponentActivity() {

    //(private late initialised var) (initialised later)
    private lateinit var linearContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        //get main layout container for all decks
        linearContainer = findViewById(R.id.linearContainer)

        //get add button and set on click listener to add decks
        var addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            addDeck("New Deck")
        }

        //TEMP --------------------------
        //create 5 decks
        repeat(5) { i ->
            addDeck("Deck $i")
        }
    }

    //func to add a deck object in the linear container
    private fun addDeck(name: String) {

        //create an instantiater manager
        val inflater = LayoutInflater.from(this)
        //instantiate a prefab view from deck_element.xml extra file
        val elementView = inflater.inflate(R.layout.deck_element, linearContainer, false)

        //get name text text view and linear layout containing buttons
        val nameText = elementView.findViewById<TextView>(R.id.nameText)
        val buttonLayout = elementView.findViewById<LinearLayout>(R.id.buttonLayout)

        //apply deck name to text views text
        nameText.text = name

        //handle click to show buttons
        nameText.setOnClickListener {
            //set buttons to visible
            nameText.visibility = View.GONE
            buttonLayout.visibility = View.VISIBLE
        }

        //hide buttons when clicked again
        for (i in 0 until buttonLayout.childCount) {
            //for every button set text to visible
            buttonLayout.getChildAt(i).setOnClickListener {
                buttonLayout.visibility = View.GONE
                nameText.visibility = View.VISIBLE
            }
        }

        linearContainer.addView(elementView)
    }
}