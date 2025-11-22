package com.example.smartwallet.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.smartwallet.R
import com.example.smartwallet.data.Transaction
import com.example.smartwallet.viewmodel.TransactionViewModel
import java.util.*

class AddTransactionFragment : Fragment() {

    private lateinit var rgTransactionType: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var etAmount: EditText
    private lateinit var etNote: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var layoutDatePicker: LinearLayout
    private lateinit var rvCategories: RecyclerView
    private lateinit var btnSaveTransaction: MaterialButton
    private lateinit var tvTitle: TextView

    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedCategory: Category? = null

    private var editingTransaction: Transaction? = null
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        arguments?.let { args ->
            val transactionId = args.getInt("transactionId", -1)
            if (transactionId != -1) {
                isEditMode = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

        rgTransactionType = view.findViewById(R.id.rg_transaction_type)
        rbExpense = view.findViewById(R.id.rb_expense)
        rbIncome = view.findViewById(R.id.rb_income)
        etAmount = view.findViewById(R.id.et_amount)
        etNote = view.findViewById(R.id.et_note)
        tvSelectedDate = view.findViewById(R.id.tv_selected_date)
        layoutDatePicker = view.findViewById(R.id.layout_date_picker)
        rvCategories = view.findViewById(R.id.rv_categories)
        btnSaveTransaction = view.findViewById(R.id.btn_save_transaction)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        setupClickListeners()
        updateDateDisplay()

        if (isEditMode) {
            btnSaveTransaction.text = "Update Transaction"
        }
    }

    private fun setupCategories() {
        val expenseCategories = listOf(
            Category("🍔", "Food & Dining"),
            Category("🚗", "Transportation"),
            Category("🏠", "Housing"),
            Category("🛒", "Shopping"),
            Category("💊", "Healthcare"),
            Category("🎬", "Entertainment"),
            Category("📚", "Education"),
            Category("💰", "Others")
        )

        val incomeCategories = listOf(
            Category("💼", "Salary"),
            Category("💸", "Business"),
            Category("🎁", "Gift"),
            Category("📈", "Investment"),
            Category("💵", "Others")
        )

        categoryAdapter = CategoryAdapter(expenseCategories) { category ->
            selectedCategory = category
            categoryAdapter.setSelectedCategory(category)
        }

        rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
        }

        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_expense -> {
                    categoryAdapter.updateCategories(expenseCategories)
                    selectedCategory = null
                }
                R.id.rb_income -> {
                    categoryAdapter.updateCategories(incomeCategories)
                    selectedCategory = null
                }
            }
        }
    }

    private fun setupClickListeners() {
        layoutDatePicker.setOnClickListener {
            showDatePicker()
        }

        btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedTimestamp

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedTimestamp = calendar.timeInMillis
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        tvSelectedDate.text = viewModel.formatDate(selectedTimestamp)
    }

    private fun saveTransaction() {
        val amountText = etAmount.text.toString()
        val note = etNote.text.toString()
        val type = if (rbExpense.isChecked) "EXPENSE" else "INCOME"

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val validationError = viewModel.validateTransaction(
            amountText,
            note,
            selectedCategory!!.name
        )

        if (validationError != null) {
            Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = editingTransaction?.id ?: 0,
            amount = amountText.toDouble(),
            note = note,
            date = selectedTimestamp,
            type = type,
            categoryIcon = selectedCategory!!.icon,
            categoryName = selectedCategory!!.name
        )

        if (isEditMode) {
            viewModel.updateTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction updated!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction saved!", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    data class Category(
        val icon: String,
        val name: String
    )
}