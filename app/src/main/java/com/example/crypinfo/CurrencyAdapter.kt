package com.example.crypinfo

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.crypinfo.models.Currency
import com.example.crypinfo.models.ExchangeRates
import com.example.crypinfo.models.GraphResponse
import com.example.crypinfo.services.ApiService
import com.example.crypinfo.services.ServiceBuilder
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.currency.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class CurrencyAdapter(
    private val context: Context,
    private val addedCurrencyData: MutableList<Currency>,
    private val addedCurrenciesList: MutableList<String>,
    private val priceCurrency: String,
    private val graphInterval: String,
    private val exchangeRates: MutableMap<String, Double>) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyAdapter.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.currency, parent, false)
        return ViewHolder(view, priceCurrency, exchangeRates)
    }

    override fun getItemCount(): Int {
        return addedCurrencyData.count()
    }

    /**
     * Определяет временные параметры "от" и "до" для запроса данных графика.
     */
    fun setGraphInterval(graphInterval: String): String {
        val zoneOffSet = ZoneOffset.of("Z")
        var from: Long = 0
        var to: Long = LocalDateTime.now().toEpochSecond(zoneOffSet)

        when (graphInterval) {
            "Hour" -> {
                from = to - 3600
            }
            "Day" -> {
                from = to - 86400
            }
            "Week" -> {
                from = to - 604800
            }
            "Month" -> {
                from = to - 2629746
            }
            "Halfyear" -> {
                from = to - 15778476
            }
            "Year" -> {
                from = to - 31556952
            }
            "3 years" -> {
                from = to - 94670856
            } else -> {
                return ""
            }
        }

        val finalFrom: String = from.toString() + "000"
        val finalTo: String = to.toString() + "000"
        return "/$finalFrom/$finalTo/"
    }

    override fun onBindViewHolder(holder: CurrencyAdapter.ViewHolder, position: Int)  {
        holder?.bindCurrency(addedCurrencyData[position], context)
        val chart = holder?.itemView.lineChart

        // Получает данные для графика.
        holder?.itemView.setOnClickListener {
            chart.setNoDataText("Wait for the data to load...")
            chart.setNoDataTextColor(Color.BLACK)
            if (holder?.itemView.lineChart.data == null) {
                val graphApiService: ApiService = ServiceBuilder.buildService(ApiService::class.java, "https://graphs2.coinmarketcap.com/currencies/")
                val graphRequestCall: Call<GraphResponse> = graphApiService.getGraphData(addedCurrencyData[addedCurrencyData.indexOf(addedCurrencyData.find { currency -> currency.name == holder.itemView.currencyTitle.text })].slug + setGraphInterval(graphInterval))
                graphRequestCall.enqueue(object: Callback<GraphResponse> {
                    override fun onResponse(call: Call<GraphResponse>, response: Response<GraphResponse>) {
                        if(response.isSuccessful) {
                            val graphData = response.body()!!.price_usd
                            val entries = mutableListOf<Entry>()
                            val xValues = mutableListOf<Double>()
                            for (i in 0 until graphData.size - 1) {
                                var graphElement = graphData[i].toString()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .replace(",", "")
                                    .split(" ")
                                entries.add(Entry(graphElement[0].toFloat(), holder?.exchangeCurrency(priceCurrency, graphElement[1].toDouble(), exchangeRates).toFloat()))
                                xValues.add(graphElement[0].toDouble())
                            }

                            // Настраивает отображение графика.
                            val dataSet = LineDataSet(entries, "Values")
                            dataSet.setColor(Color.BLACK)
                            dataSet.setDrawCircles(false)
                            dataSet.setDrawValues(false)
                            dataSet.setValueTextSize(20.toFloat())
                            val data = LineData(dataSet)
                            val xAxis: XAxis = chart.xAxis;
                            xAxis.setValueFormatter(MyXAxisValueFormatter(xValues, graphInterval))
                            chart.data = data
                            chart.setNoDataText("Wait for the data to load...")
                            chart.setNoDataTextColor(Color.BLACK)
                            chart.rendererXAxis.transformer
                            chart.description.isEnabled = false
                            chart.legend.isEnabled = false
                            chart.invalidate()
                        }
                    }

                    override fun onFailure(call: Call<GraphResponse>, t: Throwable) {
                        println(t)
                    }
                })
            }

            // Открывает / закрывает график.
            var params: RelativeLayout.LayoutParams
            if(holder?.isShown) {
                params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0)
                params.addRule(RelativeLayout.BELOW, R.id.currencyLogo)
                params.topMargin = 20
            } else {
                params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 400)
                params.addRule(RelativeLayout.BELOW, R.id.currencyLogo)
                params.topMargin = 18
            }
            holder?.itemView.lineChart.setLayoutParams(params)
            holder?.isShown = !holder?.isShown
        }
    }

    /**
     * Удаляет выбранную криптовалюту из списка.
     */
    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        addedCurrencyData.removeAt(viewHolder.adapterPosition)
        addedCurrenciesList.remove(viewHolder.itemView.currencySymbol.text)
        notifyItemRemoved(viewHolder.adapterPosition)
    }

    /**
     * Приводит временные данные в нужный формат в зависимости от выбранного интервала.
     */
    class MyXAxisValueFormatter(private val values: MutableList<Double>, private val graphInterval: String) : IAxisValueFormatter {
        lateinit var SDF: SimpleDateFormat

        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            lateinit var format: String
            when (graphInterval) {
                "Hour" -> {
                    format = "HH:mm"
                }
                "Day" -> {
                    format = "HH:mm"
                }
                "Week" -> {
                    format = "EEE dd"
                }
                "Month" -> {
                    format = "dd MMM"
                } else -> {
                    format = "MMM yyyy"
                }
            }
            SDF = SimpleDateFormat(format)
            return SDF.format(value)
        }
    }

    class ViewHolder(itemView: View, private val priceCurrency: String, private val exchangeRates: MutableMap<String, Double>) : RecyclerView.ViewHolder(itemView) {
        private val currencyTitle = itemView?.findViewById<TextView>(R.id.currencyTitle)
        private val currencyPrice = itemView?.findViewById<TextView>(R.id.currencyPrice)
        private val currencyLogo = itemView?.findViewById<ImageView>(R.id.currencyLogo)
        private val currencySymbol = itemView?.findViewById<TextView>(R.id.currencySymbol)

        var isShown: Boolean = false

        // API Endpoint для логотипа криптовалюты.
        private val imageApiUrl: String = "https://chasing-coins.com/api/v1/std/logo/"

        // Форматирует прайс валюты в нужный формат.
        private fun customFormat(pattern: String, value: Double): String {
            val myFormatter = DecimalFormat(pattern)
            return myFormatter.format(value)
        }

        // Возвращает знак выбранной валюты прайса.
        private fun currencySign(priceCurrency: String): String {
            var sign = "$"
            when (priceCurrency) {
                "USD" ->  { sign = "$" }
                "EUR" ->  { sign =  "€" }
                "RUB" ->  { sign =  "\u20BD" }
            }
            return sign
        }

        // Возвращает прайс криптовалюты в зависимости от выбранной валюты.
        fun exchangeCurrency(priceCurrency: String, currencyPrice: Double, exchangeRates: MutableMap<String, Double>): Double {
            var price: Double = currencyPrice
            when (priceCurrency) {
                "USD" -> { price = currencyPrice}
                "EUR" -> { price =  currencyPrice / exchangeRates["USD"]!!}
                "RUB" -> { price =  (currencyPrice / exchangeRates["USD"]!!) * exchangeRates["RUB"]!!}
            }
            return price
        }

        fun bindCurrency(currency: Currency, context: Context) {
            // Устанавливает паттерн для отображения прайса.
            val pattern: String = if(exchangeCurrency(priceCurrency, currency.price, exchangeRates) < 1) "#.######" else "#.##"

            // Устанавливает цвет прайса, в зависимости от изменения за сутки.
            val priceColor = if(currency.percent_change_24h < 0) context.getColor(R.color.red) else context.getColor(R.color.green)

            currencyTitle?.text = currency.name
            currencySymbol?.text = currency.symbol
            currencyPrice?.text = currencySign(priceCurrency) + customFormat(pattern, exchangeCurrency(priceCurrency, currency.price, exchangeRates)).replace(".", ",") + " " + priceCurrency
            currencyPrice?.setTextColor(priceColor)

            // Загружает логотип криптовалюты в ImageView.
            Picasso.get()
                .load(imageApiUrl + currency.symbol)
                .into(currencyLogo)
        }
    }

}
