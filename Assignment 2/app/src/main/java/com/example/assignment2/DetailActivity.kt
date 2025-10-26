package com.example.assignment2

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment2.databinding.ActivityDetailBinding
import com.example.assignment2.model.Instrument
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.chip.Chip

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var instrument: Instrument? = null

    companion object {
        const val RESULT_INSTRUMENT_ID = "result_instrument_id"
        const val RESULT_BOOKING_SUMMARY = "result_booking_summary"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instrument = intent.getParcelableExtra(MainActivity.EXTRA_INSTRUMENT)
        if (instrument == null) {
            finish()
            return
        }

        bindData()
        setupListeners()
    }

    private fun bindData() {
        instrument?.let { inst ->
            binding.imageInstrumentDetail.setImageResource(inst.drawableRes)
            binding.tvNameDetail.text = inst.name
            binding.ratingBarDetail.rating = inst.rating

            binding.tvPriceDetail.text = getString(R.string.price_per_month, inst.pricePerMonth)

            binding.chipGroupDetail.removeAllViews()
            inst.options.forEach { opt ->
                val chip = Chip(this).apply {
                    text = opt
                    isCheckable = true
                }
                binding.chipGroupDetail.addView(chip)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (validate()) {
                val renter = binding.etRenterName.text.toString().trim()
                val months = binding.etMonths.text.toString().toInt()
                val inst = instrument!!
                val summary = "Booked by $renter for $months months — ${inst.pricePerMonth} credit/month"

                val result = intent
                result.putExtra(RESULT_INSTRUMENT_ID, inst.id)
                result.putExtra(RESULT_BOOKING_SUMMARY, summary)
                result.putExtra("result_rating", binding.ratingBarDetail.rating)


                setResult(Activity.RESULT_OK, result)
                Snackbar.make(binding.root, "Saved", Snackbar.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun validate(): Boolean {
        val renter = binding.etRenterName.text.toString().trim()
        if (renter.isEmpty()) {
            binding.etRenterName.error = "Renter name required"
            return false
        }

        val monthsStr = binding.etMonths.text.toString().trim()
        val months = monthsStr.toIntOrNull()
        if (months == null || months <= 0) {
            binding.etMonths.error = "Enter months > 0"
            return false
        }

        val inst = instrument!!
        val maxCredit = 500
        val totalCost = months * inst.pricePerMonth
        if (totalCost > maxCredit) {
            binding.etMonths.error = "Cost $totalCost exceeds credit limit ($maxCredit)"
            return false
        }

        return true
    }
}
