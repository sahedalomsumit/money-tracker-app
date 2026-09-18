package com.sahed.money_tracker.data.model

data class CountryCurrency(
    val countryCode: String,
    val countryName: String,
    val flag: String,
    val defaultCurrencyCode: String,
    val defaultCurrencySymbol: String
)

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)
