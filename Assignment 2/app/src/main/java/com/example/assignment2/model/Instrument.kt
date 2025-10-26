package com.example.assignment2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Instrument(
    val id: Int,
    val name: String,
    var rating: Float,
    val options: List<String>,
    val pricePerMonth: Int,
    val drawableRes: Int
) : Parcelable
