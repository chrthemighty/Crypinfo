package com.example.crypinfo.services

import com.example.crypinfo.models.DataResponse
import com.example.crypinfo.models.ExchangeRates
import com.example.crypinfo.models.GraphResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface ApiService {
    /**
     * Получает данные о криптовалютах.
     */
    @Headers("X-CMC_PRO_API_KEY: 9d02978e-4e09-4385-8845-b3c2fe291a45")
    @GET("/v1/cryptocurrency/listings/latest")
    fun getCurrencies(): Call<DataResponse>

    /**
     * Получает данные о курсе валют.
     */
    @GET
    fun getExchangeRates(@Url url: String): Call<ExchangeRates>

    /**
     * Получает данные для графика криптовалюты.
     */
    @GET
    fun getGraphData(@Url url: String): Call<GraphResponse>
}