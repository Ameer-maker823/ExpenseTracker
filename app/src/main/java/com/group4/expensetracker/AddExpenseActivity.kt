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
            amount = amount,
            category = binding.spCategory.selectedItem.toString(),
            description = binding.etDescription.text.toString().trim(),
            date = selectedDate
        )

        lifecycleScope.launch {
            AppDatabase.getInstance(applicationContext).expenseDao().insert(expense)
            Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}