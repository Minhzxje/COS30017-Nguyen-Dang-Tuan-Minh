package com.example.smartwallet.ui

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.example.smartwallet.R
import com.example.smartwallet.data.Transaction
import com.example.smartwallet.viewmodel.TransactionViewModel
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class AllTransactionsFragment : Fragment() {

    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipIncome: Chip
    private lateinit var chipExpense: Chip
    private lateinit var rvAllTransactions: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var viewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_transactions, container, false)

        chipGroupFilter = view.findViewById(R.id.chip_group_filter)
        chipAll = view.findViewById(R.id.chip_all)
        chipIncome = view.findViewById(R.id.chip_income)
        chipExpense = view.findViewById(R.id.chip_expense)
        rvAllTransactions = view.findViewById(R.id.rv_all_transactions)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        setupFilterChips()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
            },
            formatCurrency = { amount ->
                viewModel.formatCurrency(amount)
            }
        )

        rvAllTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val transaction = transactionAdapter.currentList[position]

                viewModel.deleteTransaction(transaction)

                Snackbar.make(
                    rvAllTransactions,
                    "Transaction deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("UNDO") {
                    viewModel.insertTransaction(transaction)
                }.show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                    )
                    .addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvAllTransactions)
    }

    private fun setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chip_income) -> {
                    observeFilteredTransactions("INCOME")
                }
                checkedIds.contains(R.id.chip_expense) -> {
                    observeFilteredTransactions("EXPENSE")
                }
                else -> {
                    observeTransactions()
                }
            }
        }
    }

    private fun observeTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            updateUI(transactions)
        }
    }

    private fun observeFilteredTransactions(type: String) {
        viewModel.allTransactions.removeObservers(viewLifecycleOwner)

        viewModel.getFilteredTransactions(type).observe(viewLifecycleOwner) { transactions ->
            updateUI(transactions)
        }
    }


    private fun updateUI(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            rvAllTransactions.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvAllTransactions.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            transactionAdapter.submitList(transactions)
        }
    }
}