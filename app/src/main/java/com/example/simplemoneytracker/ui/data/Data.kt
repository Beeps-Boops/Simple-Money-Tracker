package com.example.simplemoneytracker.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.icu.util.Calendar

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val month: Int,
    val year: Int,
    val isABill: Boolean
)


@Entity(tableName = "source")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val month: Int,
    val year: Int,
    val repeats: Int,
    var amount: Double,
    val originalAmount: Double,
    val date: Long,
    var lastUpdated: Long,
    val categoryId: Int,
)
