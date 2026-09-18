package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // "DEBIT" or "CREDIT"
    val amount: Double,
    val recipientBank: String,
    val recipientAccountNumber: String,
    val recipientName: String,
    val senderBank: String,
    val senderAccountNumber: String,
    val senderName: String,
    val narration: String,
    val balanceAfter: Double,
    val reference: String,
    val timestamp: Long = System.currentTimeMillis(),
    val smsBody: String,
    val smsSenderHeader: String
)
