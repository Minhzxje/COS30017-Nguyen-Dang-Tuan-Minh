package com.example.assignment2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment2.databinding.ActivityMainBinding
import com.example.assignment2.model.Instrument
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import androidx.activity.result.contract.ActivityResultContracts
import android.view.animation.AlphaAnimation

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val instruments = mutableListOf<Instrument>()
    private var currentIndex = 0
    private val bookings = mutableMapOf<Int, String>()

    private val bookingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val instId = result.data!!.getIntExtra(DetailActivity.RESULT_INSTRUMENT_ID, -1)
                val bookingSummary =
                    result.data!!.getStringExtra(DetailActivity.RESULT_BOOKING_SUMMARY)
                val newRating =
                    result.data!!.getFloatExtra("result_rating", -1f)

                if (instId != -1 && bookingSummary != null) {
                    bookings[instId] = bookingSummary

                    val instrument = instruments.find { it.id == instId }
                    if (instrument != null && newRating >= 0f) {
                        instrument.rating = newRating
                    }

                    Snackbar.make(binding.root, "Booking saved", Snackbar.LENGTH_SHORT).show()
                    showInstrument(currentIndex)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Snackbar.make(binding.root, "Booking cancelled", Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSampleData()
        setupUi()
        showInstrument(currentIndex)
    }

    private fun setupSampleData() {
        instruments.add(
            Instrument(
                id = 1,
                name = "Acoustic Guitar",
                rating = 4.5f,
                options = listOf("Case", "Strap", "Extra Strings"),
                pricePerMonth = 30,
                drawableRes = R.drawable.guitar
            )
        )
        instruments.add(
            Instrument(
                id = 2,
                name = "Digital Keyboard",
                rating = 4.0f,
                options = listOf("Stand", "Pedal", "Bench"),
                pricePerMonth = 50,
                drawableRes = R.drawable.keyboard
            )
        )
        instruments.add(
            Instrument(
                id = 3,
                name = "Drum Kit",
                rating = 3.8f,
                options = listOf("Sticks", "Throne", "Cases"),
                pricePerMonth = 70,
                drawableRes = R.drawable.drum
            )
        )
    }

    private fun setupUi() {
        binding.btnNext.setOnClickListener {
            currentIndex = (currentIndex + 1) % instruments.size
            showInstrument(currentIndex)
        }

        binding.btnBorrow.setOnClickListener {
            val inst = instruments[currentIndex]
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(EXTRA_INSTRUMENT, inst)
            bookingLauncher.launch(intent)
        }
    }

    private fun showInstrument(index: Int) {
        val instrument = instruments[index]

        val fadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 400
        }
        binding.imageInstrument.startAnimation(fadeAnim)
        binding.tvName.startAnimation(fadeAnim)
        binding.ratingBar.startAnimation(fadeAnim)
        binding.tvPrice.startAnimation(fadeAnim)

        binding.imageInstrument.setImageResource(instrument.drawableRes)
        binding.tvName.text = instrument.name
        binding.ratingBar.rating = instrument.rating
        binding.tvPrice.text = getString(R.string.price_per_month, instrument.pricePerMonth)

        binding.chipGroupOptions.removeAllViews()
        instrument.options.forEach { opt ->
            val chip = Chip(this).apply {
                text = opt
                isCheckable = true
            }
            binding.chipGroupOptions.addView(chip)
        }

        val bookingSummary = bookings[instrument.id]
        if (bookingSummary != null) {
            binding.tvStatus.text = bookingSummary
            binding.tvStatus.setTextAppearance(R.style.BookedStatus)

            binding.btnBorrow.isEnabled = false
            binding.btnBorrow.text = "Borrowed"
            binding.btnBorrow.alpha = 0.6f
        } else {
            binding.tvStatus.text = getString(R.string.status_available)
            binding.tvStatus.setTextAppearance(R.style.AvailableStatus)

            binding.btnBorrow.isEnabled = true
            binding.btnBorrow.text = "Borrow"
            binding.btnBorrow.alpha = 1f
        }
    }

    companion object {
        const val EXTRA_INSTRUMENT = "extra_instrument"
    }
}
