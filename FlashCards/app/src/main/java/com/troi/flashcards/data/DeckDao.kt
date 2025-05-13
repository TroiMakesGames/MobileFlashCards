package com.troi.flashcards.data

import androidx.room.*

//dao - data acces object, how to acces the decks sql database - interface
@Dao
interface DeckDao {
    @Insert
    suspend fun insertDeck(deck: Deck)

    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<Deck>

    @Query("DELETE FROM decks WHERE id = :deckId")
    suspend fun deleteDeckById(deckId: Int)
}