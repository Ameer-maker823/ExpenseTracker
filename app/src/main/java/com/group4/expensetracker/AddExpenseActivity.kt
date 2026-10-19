package com.group4.expensetracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.group4.expensetracker.data.AppDatabase
import com.group4.expensetracker.data.Expense
import com.group4.expensetracker.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var selectedDate: Long = System.currentTimeMillis()

    private var editingExpense: Expense? = null
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.etDate.setText(dateFormat.format(Date(selectedDate)))
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.btnSave.setOnClickListener { saveExpense() }

        val expenseId = intent.getIntExtra(EXTRA_EXPENSE_ID, -1)
        if (expenseId != -1) loadExpense(expenseId)
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(selectedDate)
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            selectedDate = millis
            binding.etDate.setText(dateFormat.format(Date(millis)))
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    private fun saveExpense() {
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid amount"
            return
        }

        val expense = Expense(
            id = editingExpense?.id ?: 0,
            amount = amount,
            category = binding.spCategory.selectedItem.toString(),
            description = binding.etDescription.text.toString().trim(),
            date = selectedDate
        )

        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).expenseDao()
            if (editingExpense != null) {
                dao.update(expense)
                Toast.makeText(this@AddExpenseActivity, "Expense updated", Toast.LENGTH_SHORT).show()
            } else {
                dao.insert(expense)
                Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun loadExpense(id: Int) {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).expenseDao()
            val expense = dao.getExpenseById(id) ?: return@launch
            editingExpense = expense
            selectedDate = expense.date
            binding.etAmount.setText(expense.amount.toString())
            val categories = resources.getStringArray(R.array.categories)
            binding.spCategory.setSelection(categories.indexOf(expense.category).coerceAtLeast(0))
            binding.etDescription.setText(expense.description)
            binding.etDate.setText(dateFormat.format(Date(expense.date)))
            binding.btnSave.setText(R.string.btn_update)
        }
    }
    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
    }
}