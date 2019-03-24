package com.example.crypinfo.models

data class DataResponse(
    val `data`: List<Data>,
    val status: Status
)

data class Data(
    val circulating_supply: Double,
    val cmc_rank: Int,
    val date_added: String,
    val id: Int,
    val last_updated: String,
    val max_supply: Double,
    val name: String,
    val num_market_pairs: Int,
    val platform: Any,
    val quote: Quote,
    val slug: String,
    val symbol: String,
    val tags: List<String>,
    val total_supply: Double
)

data class Quote(
    val BTC: BTC,
    val USD: USD
)

data class BTC(
    val last_updated: String,
    val market_cap: Int,
    val percent_change_1h: Int,
    val percent_change_24h: Int,
    val percent_change_7d: Int,
    val price: Int,
    val volume_24h: Int
)

data class USD(
    val last_updated: String,
    val market_cap: Double,
    val percent_change_1h: Double,
    val percent_change_24h: Double,
    val percent_change_7d: Double,
    val price: Double,
    val volume_24h: Double
)

data class Status(
    val credit_count: Int,
    val elapsed: Int,
    val error_code: Int,
    val error_message: String,
    val timestamp: String
)