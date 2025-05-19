package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.viewmodel.CreditCardViewModel
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.model.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardScreen(
    navController: NavController,
    creditCardViewModel: CreditCardViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    
    val isLoading by creditCardViewModel.isLoading.collectAsState()
    val error by creditCardViewModel.error.collectAsState()

    // Validation states
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var cardHolderError by remember { mutableStateOf<String?>(null) }
    var expiryDateError by remember { mutableStateOf<String?>(null) }
    var cvvError by remember { mutableStateOf<String?>(null) }

    // Show error message if any
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message using Snackbar or Toast
            creditCardViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Credit Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Number
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { 
                    if (it.length <= 19) { // Max length for card number with spaces
                        cardNumber = it.filter { char -> char.isDigit() }
                            .chunked(4)
                            .joinToString(" ")
                        cardNumberError = null
                    }
                },
                label = { Text("Card Number") },
                placeholder = { Text("1234 5678 9012 3456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cardNumberError != null,
                supportingText = {
                    if (cardNumberError != null) {
                        Text(cardNumberError!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Card Holder Name
            OutlinedTextField(
                value = cardHolderName,
                onValueChange = { 
                    cardHolderName = it.uppercase()
                    cardHolderError = null
                },
                label = { Text("Card Holder Name") },
                placeholder = { Text("JOHN DOE") },
                isError = cardHolderError != null,
                supportingText = {
                    if (cardHolderError != null) {
                        Text(cardHolderError!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expiry Date
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { 
                        if (it.length <= 5) { // MM/YY format
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.length <= 4) {
                                expiryDate = if (filtered.length > 2) {
                                    "${filtered.substring(0, 2)}/${filtered.substring(2)}"
                                } else {
                                    filtered
                                }
                                expiryDateError = null
                            }
                        }
                    },
                    label = { Text("Expiry Date") },
                    placeholder = { Text("MM/YY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = expiryDateError != null,
                    supportingText = {
                        if (expiryDateError != null) {
                            Text(expiryDateError!!)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // CVV
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { 
                        if (it.length <= 4) {
                            cvv = it.filter { char -> char.isDigit() }
                            cvvError = null
                        }
                    },
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = cvvError != null,
                    supportingText = {
                        if (cvvError != null) {
                            Text(cvvError!!)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    // Validate inputs
                    var isValid = true

                    if (cardNumber.replace(" ", "").length != 16) {
                        cardNumberError = "Please enter a valid 16-digit card number"
                        isValid = false
                    }

                    if (cardHolderName.isBlank()) {
                        cardHolderError = "Please enter card holder name"
                        isValid = false
                    }

                    if (expiryDate.length != 5) {
                        expiryDateError = "Please enter a valid expiry date (MM/YY)"
                        isValid = false
                    }

                    if (cvv.length < 3) {
                        cvvError = "Please enter a valid CVV"
                        isValid = false
                    }

                    if (isValid && user != null) {
                        creditCardViewModel.addCreditCard(
                            userId = user.id,
                            cardNumber = cardNumber.replace(" ", ""),
                            cardHolderName = cardHolderName,
                            expiryDate = expiryDate
                        )
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && cardNumber.isNotBlank() && 
                         cardHolderName.isNotBlank() && 
                         expiryDate.isNotBlank() && 
                         cvv.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Card")
                }
            }
        }
    }
} 