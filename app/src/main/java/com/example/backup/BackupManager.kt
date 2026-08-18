package com.example.backup

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    private val humanDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun exportPurchasesToJson(purchases: List<PurchaseEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "What Did I Buy?")
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (p in purchases) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("productName", p.productName)
            obj.put("brand", p.brand)
            obj.put("model", p.model)
            obj.put("category", p.category.name)
            obj.put("customCategory", p.customCategory)
            obj.put("store", p.store)
            obj.put("purchaseDate", p.purchaseDate)
            obj.put("purchasePrice", p.purchasePrice)
            obj.put("currency", p.currency)
            obj.put("quantity", p.quantity)
            obj.put("serialNumber", p.serialNumber)
            obj.put("orderNumber", p.orderNumber)
            obj.put("receiptNumber", p.receiptNumber)
            p.warrantyStartDate?.let { obj.put("warrantyStartDate", it) }
            p.warrantyEndDate?.let { obj.put("warrantyEndDate", it) }
            obj.put("warrantyDurationMonths", p.warrantyDurationMonths)
            obj.put("warrantyStatus", p.warrantyStatus.name)
            p.returnStartDate?.let { obj.put("returnStartDate", it) }
            p.returnEndDate?.let { obj.put("returnEndDate", it) }
            obj.put("returnPeriodDays", p.returnPeriodDays)
            obj.put("returnStatus", p.returnStatus.name)
            obj.put("notes", p.notes)
            obj.put("isGift", p.isGift)
            obj.put("giftRecipient", p.giftRecipient)
            obj.put("isBorrowed", p.isBorrowed)
            obj.put("isLent", p.isLent)
            obj.put("contactName", p.contactName)
            obj.put("itemsSummary", p.itemsSummary)
            obj.put("createdAt", p.createdAt)
            array.put(obj)
        }
        root.put("purchases", array)
        return root.toString(2)
    }

    fun parsePurchasesFromJson(jsonString: String): List<PurchaseEntity> {
        val result = mutableListOf<PurchaseEntity>()
        val root = JSONObject(jsonString)
        val array = root.optJSONArray("purchases") ?: return emptyList()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val p = PurchaseEntity(
                id = 0, // auto generate on restore
                productName = obj.optString("productName", "Imported Purchase"),
                brand = obj.optString("brand", ""),
                model = obj.optString("model", ""),
                category = try {
                    PurchaseCategory.valueOf(obj.optString("category", PurchaseCategory.OTHER.name))
                } catch (e: Exception) {
                    PurchaseCategory.OTHER
                },
                customCategory = obj.optString("customCategory", ""),
                store = obj.optString("store", ""),
                purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
                purchasePrice = obj.optDouble("purchasePrice", 0.0),
                currency = obj.optString("currency", "JOD"),
                quantity = obj.optInt("quantity", 1),
                serialNumber = obj.optString("serialNumber", ""),
                orderNumber = obj.optString("orderNumber", ""),
                receiptNumber = obj.optString("receiptNumber", ""),
                warrantyStartDate = if (obj.has("warrantyStartDate")) obj.getLong("warrantyStartDate") else null,
                warrantyEndDate = if (obj.has("warrantyEndDate")) obj.getLong("warrantyEndDate") else null,
                warrantyDurationMonths = obj.optInt("warrantyDurationMonths", 0),
                warrantyStatus = try {
                    WarrantyStatus.valueOf(obj.optString("warrantyStatus", WarrantyStatus.UNKNOWN.name))
                } catch (e: Exception) {
                    WarrantyStatus.UNKNOWN
                },
                returnStartDate = if (obj.has("returnStartDate")) obj.getLong("returnStartDate") else null,
                returnEndDate = if (obj.has("returnEndDate")) obj.getLong("returnEndDate") else null,
                returnPeriodDays = obj.optInt("returnPeriodDays", 0),
                returnStatus = try {
                    ReturnStatus.valueOf(obj.optString("returnStatus", ReturnStatus.UNKNOWN.name))
                } catch (e: Exception) {
                    ReturnStatus.UNKNOWN
                },
                notes = obj.optString("notes", ""),
                isGift = obj.optBoolean("isGift", false),
                giftRecipient = obj.optString("giftRecipient", ""),
                isBorrowed = obj.optBoolean("isBorrowed", false),
                isLent = obj.optBoolean("isLent", false),
                contactName = obj.optString("contactName", ""),
                itemsSummary = obj.optString("itemsSummary", ""),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = System.currentTimeMillis()
            )
            result.add(p)
        }
        return result
    }

    fun exportPurchasesToCsv(purchases: List<PurchaseEntity>): String {
        val sb = StringBuilder()
        sb.append("Product Name,Brand,Model,Category,Store,Purchase Date,Price,Currency,Serial Number,Order Number,Warranty Expiration,Return Deadline,Notes\n")
        for (p in purchases) {
            val dateStr = humanDateFormat.format(Date(p.purchaseDate))
            val warStr = p.warrantyEndDate?.let { humanDateFormat.format(Date(it)) } ?: ""
            val retStr = p.returnEndDate?.let { humanDateFormat.format(Date(it)) } ?: ""
            val escapedName = "\"${p.productName.replace("\"", "\"\"")}\""
            val escapedStore = "\"${p.store.replace("\"", "\"\"")}\""
            val escapedNotes = "\"${p.notes.replace("\"", "\"\"")}\""

            sb.append("$escapedName,${p.brand},${p.model},${p.category.displayName},$escapedStore,$dateStr,${p.purchasePrice},${p.currency},${p.serialNumber},${p.orderNumber},$warStr,$retStr,$escapedNotes\n")
        }
        return sb.toString()
    }

    fun buildWarrantyClaimText(
        purchase: PurchaseEntity,
        problemDescription: String,
        attachedDocTitles: List<String>
    ): String {
        val pDate = humanDateFormat.format(Date(purchase.purchaseDate))
        val warEnd = purchase.warrantyEndDate?.let { humanDateFormat.format(Date(it)) } ?: "Not specified"

        return """
            ==============================================
            WARRANTY SERVICE CLAIM PACKAGE
            Generated by What Did I Buy?
            ==============================================
            
            PRODUCT INFORMATION:
            • Item: ${purchase.productName}
            • Brand: ${purchase.brand.ifEmpty { "N/A" }}
            • Model: ${purchase.model.ifEmpty { "N/A" }}
            • Serial Number: ${purchase.serialNumber.ifEmpty { "N/A" }}
            
            PURCHASE DETAILS:
            • Purchased On: $pDate
            • Merchant / Store: ${purchase.store.ifEmpty { "N/A" }}
            • Purchase Price: ${purchase.purchasePrice} ${purchase.currency}
            • Invoice / Order #: ${purchase.orderNumber.ifEmpty { purchase.receiptNumber.ifEmpty { "N/A" } }}
            
            WARRANTY STATUS:
            • Warranty Expiration: $warEnd
            • Warranty Coverage: ${purchase.warrantyDurationMonths} Months
            • Current Status: ${purchase.warrantyStatus.displayName}
            
            PROBLEM DESCRIPTION:
            $problemDescription
            
            ATTACHED VERIFICATION DOCUMENTS:
            ${if (attachedDocTitles.isEmpty()) "• (Digital receipt & documents linked in app)" else attachedDocTitles.joinToString("\n") { "• $it" }}
            
            ----------------------------------------------
            This claim package summarizes purchase records for verification.
        """.trimIndent()
    }

    fun buildReturnSlipText(purchase: PurchaseEntity): String {
        val pDate = humanDateFormat.format(Date(purchase.purchaseDate))
        val retEnd = purchase.returnEndDate?.let { humanDateFormat.format(Date(it)) } ?: "Unspecified"
        val now = System.currentTimeMillis()
        val daysRemaining = purchase.returnEndDate?.let { ((it - now) / 86400000L).coerceAtLeast(0) } ?: 0

        return """
            ==============================================
            PURCHASE RETURN SUMMARY SLIP
            Generated by What Did I Buy?
            ==============================================
            
            ITEM DETAILS:
            • Item: ${purchase.productName}
            • Brand / Model: ${purchase.brand} ${purchase.model}
            • Total Paid: ${purchase.purchasePrice} ${purchase.currency}
            
            MERCHANT & RECEIPT:
            • Store: ${purchase.store}
            • Purchase Date: $pDate
            • Order / Receipt #: ${purchase.orderNumber.ifEmpty { purchase.receiptNumber.ifEmpty { "On File" } }}
            
            RETURN OPPORTUNITY:
            • Return Deadline: $retEnd
            • Days Remaining: $daysRemaining day(s)
            • Return Status: ${purchase.returnStatus.displayName}
            
            NOTES:
            ${purchase.notes.ifEmpty { "None" }}
        """.trimIndent()
    }

    fun shareText(context: Context, subject: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, subject)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
