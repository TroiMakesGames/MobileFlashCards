package com.troi.flashcards.data

import androidx.room.Entity
import androidx.room.PrimaryKey

//create database table
@Entity(tableName = "decks")
data class DeckSave(
    //autogenerate decks id
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val name: String
)