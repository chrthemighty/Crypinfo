package com.example.crypinfo.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    fun <T> buildService(serviceType: Class<T>, apiBaseUrl: String): T {
        var baseUrl: String = apiBaseUrl
        val okHttp: OkHttpClient.Builder = OkHttpClient.Builder()
        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp.build())
        val retrofit: Retrofit = builder.build()
        return retrofit.create(serviceType)
    }
}