package com.example.crypinfo.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import kotlinx.android.synthetic.main.settings_dialog.view.*
import android.widget.ArrayAdapter
import android.app.Activity
import com.example.crypinfo.R


class SettingsDialog(): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val view = activity!!.layoutInflater.inflate(com.example.crypinfo.R.layout.settings_dialog, null)

        // Получает данные из MainActivity о текущих настройках.
        val priceCurrency = arguments?.getString("priceCurrency")
        val graphInterval = arguments?.getString("graphInterval")

        // Устанавливает значения для dropdown с настройками интервала графика.
        val graphList = mutableListOf(
            "Hour",
            "Day",
            "Week",
            "Month",
            "Halfyear",
            "Year",
            "3 years",
            "All time"
        )
        val graphAdapter = ArrayAdapter<String>(
            context,
            R.layout.dropdown_item_small, graphList
        )
        graphAdapter.setDropDownViewResource(R.layout.dropdown_item_small)
        view.graphSetting.setAdapter(graphAdapter)
        view.graphSetting.setSelection(graphList.indexOf(graphInterval))

        // Устанавливает значения для dropdown с настройками прайса валюты.
        val currencyList = mutableListOf(
            "USD",
            "EUR",
            "RUB"
        )
        val currencyAdapter = ArrayAdapter<String>(
            context,
            R.layout.dropdown_item_small, currencyList
        )
        currencyAdapter.setDropDownViewResource(R.layout.dropdown_item_small)
        view.currencySetting.setAdapter(currencyAdapter)
        view.currencySetting.setSelection(currencyList.indexOf(priceCurrency))

        // Передает измененные настройки и закрывает окно настроек при нажатии на ok.
        val activityView = view
        view.confirmButton.setOnClickListener {
            this.mListener?.onComplete(activityView.graphSetting.selectedItem.toString(), activityView.currencySetting.selectedItem.toString())
            dismiss()
        }

        // Закрывает окно настроек при нажатии на cancel.
        view.cancelButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    /**
     * Следит за изменением интервала графика и валюты прайса.
     */
    interface OnCompleteListener {
        fun onComplete(graphInterval: String, priceCurrency: String)
    }
    private var mListener: OnCompleteListener? = null
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            this.mListener = activity as OnCompleteListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement OnCompleteListener")
        }

    }
}