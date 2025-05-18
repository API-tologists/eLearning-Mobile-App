package com.example.elearning.model

data class CreditCard(
    val id: String = "",
    val userId: String = "",
    val cardNumber: String = "", // Last 4 digits only for security
    val cardHolderName: String = "",
    val expiryDate: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) 