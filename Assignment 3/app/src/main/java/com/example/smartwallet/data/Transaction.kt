package com.example.smartwallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val note: String,
    val date: Long,
    val type: String,
    val categoryIcon: String,
    val categoryName: String
)