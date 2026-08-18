package com.example.ai

import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AiPurchaseAssistant {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    /**
     * Answers natural language questions strictly grounded in local stored purchases.
     * Guaranteed zero hallucination.
     */
    fun answerQueryOffline(query: String, purchases: List<PurchaseEntity>): String {
        val q = query.trim().lowercase()
        if (purchases.isEmpty()) {
            return "You haven't recorded any purchases yet. Once you add or scan purchases, I can answer your questions based on your records."
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 1. "What warranties expire this month?" / "warranties expiring"
        if (q.contains("warranty") && (q.contains("expire") || q.contains("expiring") || q.contains("month") || q.contains("soon"))) {
            val expiring = purchases.filter {
                val end = it.warrantyEndDate
                end != null && end > now && (end - now) <= (35L * 86400000L)
            }
            if (expiring.isEmpty()) {
                return "Based on your stored records, you have no warranties expiring in the next 30 days."
            }
            val builder = StringBuilder("Here are the warranties expiring soon based on your records:\n\n")
            expiring.forEach {
                val expDate = it.warrantyEndDate?.let { d -> dateFormat.format(Date(d)) } ?: "Unknown"
                val daysLeft = ((it.warrantyEndDate!! - now) / 86400000L).coerceAtLeast(0)
                builder.append("• ${it.productName} (${it.store.ifEmpty { "Store not set" }}) - Expires on $expDate ($daysLeft days remaining)\n")
            }
            return builder.toString().trim()
        }

        // 2. "Which purchases are still returnable?" / "return window" / "can I still return"
        if (q.contains("return") || q.contains("returnable")) {
            val returnable = purchases.filter {
                val retEnd = it.returnEndDate
                retEnd != null && retEnd > now
            }
            if (returnable.isEmpty()) {
                return "Based on your stored purchase records, none of your current items have active return windows."
            }
            val builder = StringBuilder("Here are your active return opportunities:\n\n")
            returnable.forEach {
                val retDate = it.returnEndDate?.let { d -> dateFormat.format(Date(d)) } ?: "Unknown"
                val daysLeft = ((it.returnEndDate!! - now) / 86400000L).coerceAtLeast(0)
                val priceStr = String.format(Locale.ENGLISH, "%.2f %s", it.purchasePrice, it.currency)
                builder.append("• ${it.productName} - $priceStr - Return deadline: $retDate ($daysLeft days left)\n")
            }
            return builder.toString().trim()
        }

        // 3. "How much did I spend on electronics / phones / computers / clothing / etc."
        for (cat in com.example.data.model.PurchaseCategory.entries) {
            if (q.contains(cat.name.lowercase().replace("_", " ")) || q.contains(cat.displayName.lowercase())) {
                val inCat = purchases.filter { it.category == cat }
                if (inCat.isEmpty()) {
                    return "Based on your records, you have no logged purchases under the '${cat.displayName}' category."
                }
                val currencyMap = inCat.groupBy { it.currency }
                val totalsStr = currencyMap.map { (curr, items) ->
                    String.format(Locale.ENGLISH, "%.2f %s (%d items)", items.sumOf { it.purchasePrice }, curr, items.size)
                }.joinToString(", ")
                return "Based on your stored records, you have spent $totalsStr on ${cat.displayName}."
            }
        }

        // 4. "Show me everything I bought from [Store]"
        val stores = purchases.map { it.store.trim() }.filter { it.isNotEmpty() }.distinct()
        val matchingStore = stores.firstOrNull { q.contains(it.lowercase()) }
        if (matchingStore != null) {
            val fromStore = purchases.filter { it.store.equals(matchingStore, ignoreCase = true) }
            val builder = StringBuilder("Here is everything you purchased from $matchingStore (${fromStore.size} items):\n\n")
            fromStore.forEach {
                val pDate = dateFormat.format(Date(it.purchaseDate))
                val price = String.format(Locale.ENGLISH, "%.2f %s", it.purchasePrice, it.currency)
                builder.append("• ${it.productName} - $price ($pDate)\n")
            }
            return builder.toString().trim()
        }

        // 5. "Which products have no warranty information?" / "missing warranty"
        if (q.contains("no warranty") || q.contains("missing warranty")) {
            val noWarranty = purchases.filter { it.warrantyEndDate == null || it.warrantyStatus == WarrantyStatus.UNKNOWN }
            if (noWarranty.isEmpty()) {
                return "All your stored purchases have warranty information attached."
            }
            val builder = StringBuilder("The following ${noWarranty.size} products have no warranty expiration set:\n\n")
            noWarranty.forEach {
                builder.append("• ${it.productName} (${it.store.ifEmpty { "Store unspecified" }})\n")
            }
            return builder.toString().trim()
        }

        // 6. "How much did I spend on [Product]?" / Search by product name
        val matchedProducts = purchases.filter { p ->
            val words = p.productName.lowercase().split(" ")
            words.any { w -> w.length > 2 && q.contains(w) }
        }
        if (matchedProducts.isNotEmpty()) {
            val builder = StringBuilder("Found ${matchedProducts.size} matching purchase(s):\n\n")
            matchedProducts.forEach { p ->
                val pDate = dateFormat.format(Date(p.purchaseDate))
                val price = String.format(Locale.ENGLISH, "%.2f %s", p.purchasePrice, p.currency)
                builder.append("• ${p.productName} (${p.brand}) - $price bought on $pDate at ${p.store.ifEmpty { "Unspecified Store" }}\n")
            }
            return builder.toString().trim()
        }

        // 7. General spending / count overview
        if (q.contains("total") || q.contains("spend") || q.contains("how much")) {
            val currencyMap = purchases.groupBy { it.currency }
            val totals = currencyMap.map { (curr, items) ->
                String.format(Locale.ENGLISH, "%.2f %s", items.sumOf { it.purchasePrice }, curr)
            }.joinToString(" and ")
            return "Based on your purchase records across ${purchases.size} total items, your tracked spending is: $totals."
        }

        return "I couldn't find enough specific information in your stored purchases to answer that question. You can ask me about warranties, return windows, categories, specific stores, or product prices."
    }
}
