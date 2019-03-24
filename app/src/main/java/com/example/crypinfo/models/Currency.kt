package com.example.crypinfo.models

data class Currency(
    val name: String,
    val slug: String,
    val price: Double,
    val symbol: String,
    val percent_change_24h: Double
)