package com.example.inv_5.ui.purchases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PurchasesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is purchases Fragment"
    }
    val text: LiveData<String> = _text
}