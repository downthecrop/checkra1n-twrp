package com.downthecrop.checkra1n_kotlin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    private val _button = MutableLiveData<String>().apply {
        value = ""
    }
val button: LiveData<String> = _button
}