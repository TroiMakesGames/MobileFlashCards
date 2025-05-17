package com.troi.flashcards.data

import android.bluetooth.le.AdvertisingSetParameters
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeckSave::class], version = 2, exportSchema = false)      //database that has deck type entitites
abstract class AppDatabase: RoomDatabase() {

    //acces deck dao interface
    abstract fun deckDao(): DeckDao

    //makes only one database exist (companion object, volatile, synchronised)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashcards_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}