package com.example.smartwallet.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.smartwallet.R
import com.example.smartwallet.data.Transaction
import com.example.smartwallet.viewmodel.TransactionViewModel
import java.text.DecimalFormat
import java.text.NumberFormat
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

    private lateinit var viewModel: TransactionViewModel
    private lateinit var categoryAdapter: CategoryAdapter

    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedCategory: Category? = null

    private var editingTransaction: Transaction? = null
    private var isEditMode: Boolean = false

    private var isFormatting: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val transactionId = arguments?.getInt("transactionId", -1) ?: -1
        if (transactionId != -1) {
            isEditMode = true
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

        setupAmountFormatting()
        setupCategories()
        setupClickListeners()
        updateDateDisplay()

        if (isEditMode) {
            btnSaveTransaction.text = getString(R.string.update_transaction)
            loadTransactionForEditing(arguments?.getInt("transactionId", -1) ?: -1)
        } else {
            btnSaveTransaction.text = getString(R.string.save_transaction)
        }
    }

    private fun setupAmountFormatting() {
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                val originalString = s.toString().replace(".", "")
                if (originalString.isNotEmpty()) {
                    try {
                        val number = originalString.toLong()
                        val formatted = formatNumber(number)


                        etAmount.removeTextChangedListener(this)
                        etAmount.setText(formatted)
                        etAmount.setSelection(formatted.length)
                        etAmount.addTextChangedListener(this)
                    } catch (e: NumberFormatException) {
                        etAmount.removeTextChangedListener(this)
                        etAmount.setText("")
                        etAmount.addTextChangedListener(this)
                    }
                }

                isFormatting = false
            }
        })
    }

    private fun formatNumber(number: Long): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN")) as DecimalFormat
        formatter.applyPattern("#,###")
        return formatter.format(number)
    }

    private fun parseFormattedNumber(formattedNumber: String): Double {
        val cleanString = formattedNumber.replace(".", "")
        return if (cleanString.isNotEmpty()) {
            cleanString.toDouble()
        } else {
            0.0
        }
    }

    private fun loadTransactionForEditing(transactionId: Int) {
        viewModel.getTransactionById(transactionId) { transaction ->
            transaction?.let {
                editingTransaction = it
                populateForm(it)
            }
        }
    }

    private fun populateForm(transaction: Transaction) {
        // Set amount - format with thousand separators
        val amountText = if (transaction.amount % 1 == 0.0) {
            formatNumber(transaction.amount.toLong())
        } else {
            formatNumber(transaction.amount.toLong())
        }
        etAmount.setText(amountText)

        // Set note
        etNote.setText(transaction.note)

        // Set date
        selectedTimestamp = transaction.date
        updateDateDisplay()

        // Set transaction type
        if (transaction.type == "INCOME") {
            rbIncome.isChecked = true
        } else {
            rbExpense.isChecked = true
        }

        val targetCategory = Category(transaction.categoryIcon, transaction.categoryName)
        selectedCategory = targetCategory

        if (transaction.type == "INCOME") {
            categoryAdapter.updateCategories(incomeCategories)
        } else {
            categoryAdapter.updateCategories(expenseCategories)
        }

        val currentCategories = if (transaction.type == "INCOME") incomeCategories else expenseCategories
        val categoryPosition = currentCategories.indexOfFirst {
            it.name == transaction.categoryName && it.icon == transaction.categoryIcon
        }

        if (categoryPosition != -1) {
            categoryAdapter.setSelectedCategory(currentCategories[categoryPosition])
        }
    }

    private fun setupCategories() {
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
                    // Reselect category if editing and it matches the type
                    editingTransaction?.let { transaction ->
                        if (transaction.type == "EXPENSE") {
                            val targetCategory = Category(transaction.categoryIcon, transaction.categoryName)
                            selectedCategory = targetCategory
                            val categoryPosition = expenseCategories.indexOfFirst {
                                it.name == transaction.categoryName && it.icon == transaction.categoryIcon
                            }
                            if (categoryPosition != -1) {
                                categoryAdapter.setSelectedCategory(expenseCategories[categoryPosition])
                            }
                        } else {
                            selectedCategory = null
                        }
                    } ?: run {
                        selectedCategory = null
                    }
                }
                R.id.rb_income -> {
                    categoryAdapter.updateCategories(incomeCategories)
                    // Reselect category if editing and it matches the type
                    editingTransaction?.let { transaction ->
                        if (transaction.type == "INCOME") {
                            val targetCategory = Category(transaction.categoryIcon, transaction.categoryName)
                            selectedCategory = targetCategory
                            val categoryPosition = incomeCategories.indexOfFirst {
                                it.name == transaction.categoryName && it.icon == transaction.categoryIcon
                            }
                            if (categoryPosition != -1) {
                                categoryAdapter.setSelectedCategory(incomeCategories[categoryPosition])
                            }
                        } else {
                            selectedCategory = null
                        }
                    } ?: run {
                        selectedCategory = null
                    }
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
        val amountText = etAmount.text.toString().replace(".", "") // Remove formatting for parsing
        val note = etNote.text.toString()
        val type = if (rbExpense.isChecked) "EXPENSE" else "INCOME"

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), getString(R.string.please_select_category), Toast.LENGTH_SHORT).show()
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

        val amount = try {
            if (amountText.isNotEmpty()) {
                amountText.toDouble()
            } else {
                0.0
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = editingTransaction?.id ?: 0,
            amount = amount,
            note = note,
            date = selectedTimestamp,
            type = type,
            categoryIcon = selectedCategory!!.icon,
            categoryName = selectedCategory!!.name
        )

        if (isEditMode) {
            viewModel.updateTransaction(transaction)
            Toast.makeText(requireContext(), getString(R.string.transaction_updated), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertTransaction(transaction)
            Toast.makeText(requireContext(), getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    companion object {
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
    }

    data class Category(
        val icon: String,
        val name: String
    )
}