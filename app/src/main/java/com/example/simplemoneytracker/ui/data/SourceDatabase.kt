package com.example.simplemoneytracker.ui.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Source::class], version = 7, exportSchema = false)
abstract class SourceDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile
        private var Instance: SourceDatabase? = null

        fun getDatabase(context: Context): SourceDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SourceDatabase::class.java, "source_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}