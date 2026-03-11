package com.example.spendsense.model

enum class TransactionType { INCOME, EXPENSE }

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // Required for Firestore deserialization
    constructor() : this("", "", 0.0, TransactionType.EXPENSE, "", 0L)
}
