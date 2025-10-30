package com.example.inv_5.ui.vendors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VendorsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is vendors Fragment"
    }
    val text: LiveData<String> = _text
}