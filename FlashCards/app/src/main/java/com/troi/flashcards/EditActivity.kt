package com.troi.flashcards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editlayout)

        //initialize database and dao
        appDatabase = AppDatabase.getDatabase((applicationContext))
        deckDao = appDatabase.deckDao()

        //init vars and get deck id
        deckId = intent.getIntExtra("deckId", -1)

        //get deck save
        if (deckId != -1) {
            Log.d("MainActivity", "||||| obtained deck id: " + deckId.toString())

            //suspend func in corotuine
            lifecycleScope.launch(Dispatchers.IO) {
                //get deck save by id
                val deckSave = deckDao.getDeckById(deckId)

                //if correctly got decksave, sync name var to saved name
                if (deckSave != null)
                { deckName = deckSave.name }
                else
                {Log.d("MainActivity", "||||| could not load deck save due to database row of index not containing a saved deck")}

                //switch to main thread and update name textview
                withContext(Dispatchers.Main) {
                    val deckNameView = findViewById<EditText>(R.id.deckName)
                    deckNameView.setText(deckName)
                }
            }
        }
        else {
            Log.d("MainActivity", "||||| could not load deck save due to incorrectly passed deck id from main activity to edit activity")
        }
    }

    //ovveride on pause to save notes
    override fun onPause() {
        super.onPause()

        //save name to deck save
        //get deck name view
        val deckNameView = findViewById<EditText>(R.id.deckName)
        val currentDeckName = deckNameView.text

        //update db row of id with a new created deck save with corrected name and id
        //suspend func in corotuine
        lifecycleScope.launch(Dispatchers.IO) {
            deckDao.updateDeck(DeckSave(name = currentDeckName.toString(), id = deckId))
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
}