package com.example.vendontme.ui.receipt

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vendontme.data.model.ReceiptItem
import com.example.vendontme.data.repository.AuthRepository
import com.example.vendontme.data.repository.ReceiptRepository
import kotlinx.coroutines.launch
import java.util.UUID

class ReceiptViewModel(
    private val receiptRepository: ReceiptRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun saveReceipt(
        groupId: String,
        imageUri: Uri,
        items: List<ReceiptItemInput>,
        totalAmount: Double,
        description: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null

                val userId = authRepository.getCurrentUser()?.id
                    ?: authRepository.getCurrentUserId()
                    ?: throw Exception("Not logged in")

                println("DEBUG: User ID = $userId")
                println("DEBUG: Group ID = $groupId")

                // 2. Upload image to Supabase Storage
                println("DEBUG: Starting image upload...")
                val uploadResult = receiptRepository.uploadReceiptImage(
                    groupId = groupId,
                    imageUri = imageUri,
                    context = context
                )

                if (uploadResult.isFailure) {
                    val uploadError = uploadResult.exceptionOrNull()
                    println("DEBUG: Upload failed - ${uploadError?.message}")
                    throw uploadError ?: Exception("Upload failed")
                }

                val imageUrl = uploadResult.getOrThrow()
                println("DEBUG: Image uploaded - URL = $imageUrl")

                // 3. Create receipt in database
                println("DEBUG: Creating receipt...")
                val receiptResult = receiptRepository.createReceipt(
                    groupId = groupId,
                    imageUrl = imageUrl,
                    totalAmount = totalAmount,
                    description = description,
                    userId = userId
                )

                if (receiptResult.isFailure) {
                    val receiptError = receiptResult.exceptionOrNull()
                    println("DEBUG: Receipt creation failed - ${receiptError?.message}")
                    throw receiptError ?: Exception("Failed to create receipt")
                }

                val receipt = receiptResult.getOrThrow()
                println("DEBUG: Receipt created - ID = ${receipt.id}")

                // 4. Add items to receipt
                val receiptItems = items
                    .filter { it.name.isNotBlank() && it.price.isNotBlank() }
                    .map { input ->
                        ReceiptItem(
                            id = UUID.randomUUID().toString(),
                            receiptId = receipt.id,
                            name = input.name,
                            price = input.price.toDouble(),
                            quantity = input.quantity.toIntOrNull() ?: 1
                        )
                    }

                println("DEBUG: Adding ${receiptItems.size} items...")
                val itemsResult = receiptRepository.addReceiptItems(receipt.id, receiptItems)

                if (itemsResult.isFailure) {
                    val itemsError = itemsResult.exceptionOrNull()
                    println("DEBUG: Items creation failed - ${itemsError?.message}")
                    throw itemsError ?: Exception("Failed to add items")
                }

                println("DEBUG: SUCCESS! All items saved")
                onSuccess()

            } catch (e: Exception) {
                println("DEBUG: ERROR - ${e.message}")
                e.printStackTrace()
                error = e.message ?: "Failed to save receipt"
            } finally {
                isLoading = false
            }
        }
    }
}