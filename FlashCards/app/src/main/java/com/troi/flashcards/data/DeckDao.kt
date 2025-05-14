package com.troi.flashcards.data

import androidx.room.*

//dao - data acces object, how to acces the decks sql database - interface
@Dao
interface DeckDao {
    @Insert
    suspend fun insertDeck(deck: DeckSave)

    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<DeckSave>

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Int)
}