package com.example.smartwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.smartwallet.R
import com.example.smartwallet.viewmodel.TransactionViewModel

class DashboardFragment : Fragment() {

    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvSeeAll: TextView
    private lateinit var rvRecentTransactions: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddTransaction: FloatingActionButton

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
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvCurrentBalance = view.findViewById(R.id.tv_current_balance)
        tvTotalIncome = view.findViewById(R.id.tv_total_income)
        tvTotalExpense = view.findViewById(R.id.tv_total_expense)
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvSeeAll = view.findViewById(R.id.tv_see_all)
        rvRecentTransactions = view.findViewById(R.id.rv_recent_transactions)
        layoutEmptyState = view.findViewById(R.id.layout_empty_state)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setGreeting()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
            },
            formatCurrency = { amount ->
                viewModel.formatCurrency(amount)
            }
        )

        rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add_transaction)
        }

        tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_all_transactions)
        }
    }

    private fun observeViewModel() {
        viewModel.currentBalance.observe(viewLifecycleOwner) { balance ->
            tvCurrentBalance.text = viewModel.formatCurrency(balance)
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            tvTotalIncome.text = viewModel.formatCurrency(income)
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            tvTotalExpense.text = viewModel.formatCurrency(expense)
        }

        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                rvRecentTransactions.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                rvRecentTransactions.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
                transactionAdapter.submitList(transactions)
            }
        }
    }

    private fun setGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning!"
            in 12..17 -> "Good afternoon!"
            else -> "Good evening!"
        }
        tvGreeting.text = greeting
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvRecentTransactions.adapter = null
    }
}