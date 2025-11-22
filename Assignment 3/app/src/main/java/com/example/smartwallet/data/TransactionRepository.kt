package com.example.smartwallet.data

import androidx.lifecycle.LiveData

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val recentTransactions: LiveData<List<Transaction>> = transactionDao.getRecentTransactions()
    val totalIncome: LiveData<Double> = transactionDao.getTotalIncome()
    val totalExpense: LiveData<Double> = transactionDao.getTotalExpense()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTransactionsByType(type: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }
}