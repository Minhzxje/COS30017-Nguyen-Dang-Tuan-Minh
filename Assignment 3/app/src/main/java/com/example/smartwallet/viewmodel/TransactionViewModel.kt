package com.example.smartwallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartwallet.data.AppDatabase
import com.example.smartwallet.data.Transaction
import com.example.smartwallet.data.TransactionRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
    }

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions
    val recentTransactions: LiveData<List<Transaction>> = repository.recentTransactions
    val totalIncome: LiveData<Double> = repository.totalIncome
    val totalExpense: LiveData<Double> = repository.totalExpense

    val currentBalance: MediatorLiveData<Double> = MediatorLiveData()

    init {
        currentBalance.addSource(totalIncome) { income ->
            val expense = totalExpense.value ?: 0.0
            currentBalance.value = income - expense
        }

        currentBalance.addSource(totalExpense) { expense ->
            val income = totalIncome.value ?: 0.0
            currentBalance.value = income - expense
        }
    }

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insert(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.update(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun getFilteredTransactions(type: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    // NEW: Get transaction by ID for editing
    fun getTransactionById(id: Int, onResult: (Transaction?) -> Unit) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(id)
            onResult(transaction)
        }
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun validateTransaction(amount: String, note: String, categoryName: String): String? {
        return when {
            amount.isBlank() -> "Amount cannot be empty"
            amount.toDoubleOrNull() == null -> "Invalid amount format"
            amount.toDouble() <= 0 -> "Amount must be greater than zero"
            note.isBlank() -> "Note cannot be empty"
            categoryName.isBlank() -> "Please select a category"
            else -> null
        }
    }

    fun calculatePercentage(categoryAmount: Double, total: Double): Int {
        return if (total > 0) {
            ((categoryAmount / total) * 100).toInt()
        } else {
            0
        }
    }
}