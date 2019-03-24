package com.example.crypinfo.models

data class GraphResponse(
    val market_cap_by_available_supply: List<Any>,
    val price_btc: List<Any>,
    val price_usd: List<Any>,
    val volume_usd: List<Any>
)