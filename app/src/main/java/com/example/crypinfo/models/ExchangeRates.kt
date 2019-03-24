package com.example.crypinfo.models

data class ExchangeRates(
    val base: String,
    val date: String,
    val rates: Rates,
    val success: Boolean,
    val timestamp: Int
)

data class Rates(
    val RUB: Double,
    val USD: Double
)