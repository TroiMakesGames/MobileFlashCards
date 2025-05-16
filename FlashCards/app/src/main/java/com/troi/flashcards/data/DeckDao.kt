package com.troi.flashcards.data

import androidx.room.*

//dao - data acces object, how to acces the decks sql database - interface
@Dao
interface DeckDao {
    @Insert
    suspend fun insertDeck(deck: DeckSave): Long

    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<DeckSave>

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Int)

    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckById(deckId: Int): DeckSave?

    @Update
    suspend fun updateDeck(deck: DeckSave)

    //hard database reset
    @Query("DELETE FROM decks")
    suspend fun clearAllDecks()
}