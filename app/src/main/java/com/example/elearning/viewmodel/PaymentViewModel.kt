package com.example.elearning.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.service.PaymentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    private val paymentService = PaymentService(application)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val paymentState = paymentService.paymentState

    init {
        paymentService.initialize()
        
        // Observe payment state changes
        viewModelScope.launch {
            paymentState.collectLatest { state ->
                when (state) {
                    is PaymentService.PaymentState.Error -> {
                        _error.value = state.message
                        _isLoading.value = false
                    }
                    is PaymentService.PaymentState.Success -> {
                        _isLoading.value = false
                        // Handle successful payment
                    }
                    is PaymentService.PaymentState.Canceled -> {
                        _isLoading.value = false
                        // Handle canceled payment
                    }
                    else -> {}
                }
            }
        }
    }

    fun initiatePayment(amount: Double, currency: String = "USD") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                paymentService.processPayment(amount, currency)
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 