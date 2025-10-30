package com.example.inv_5.ui.stockmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StockManagementViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is stock management Fragment"
    }
    val text: LiveData<String> = _text
}