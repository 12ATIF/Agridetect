package com.dicoding.capstone.dermaface.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.capstone.dermaface.data.model.HistoryResponse
import com.dicoding.capstone.dermaface.repository.LocalHistoryRepository
import kotlinx.coroutines.launch

class ScanHistoryViewModel(private val repository: LocalHistoryRepository) : ViewModel() {

    private val _histories = MutableLiveData<List<HistoryResponse>>()
    val histories: LiveData<List<HistoryResponse>> get() = _histories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchHistories() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.fetchHistories()
            _histories.value = result.getOrNull() ?: emptyList()
            _isLoading.value = false
        }
    }
}