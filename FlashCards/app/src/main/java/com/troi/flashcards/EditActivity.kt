package com.troi.flashcards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.troi.flashcards.data.AppDatabase
import com.troi.flashcards.data.DeckSave
import com.troi.flashcards.data.DeckDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditActivity : AppCompatActivity() {

    //lateinit vars
    private lateinit var appDatabase: AppDatabase
    private lateinit var deckDao: DeckDao

    //local scope deck id and name
    var deckId = -1
    var deckName = "missing"
    var content = "missing question 1||missing question 2||missing question 3"
    private lateinit var questions: MutableList<String>

    //get linear container for edit texts
    private lateinit var linearContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editlayout)

        //initialize database and dao
        appDatabase = AppDatabase.getDatabase((applicationContext))
        deckDao = appDatabase.deckDao()

        //get linear container for question views
        linearContainer = findViewById(R.id.linearContainer)

        //init vars and get deck id
        deckId = intent.getIntExtra("deckId", -1)

        //get deck save
        if (deckId != -1) {
            Log.d("MainActivity", "||||| obtained deck id: " + deckId.toString())

            //suspend func in corotuine
            val context = this
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
                {Log.d("MainActivity", "||||| could not load deck save due to database row of index not containing a saved deck")}

                //switch to main thread and update name textview
                withContext(Dispatchers.Main) {
                    val deckNameView = findViewById<EditText>(R.id.deckName)
                    deckNameView.setText(deckName)

                    //display content questions in main after getting content
                    //seperate string with seperators into questions
                    questions = content.split("||").toMutableList()

                    //loop and create ui
                    for (question in questions) {
                        //create edittext view for question
                        var newQuestionView = generateQuestionView(question, context)
                        linearContainer.addView(newQuestionView)
                    }
                }
            }
        }
        else {
            Log.d("MainActivity", "||||| could not load deck save due to incorrectly passed deck id from main activity to edit activity")
        }

        //create add button onclicklistener to add new questions
        val addButton = findViewById<Button>(R.id.addButton)

        addButton.setOnClickListener() {
            var newQuestionView = generateQuestionView("New Question", this)
            linearContainer.addView(newQuestionView)
        }
    }

    //ovveride on pause to save notes
    override fun onPause() {
        super.onPause()

        //save changes to deck save (name and content)
        //get deck name view
        val deckNameView = findViewById<EditText>(R.id.deckName)
        val currentDeckName = deckNameView.text

        //get all question edit texts
        val newQuestions: MutableList<String> = mutableListOf()
        for (i in 1 until linearContainer.childCount) {
            //get view element, get question edit text, get questions edit content and append to list
            val childView = linearContainer.getChildAt(i)
            val newQuestion = childView.findViewById<EditText>(R.id.question)
            newQuestions.add(newQuestion.text.toString())
        }

        //combine content into content string
        val combinedQuestions = newQuestions.joinToString(separator = "||")

        //(apply new content when replacing deck save with new instance with updated content)
        Log.d("MainActivity", "||||| saved changes to deck row: " + combinedQuestions)

        //update db row of id with a new created deck save with corrected name and id
        //suspend func in corotuine
        lifecycleScope.launch(Dispatchers.IO) {
            deckDao.updateDeck(DeckSave(name = currentDeckName.toString(), id = deckId, content = combinedQuestions))
        }
    }

    //----------------------------------------------------------------------------------------------

    //function that removes mobile keyboard display when pressing of of an input field EditText
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    //function that reloads main activity and restarts it instead of resuming it when user presses navigation "back" button
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        //start new main activity
        startActivity(intent)
        //stop this activity
        finish()
    }

    //func to generate question view
    fun generateQuestionView(question: String, context: Context): View {
        //create inflater
        val inflater = LayoutInflater.from(context)

        //instatniate view prefab
        var questionView = inflater.inflate(R.layout.question_element, linearContainer, false)

        //get child edit text view
        val editView = questionView.findViewById<EditText>(R.id.question)

        //apply text
        editView.setText(question)

        //set on update listener to check if question is empty and remove it
        editView.setOnFocusChangeListener {view, hasFocus ->
            if (!hasFocus)
            {
                if (editView.text.toString().trim().isEmpty())
                {
                    Log.d("MainActivity", "||||| removed question note due to empty content")
                    linearContainer.removeView(questionView)
                }
            }
        }

        //return final view
        return questionView
    }
}