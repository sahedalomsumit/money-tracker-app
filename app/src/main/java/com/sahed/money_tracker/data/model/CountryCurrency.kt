package com.sahed.money_tracker.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CountryCurrency(
    val countryCode: String,
    val countryName: String,
    val flag: String,
    val defaultCurrencyCode: String,
    val defaultCurrencySymbol: String
)

@Immutable
data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)
