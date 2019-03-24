package com.example.crypinfo

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.ArrayAdapter
import com.example.crypinfo.dialogs.SettingsDialog
import com.example.crypinfo.models.Currency
import com.example.crypinfo.models.DataResponse
import com.example.crypinfo.models.ExchangeRates
import com.example.crypinfo.services.ApiService
import com.example.crypinfo.services.ServiceBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.currency.view.*


class MainActivity : AppCompatActivity(), SettingsDialog.OnCompleteListener {

    /**
     * Обновляет значение настроек при изменении.
     */
    override fun onComplete(graphInterval: String, priceCurrency: String) {
        this.graphInterval = graphInterval
        this.priceCurrency = priceCurrency
        pref.edit().putString("priceCurrency", this.priceCurrency).commit()
        pref.edit().putString("graphInterval", this.graphInterval).commit()
        updateCurrencies()
    }

    /**
     * Список названий всех криптовалют для поиска.
     */
    var allCurrenciesList = mutableListOf<String>()

    /**
     * Список данных о всех криптовалютах.
     */
    var allCurrenciesData = mutableListOf<Currency>()

    lateinit var graphInterval: String
    lateinit var priceCurrency: String
    lateinit var pref: SharedPreferences
    lateinit var adapter: CurrencyAdapter
    lateinit var deleteIcon: Drawable

    /**
     * Данные о курсе валют.
     */
    var exchangeRates = mutableMapOf(
        "USD" to 0.0,
        "RUB" to 0.0
    )

    /**
     * Список символов добавленных криптовалют.
     */
    var addedCurrenciesList = mutableListOf<String>()

    /**
     * Список данных о добавленных криптовалютах.
     */
    var addedCurrenciesData = mutableListOf<Currency>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получает данные из SharedPreferences.
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        priceCurrency = pref.getString("priceCurrency", "USD")
        graphInterval = pref.getString("graphInterval", "All time")

        // Добавляет BTC, LTC, ETH при первом запуске или очистке SharedPreferences.
        if (pref.all.isEmpty()) {
            addedCurrenciesList.add("BTC")
            pref.edit().putString("BTC", "BTC").commit()
            addedCurrenciesList.add("LTC")
            pref.edit().putString("LTC", "LTC").commit()
            addedCurrenciesList.add("ETH")
            pref.edit().putString("ETH", "ETH").commit()
        } else {
            // Добавляет символы добавленных валют, если они есть в SharedPreferences.
            pref.all.forEach{
                if (it.key != "graphInterval" && it.key != "priceCurrency") {
                    addedCurrenciesList.add(it.key)
                }
            }
        }

        setContentView(R.layout.activity_main)

        // Обновляет данные о криптовалютах при свайпе.
        swipeRefresher.setColorSchemeResources(R.color.primaryColor)
        swipeRefresher.setOnRefreshListener {
            loadCurrencies()
        }

        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_trash)!!
        loadCurrencies()

        // Добавляет выбранную криптовалюту в список и SharedPreferences.
        addButton.setOnClickListener(View.OnClickListener {
            addedCurrenciesList.add(searchInput.text.toString().split(" ")[0].trim())
            pref.edit().putString(searchInput.text.toString().split(" ")[0].trim(), searchInput.text.toString().split(" ")[0].trim()).commit()
            searchInput.setText("")
            updateCurrencies()
        })

        // Открывает search dropdown по нажатию.
        searchInput.setOnClickListener(View.OnClickListener {
            searchInput.showDropDown()
        })
        val searchAdapter = ArrayAdapter<String>(this, R.layout.dropdown_item, allCurrenciesList)
        searchInput.setAdapter(searchAdapter)
        searchInput.dropDownVerticalOffset = 10
        searchInput.setDropDownBackgroundResource(R.drawable.rounded_corner)

        // Открывает окно с настройками по нажатию.
        settingsButton.setOnClickListener(View.OnClickListener {
            val settingsDialog = SettingsDialog()
            val bundle = Bundle()
            // Передает значения текущих настроек.
            bundle.putString("graphInterval", graphInterval)
            bundle.putString("priceCurrency", priceCurrency)

            settingsDialog.arguments = bundle
            settingsDialog.show(supportFragmentManager, "SettingsDialog")
        })


        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    /**
     * Загружает данные о курсах валют для USD и RUB.
     */
    private fun loadExchangeRates() {
        val apiService: ApiService = ServiceBuilder.buildService(ApiService::class.java, "http://data.fixer.io/api/")
        val requestCall: Call<ExchangeRates> = apiService.getExchangeRates("latest?access_key=469655af56789c8e6b7da0f8b23f6bd6&symbols=USD,RUB")
        requestCall.enqueue(object: Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                if(response.isSuccessful) {
                    exchangeRates["USD"] = response.body()!!.rates.USD
                    exchangeRates["RUB"] = response.body()!!.rates.RUB
                }
                updateCurrencies()
            }

            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                println(t)
            }
        })
    }

    /**
     * Загружает данных о криптовалютах.
     */
    private fun loadCurrencies() {
        allCurrenciesData.clear()
        allCurrenciesList.clear()
        val apiService: ApiService = ServiceBuilder.buildService(ApiService::class.java, "https://pro-api.coinmarketcap.com")
        val requestCall: Call<DataResponse> = apiService.getCurrencies()
        requestCall.enqueue(object: Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if(response.isSuccessful) {
                    for (i in 0 until response.body()!!.data.size - 1) {
                        var responseItem = response.body()!!.data[i]
                        allCurrenciesData.add(
                            Currency(
                                responseItem.name,
                                responseItem.slug,
                                responseItem.quote.USD.price,
                                responseItem.symbol,
                                responseItem.quote.USD.percent_change_24h
                            )
                        )
                        // Добавляет названия и символ криптовалют для поиска.
                        allCurrenciesList.add(responseItem.symbol + ' ' + responseItem.name)
                    }
                }
                loadExchangeRates()
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                println(t)
            }
        })
    }

    /**
     * Обновляет добавленные криптовалюты.
     */
    fun updateCurrencies() {
        addedCurrenciesData.clear()
        recyclerView.adapter?.notifyDataSetChanged()

        for (i in 0 until allCurrenciesData.size - 1) {
            if (addedCurrenciesList.contains(allCurrenciesData[i].symbol)) {
                addedCurrenciesData.add(allCurrenciesData[i])
            }
        }

        adapter = CurrencyAdapter(this, addedCurrenciesData, addedCurrenciesList, priceCurrency, graphInterval, exchangeRates)
        recyclerView.adapter = adapter

        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        swipeRefresher.isRefreshing = false
    }

    /**
     * Удаляет криптовалюту из списка по свайпу влево.
     */
    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
            return false
        }

        // Добавляет иконку удаления.
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView

            val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2

            if (dX < 0) {
                deleteIcon.setBounds(
                    itemView.right - 20 - deleteIcon.intrinsicWidth + dX.toInt() / 5,
                    itemView.top + iconMargin,
                    itemView.right - 20 + dX.toInt() / 5,
                    itemView.bottom - iconMargin)
                deleteIcon.alpha = 255 - (255 * (dX * -1 / (itemView.width + itemView.left * 2))).toInt()
            }

            deleteIcon.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        // Убирает удаленную криптовалюту из списка и SharedPreferences
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
            pref.edit().remove(viewHolder.itemView.currencySymbol.text.toString()).commit()
            adapter.removeItem(viewHolder)
        }
    }
}
