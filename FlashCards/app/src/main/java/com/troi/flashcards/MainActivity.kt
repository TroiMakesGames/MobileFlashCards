package com.troi.flashcards

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
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
import com.troi.flashcards.data.AppDatabase
import com.troi.flashcards.ui.theme.FlashCardsTheme

//room db stuff
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.troi.flashcards.data.Deck
import com.troi.flashcards.data.DeckDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    //(private late initialised var) (initialised later)
    private lateinit var linearContainer: LinearLayout

    //get var for deck indexing
    var numOfDecks: Int = 0

    //room db stuff
    private lateinit var appDatabase: AppDatabase
    private lateinit var deckDao: DeckDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        //get main layout container for all decks
        linearContainer = findViewById(R.id.linearContainer)

        //get add button and set on click listener to add decks
        var addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {

            //create some initial vars
            var newDeckName = "New Deck"
            var newDeckId = -1

            //create a new deck saved object in db and return its id for further operations
            CoroutineScope(Dispatchers.Main).launch {
                newDeckId = addDeckToDatabase(newDeckName)
            }

            //create a ui element
            addDeckElement(newDeckId, newDeckName, numOfDecks)
        }

        //initialize database and dao
        appDatabase = AppDatabase.getDatabase((applicationContext))
        deckDao = appDatabase.deckDao()

        //create deck elements on xml from queried existing decks
        loadDecksAsUI()
    }

    //func to add a deck object in the linear container
    private fun addDeckElement(deckId: Int, name: String, newDeckIndex: Int) {

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
                //toggle buttons visibility
                buttonLayout.visibility = View.GONE
                nameText.visibility = View.VISIBLE

                //play button pressed
                if (i == 0)
                {loadPlayActivity(newDeckIndex)}

                //edit button pressed
                else if (i == 1)
                {loadEditActivity(newDeckIndex)}

                //delete button pressed
                else if (i == 2)
                {
                    //remove ui element
                    deleteDeckElement(elementView)
                    //remove from db by id
                    removeDeck(deckId)
                }
            }
        }

        //increase deck num for next indexes
        numOfDecks += 1

        linearContainer.addView(elementView)
    }

    //----------------------------------------------------------------------------------------------

    private fun loadPlayActivity(deckIndex: Int) {
        //get new intent
        val intent = Intent(this, PlayActivity::class.java)

        //send deck index data
        intent.putExtra("deckIndex", deckIndex)

        //start intent as activity
        startActivity(intent)
        overridePendingTransition(0, 0)     //skip animation
    }

    private fun loadEditActivity(deckIndex: Int) {
        //get new intent
        val intent = Intent(this, EditActivity::class.java)

        //send deck index data
        intent.putExtra("deckIndex", deckIndex)

        //start intent as activity
        startActivity(intent)
        overridePendingTransition(0, 0)     //skip animation
    }

    private fun deleteDeckElement(deckElement: View) {
        //delete deck
        linearContainer.removeView(deckElement)
        numOfDecks -= 1
    }

    //----------------------------------------------------------------------------------------------

    //after creating a deck add it to the database
    suspend fun addDeckToDatabase(deckName: String): Int {
        //create new deck object
        val newDeck = Deck(name = deckName)
        deckDao.insertDeck(newDeck)

        return newDeck.id
    }

    //get all deck list from db and load ui elements
    private fun loadDecksAsUI() {
        //load decks from db, coroutine fro db operations
        CoroutineScope(Dispatchers.Main).launch {
            val allDecks = deckDao.getAllDecks()

            //add deck elements as ui, run in corotuine for allDecks scope
            for (deck in allDecks)
            { addDeckElement(deck.id, deck.name, numOfDecks) }
        }
    }

    //delete saved deck from db
    private fun removeDeck(deckId: Int) {
        //coroutine for db operations
        CoroutineScope(Dispatchers.IO).launch {
            deckDao.deleteDeckById(deckId)
        }
    }
}