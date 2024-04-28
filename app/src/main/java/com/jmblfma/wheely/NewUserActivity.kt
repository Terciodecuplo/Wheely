package com.jmblfma.wheely

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmblfma.wheely.databinding.NewUserLayoutBinding
import java.util.Calendar
import java.util.Locale

class NewUserActivity:AppCompatActivity() {
    private lateinit var binding: NewUserLayoutBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = NewUserLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarNewUser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.userBirthdayEdittext.setOnClickListener{
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                binding.userBirthdayEdittext.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

}