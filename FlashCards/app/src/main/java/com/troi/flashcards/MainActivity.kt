package com.troi.flashcards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.troi.flashcards.data.DeckSave
import com.troi.flashcards.data.DeckDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    //(private late initialised var) (initialised later)
    private lateinit var linearContainer: LinearLayout

    //room db stuff
    private lateinit var appDatabase: AppDatabase
    private lateinit var deckDao: DeckDao

    //create list of all decks
    val allDecks: MutableList<Deck> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        //get main layout container for all decks
        linearContainer = findViewById(R.id.linearContainer)

        //initialize database and dao
        appDatabase = AppDatabase.getDatabase((applicationContext))
        deckDao = appDatabase.deckDao()

        //load saved decks to list of all
        var savedDeckSaves: List<DeckSave> = listOf()
        var context = this          //vars to pass into corotuine
        var lifeCycleOwner = this   //
        lifecycleScope.launch(Dispatchers.IO) {
            savedDeckSaves = deckDao.getAllDecks()
            Log.d("MainActivity", "||||| loaded saved saves")

            //from saved get deck objects
            for (savedSave in savedDeckSaves) {
                var loadedDeck = Deck(savedSave.name, deckDao, savedSave, linearContainer, context, lifeCycleOwner)
                allDecks.add(loadedDeck)
                Log.d("MainActivity", "||||| created loaded deck objects")
            }

            //switch coroutine to main thread for ui changes (that can only happen on main thread)
            withContext(Dispatchers.Main) {
                //display all deck uis
                for (deck in allDecks) {
                    deck.addElementToLayout(linearContainer)
                }
            }
        }

        //get add button and set on click listener to add decks
        var addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {

            //create new deck and add it to list of all, then add to db and display ui
            var newDeck = Deck("New Deck", deckDao, null, linearContainer, this, this)
            allDecks.add(newDeck)

            newDeck.addSaveToDatabase(this)
            newDeck.addElementToLayout(linearContainer)
        }
    }
}

//--------------------------------------------------------------------------------------------------

//deck class
class Deck(var deckName: String, deckDao: DeckDao, prevSave: DeckSave?, linearContainer: LinearLayout, context: Context, lifecycleOwner: LifecycleOwner) {

    //initialize vars
    var name: String = deckName
    var deckSave = DeckSave(name = name)
    var id: Int = deckSave.id
    var deckElement = generateDeckElement(this, linearContainer, context, lifecycleOwner)

    var deckDao = deckDao
    var hasDisplayedView = false

    //init to correctly apply previous save if it exists
    init {
        //if previous save exists, override auto generated save and id, regenerate element with new correct id
        if (prevSave != null)
        {
            deckSave = prevSave
            id = deckSave.id
            deckElement = generateDeckElement(this, linearContainer, context, lifecycleOwner)
        }
        //otherwise everything has been generated correctly
    }

    //function to add deck save to db
    fun addSaveToDatabase(lifecycleOwner: LifecycleOwner) {
        //generate a decksave
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO)  {
            deckDao.insertDeck(deckSave)

            //temp logging
            Log.d("MainActivity", "||||| added save to database with id: $id")
        }
    }

    //func to add deck element to views
    fun addElementToLayout(linearContainer: LinearLayout) {
        linearContainer.addView(deckElement)
        hasDisplayedView = true
    }

    //func to delete deck from views and database
    fun deleteDeck(linearContainer: LinearLayout, lifecycleOwner: LifecycleOwner) {
        //remove from db
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO)  {
            deckDao.deleteDeckById(deckSave.id)
            Log.d("MainActivity", "||||| deleted save from database")
        }

        //remove from layout if displayed before
        if (hasDisplayedView) {
            linearContainer.removeView(deckElement)
        }
    }
}

//--------------------------------------------------------------------------------------------------

//func to add a deck object in the linear container
fun generateDeckElement(parentDeck: Deck, linearContainer: LinearLayout, context: Context, lifecycleOwner: LifecycleOwner): View {

    //create an instantiater manager
    val inflater = LayoutInflater.from(context)

    //instantiate a prefab view from deck_element.xml extra file
    val elementView = inflater.inflate(R.layout.deck_element, linearContainer, false)

    //get name text text view and linear layout containing buttons
    val nameText = elementView.findViewById<TextView>(R.id.nameText)
    val buttonLayout = elementView.findViewById<LinearLayout>(R.id.buttonLayout)

    //apply deck name to text views text
    nameText.text = parentDeck.name

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
            {loadPlayActivity(parentDeck.id, context)}

            //edit button pressed
            else if (i == 1)
            {loadEditActivity(parentDeck.id, context)}

            //delete button pressed
            else if (i == 2)
            {
                //delete deck
                parentDeck.deleteDeck(linearContainer, lifecycleOwner)
            }
        }
    }

    return elementView
}

//----------------------------------------------------------------------------------------------

private fun loadPlayActivity(deckIndex: Int, context: Context) {
    //get new intent
    val intent = Intent(context, PlayActivity::class.java)

    //send deck index data
    intent.putExtra("deckIndex", deckIndex)

    //start intent as activity
    context.startActivity(intent)
}

private fun loadEditActivity(deckIndex: Int, context: Context) {
    //get new intent
    val intent = Intent(context, EditActivity::class.java)

    //send deck index data
    intent.putExtra("deckIndex", deckIndex)

    //start intent as activity
    context.startActivity(intent)
}