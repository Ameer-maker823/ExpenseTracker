package com.group4.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group4.expensetracker.databinding.ActivityMainBinding
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.group4.expensetracker.data.AppDatabase
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter = ExpenseAdapter { expense ->
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        val dao = AppDatabase.getInstance(applicationContext).expenseDao()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    dao.getAllExpenses().collect { expenses ->
                        adapter.submitList(expenses)
                        binding.tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    dao.getTotalSpending().collect { total ->
                        binding.tvTotal.text = String.format(Locale.getDefault(), "%.2f", total ?: 0.0)
                    }
                }
            }
        }
    }
}