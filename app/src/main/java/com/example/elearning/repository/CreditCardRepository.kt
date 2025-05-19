package com.example.elearning.repository

import com.example.elearning.model.CreditCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CreditCardRepository {
    private val db = FirebaseFirestore.getInstance()
    private val creditCardsCollection = db.collection("credit_cards")

    suspend fun addCreditCard(userId: String, cardNumber: String, cardHolderName: String, expiryDate: String): Result<CreditCard> {
        return try {
            // Only store last 4 digits of card number for security
            val lastFourDigits = cardNumber.takeLast(4)
            
            val creditCard = CreditCard(
                userId = userId,
                cardNumber = lastFourDigits,
                cardHolderName = cardHolderName,
                expiryDate = expiryDate
            )

            val docRef = creditCardsCollection.document()
            val cardWithId = creditCard.copy(id = docRef.id)
            
            docRef.set(cardWithId).await()
            Result.success(cardWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCreditCards(userId: String): Flow<List<CreditCard>> = flow {
        try {
            val snapshot = creditCardsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val cards = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CreditCard::class.java)
            }
            emit(cards)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun deleteCreditCard(cardId: String): Result<Unit> {
        return try {
            creditCardsCollection.document(cardId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultCard(cardId: String, userId: String): Result<Unit> {
        return try {
            // First, set all cards to non-default
            val batch = db.batch()
            val cards = creditCardsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            cards.documents.forEach { doc ->
                batch.update(doc.reference, "isDefault", false)
            }
            
            // Then set the selected card as default
            batch.update(creditCardsCollection.document(cardId), "isDefault", true)
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 