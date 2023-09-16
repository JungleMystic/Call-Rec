package com.lrm.voicerec.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lrm.voicerec.constants.TAG

class RecViewModel: ViewModel() {

    private val _pageSelected = MutableLiveData<Int>(0)
    val pageSelected: LiveData<Int> = _pageSelected

    fun setSelectedPage(page: Int) {
        _pageSelected.value = page
        Log.i(TAG, "setSelectedPage is called -> Page: $page")
    }
}