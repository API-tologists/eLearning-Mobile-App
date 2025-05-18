package com.example.elearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.CreditCard
import com.example.elearning.repository.CreditCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreditCardViewModel : ViewModel() {
    private val repository = CreditCardRepository()
    
    private val _creditCards = MutableStateFlow<List<CreditCard>>(emptyList())
    val creditCards: StateFlow<List<CreditCard>> = _creditCards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCreditCards(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getCreditCards(userId).collect { cards ->
                    _creditCards.value = cards
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCreditCard(userId: String, cardNumber: String, cardHolderName: String, expiryDate: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addCreditCard(userId, cardNumber, cardHolderName, expiryDate)
                    .onSuccess {
                        loadCreditCards(userId)
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCreditCard(cardId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteCreditCard(cardId)
                    .onSuccess {
                        loadCreditCards(userId)
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDefaultCard(cardId: String, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.setDefaultCard(cardId, userId)
                    .onSuccess {
                        loadCreditCards(userId)
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 