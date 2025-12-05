package com.example.vendontme.data.repository

import android.content.Context
import android.net.Uri
import com.example.vendontme.core.SupabaseClient
import com.example.vendontme.data.model.Receipt
import com.example.vendontme.data.model.ReceiptItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import java.util.UUID

interface ReceiptRepository {
    suspend fun uploadReceiptImage(
        groupId: String,
        imageUri: Uri,
        context: Context
    ): Result<String>

    suspend fun createReceipt(
        groupId: String,
        imageUrl: String,
        totalAmount: Double,
        description: String?,
        userId: String
    ): Result<Receipt>

    suspend fun addReceiptItems(
        receiptId: String,
        items: List<ReceiptItem>
    ): Result<Unit>

    suspend fun getReceiptsForGroup(groupId: String): Result<List<Receipt>>
    suspend fun getReceiptById(receiptId: String): Result<Receipt>
    suspend fun getItemsForReceipt(receiptId: String): Result<List<ReceiptItem>>
    suspend fun updateReceipt(receipt: Receipt): Result<Unit>
    suspend fun updateReceiptItem(item: ReceiptItem): Result<Unit>
    suspend fun deleteReceiptItem(itemId: String): Result<Unit>
}

class SupabaseReceiptRepository(
    private val supabaseClient: SupabaseClient
) : ReceiptRepository {

    private val storage = supabaseClient.client.storage
    private val postgrest = supabaseClient.client.postgrest
    private val bucket = "receipts"

    override suspend fun uploadReceiptImage(
        groupId: String,
        imageUri: Uri,
        context: Context
    ): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
                ?: return Result.failure(Exception("Failed to read image"))
            inputStream.close()

            val receiptId = UUID.randomUUID().toString()
            val path = "$groupId/$receiptId.jpg"

            storage[bucket].upload(path, imageBytes)
            val url = storage[bucket].publicUrl(path)

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReceipt(
        groupId: String,
        imageUrl: String,
        totalAmount: Double,
        description: String?,
        userId: String
    ): Result<Receipt> {
        return try {
            val receipt = Receipt(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                imageUrl = imageUrl,
                totalAmount = totalAmount,
                createdBy = userId,
                description = description
            )

            postgrest["receipts"].insert(receipt)

            Result.success(receipt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addReceiptItems(
        receiptId: String,
        items: List<ReceiptItem>
    ): Result<Unit> {
        return try {
            items.forEach { item ->
                postgrest["receipt_items"].insert(item)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReceiptsForGroup(groupId: String): Result<List<Receipt>> {
        return try {
            val receipts = postgrest["receipts"]
                .select {
                    filter {
                        eq("group_id", groupId)
                    }
                    order(column = "created_at", order = Order.DESCENDING)
                }
                .decodeList<Receipt>()

            println("DEBUG: Fetched ${receipts.size} receipts for group $groupId")
            Result.success(receipts)
        } catch (e: Exception) {
            println("DEBUG: Failed to fetch receipts - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getReceiptById(receiptId: String): Result<Receipt> {
        return try {
            println("DEBUG: Fetching receipt $receiptId from database...")

            val receipt = postgrest["receipts"]
                .select {
                    filter {
                        eq("id", receiptId)
                    }
                }
                .decodeSingle<Receipt>()

            println("DEBUG: Fetched receipt - description: ${receipt.description}, total: ${receipt.totalAmount}")
            Result.success(receipt)
        } catch (e: Exception) {
            println("DEBUG: getReceiptById error - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getItemsForReceipt(receiptId: String): Result<List<ReceiptItem>> {
        return try {
            println("DEBUG: Fetching items for receipt $receiptId from database...")

            val items = postgrest["receipt_items"]
                .select {
                    filter {
                        eq("receipt_id", receiptId)
                    }
                }
                .decodeList<ReceiptItem>()

            println("DEBUG: Fetched ${items.size} items")
            items.forEachIndexed { index, item ->
                println("DEBUG: Item $index - name: ${item.name}, price: ${item.price}, qty: ${item.quantity}")
            }

            Result.success(items)
        } catch (e: Exception) {
            println("DEBUG: getItemsForReceipt error - ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateReceipt(receipt: Receipt): Result<Unit> {
        return try {
            println("DEBUG: Updating receipt ${receipt.id}")
            println("DEBUG: New description: '${receipt.description}'")
            println("DEBUG: New total: ${receipt.totalAmount}")

            // Use upsert with onConflict
            postgrest["receipts"].upsert(receipt) {
                // Specify which column has the unique constraint
                onConflict = "id"
            }

            println("DEBUG: Successfully upserted receipt ${receipt.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Failed to update receipt - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateReceiptItem(item: ReceiptItem): Result<Unit> {
        return try {
            println("DEBUG: Updating item ${item.id}")
            println("DEBUG: New name: '${item.name}', price: ${item.price}, qty: ${item.quantity}")

            // Use upsert with onConflict
            postgrest["receipt_items"].upsert(item) {
                // Specify which column has the unique constraint
                onConflict = "id"
            }

            println("DEBUG: Successfully upserted item ${item.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Failed to update item - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteReceiptItem(itemId: String): Result<Unit> {
        return try {
            println("DEBUG: Deleting item $itemId")

            postgrest["receipt_items"]
                .delete {
                    filter {
                        eq("id", itemId)
                    }
                }

            println("DEBUG: Successfully deleted item $itemId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Failed to delete item - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}