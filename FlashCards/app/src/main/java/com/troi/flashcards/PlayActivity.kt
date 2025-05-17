package com.troi.flashcards

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.troi.flashcards.data.AppDatabase
import com.troi.flashcards.data.DeckDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class PlayActivity : AppCompatActivity() {

    //lateinit vars
    private lateinit var appDatabase: AppDatabase
    private lateinit var deckDao: DeckDao

    //local scope deck id and name
    var deckId = -1
    var deckName = "missing"
    var questions: MutableList<String> = mutableListOf()

    //get linear conmtainer
    private lateinit var linearContainer: LinearLayout

    //get question view
    private lateinit var questionView: TextView

    //question selection logic
    private var questionIndexes: MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlayout)

        //initialize database and dao
        appDatabase = AppDatabase.getDatabase((applicationContext))
        deckDao = appDatabase.deckDao()

        //get linear container
        linearContainer = findViewById(R.id.linearContainer)

        //get question view
        questionView = findViewById<TextView>(R.id.question)

        //init vars and get deck id
        deckId = intent.getIntExtra("deckId", -1)

        //get initial list of question indexes
        questionIndexes = (0..questions.size-1).toMutableList()

        //get deck save
        if (deckId != -1) {
            Log.d("MainActivity", "||||| obtained deck id: " + deckId.toString())

            //suspend func in corotuine
            val context = this
            var content = ""
            lifecycleScope.launch(Dispatchers.IO) {
                //get deck save by id
                val deckSave = deckDao.getDeckById(deckId)

                //if correctly got decksave, sync name var to saved name, sync content var to saved var
                if (deckSave != null)
                {
                    deckName = deckSave.name
                    content = deckSave.content
                }
                else
                {
                    Log.d("MainActivity", "||||| could not load deck save due to database row of index not containing a saved deck")}

                //switch to main thread and update name textview
                withContext(Dispatchers.Main) {
                    val deckNameView = findViewById<TextView>(R.id.deckName)
                    deckNameView.text = deckName

                    //seperate content into questions
                    questions = content.split("||").toMutableList()

                    //set initial question
                    displayNextQuestion()
                }
            }
        }
        else {
            Log.d("MainActivity", "||||| could not load deck save due to incorrectly passed deck id from main activity to edit activity")
        }

        //set onclicklistener for layout to switch question
        linearContainer.setOnClickListener  {
            displayNextQuestion()
        }
    }

    //function to manage question logic
    private fun displayNextQuestion() {
        //if possible indexes is empty, reset it
        if (questionIndexes.size == 0) {
            questionIndexes = (0..questions.size-1).toMutableList()
            Log.d("MainActivity", "||||| regenerated questionIndexes due to passing through all of the questions already")
        }

        //get random element from list
        var randomIndex = (0..questionIndexes.size-1).random()

        //set question to string at the generated index
        setQuestion(questionIndexes[randomIndex])

        //remove generated index from list
        questionIndexes.removeAt(randomIndex)
        Log.d("MainActivity", "||||| " + questionIndexes.size)
    }

    //function that displays question by index of question on main question view
    private fun setQuestion(index: Int) {
        //set question views content to string at index of all questions
        questionView.text = questions[index]
    }
}