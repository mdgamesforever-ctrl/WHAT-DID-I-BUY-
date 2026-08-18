package com.example.ocr

import com.example.data.model.PurchaseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object OcrParser {

    private val DATE_FORMATS = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
        SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
        SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH),
        SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH),
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
    )

    private val KNOWN_STORES = listOf(
        "Carrefour", "كارفور", "Amazon", "أمازون", "IKEA", "ايكيا",
        "Apple Store", "Samsung", "سامسونج", "Walmart", "Best Buy",
        "Zara", "زارا", "H&M", "Nike", "نايكي", "Adidas", "اديداس",
        "Virgin Megastore", "فيرجن", "Lulu Hypermarket", "لولو هايبرماركت",
        "City Mall", "سيتي مول", "DNA", "SmartBuy", "سمارت باي",
        "Home Centre", "هوم سنتر", "Sharaf DG", "Target", "Decathlon",
        "Sephora", "سيفورا", "Jarir Bookstore", "مكتبة جرير", "Extra", "اكسترا"
    )

    fun parseReceiptText(rawText: String, imageUri: String = ""): OcrDraftPurchase {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return OcrDraftPurchase(rawText = rawText, imageUri = imageUri)
        }

        // 1. Detect Store Name
        var detectedStore = ""
        var isConfidentStore = true
        for (known in KNOWN_STORES) {
            if (rawText.contains(known, ignoreCase = true)) {
                detectedStore = known
                break
            }
        }
        if (detectedStore.isEmpty()) {
            // First 1-3 lines usually contain the store header
            detectedStore = lines.firstOrNull { it.length > 2 && !it.contains(Regex("[0-9]{3,}")) } ?: lines[0]
            isConfidentStore = detectedStore.length < 30
        }

        // 2. Detect Currency
        var detectedCurrency = "JOD"
        when {
            rawText.contains("JOD", ignoreCase = true) || rawText.contains("JD", ignoreCase = true) || rawText.contains("د.أ") || rawText.contains("دينار") -> detectedCurrency = "JOD"
            rawText.contains("SAR", ignoreCase = true) || rawText.contains("ر.س") || rawText.contains("ريال") -> detectedCurrency = "SAR"
            rawText.contains("AED", ignoreCase = true) || rawText.contains("د.إ") || rawText.contains("درهم") -> detectedCurrency = "AED"
            rawText.contains("USD", ignoreCase = true) || rawText.contains("$") -> detectedCurrency = "USD"
            rawText.contains("EUR", ignoreCase = true) || rawText.contains("€") -> detectedCurrency = "EUR"
            rawText.contains("GBP", ignoreCase = true) || rawText.contains("£") -> detectedCurrency = "GBP"
            rawText.contains("EGP", ignoreCase = true) || rawText.contains("ج.م") || rawText.contains("جنيه") -> detectedCurrency = "EGP"
        }

        // 3. Detect Total Price
        var detectedTotal = 0.0
        var isConfidentTotal = false
        val totalKeywords = listOf("TOTAL", "GRAND TOTAL", "AMOUNT", "NET", "TOTAL DUE", "المجموع", "الإجمالي", "صافي القيمة")
        for (line in lines.reversed()) {
            val upper = line.uppercase()
            if (totalKeywords.any { upper.contains(it) }) {
                val numberMatch = Regex("(\\d+[.,]\\d{1,3}|\\d+)").findAll(line).lastOrNull()
                if (numberMatch != null) {
                    val cleaned = numberMatch.value.replace(",", ".")
                    val parsed = cleaned.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        detectedTotal = parsed
                        isConfidentTotal = true
                        break
                    }
                }
            }
        }
        if (detectedTotal == 0.0) {
            // Find the largest reasonable monetary figure
            val numbers = Regex("(\\d+[.,]\\d{2})").findAll(rawText)
                .mapNotNull { it.value.replace(",", ".").toDoubleOrNull() }
                .filter { it in 0.5..50000.0 }
                .toList()
            if (numbers.isNotEmpty()) {
                detectedTotal = numbers.maxOrNull() ?: 0.0
                isConfidentTotal = false
            }
        }

        // 4. Detect Date
        var purchaseDateMillis = System.currentTimeMillis()
        var isConfidentDate = false
        val dateRegex = Regex("(\\b\\d{4}[-/.]\\d{1,2}[-/.]\\d{1,2}\\b|\\b\\d{1,2}[-/.]\\d{1,2}[-/.]\\d{2,4}\\b)")
        val match = dateRegex.find(rawText)
        if (match != null) {
            val dateStr = match.value
            for (format in DATE_FORMATS) {
                try {
                    val parsed = format.parse(dateStr)
                    if (parsed != null && parsed.before(Date(System.currentTimeMillis() + 86400000L))) {
                        purchaseDateMillis = parsed.time
                        isConfidentDate = true
                        break
                    }
                } catch (e: Exception) {
                    // Try next
                }
            }
        }

        // 5. Parse Itemized lines
        val items = mutableListOf<OcrItem>()
        for (line in lines) {
            val isTotalLine = totalKeywords.any { line.uppercase().contains(it) }
            if (isTotalLine) continue
            val itemPriceMatch = Regex("(.+?)\\s+(\\d+[.,]\\d{2})\\s*$").find(line)
            if (itemPriceMatch != null) {
                val name = itemPriceMatch.groupValues[1].trim()
                val price = itemPriceMatch.groupValues[2].replace(",", ".").toDoubleOrNull() ?: 0.0
                if (name.length > 2 && price > 0 && price <= (if (detectedTotal > 0) detectedTotal else 10000.0)) {
                    items.add(OcrItem(name = name, quantity = 1, price = price))
                }
            }
        }
        val isConfidentItems = items.isNotEmpty()

        // 6. Primary product name & suggested category
        val primaryProductName = if (items.isNotEmpty()) {
            items.first().name
        } else {
            detectedStore.ifEmpty { "Purchase Item" }
        }

        val suggestedCategory = guessCategory(rawText, primaryProductName)

        // 7. Serial / Invoice / Warranty / Return policy detection
        val serialMatch = Regex("(?:S/N|SN|SERIAL|سيريال|الرقم التسلسلي)[:\\s#]+([A-Za-z0-9\\-]{5,20})", RegexOption.IGNORE_CASE).find(rawText)
        val serialNumber = serialMatch?.groupValues?.getOrNull(1) ?: ""

        val invoiceMatch = Regex("(?:INV|INVOICE|RECEIPT|BILL|فاتورة|وصل)[:\\s#]+([A-Za-z0-9\\-]{3,15})", RegexOption.IGNORE_CASE).find(rawText)
        val invoiceNumber = invoiceMatch?.groupValues?.getOrNull(1) ?: ""

        // Return policy detection
        var returnDays = 14
        var detectedReturnPolicy = ""
        val returnMatch = Regex("(\\d+)\\s*(?:days?|يوم|أيام|ايام)\\s*(?:return|exchange|refund|استرجاع|تبديل)", RegexOption.IGNORE_CASE).find(rawText)
        if (returnMatch != null) {
            returnDays = returnMatch.groupValues[1].toIntOrNull() ?: 14
            detectedReturnPolicy = "${returnDays} days detected"
        }

        val calReturn = Calendar.getInstance().apply {
            timeInMillis = purchaseDateMillis
            add(Calendar.DAY_OF_YEAR, returnDays)
        }
        val returnDeadlineMillis = calReturn.timeInMillis

        // Warranty detection
        var warrantyMonths = when (suggestedCategory) {
            PurchaseCategory.ELECTRONICS, PurchaseCategory.PHONES, PurchaseCategory.COMPUTERS -> 12
            PurchaseCategory.HOME_APPLIANCES -> 24
            PurchaseCategory.VEHICLES, PurchaseCategory.VEHICLE_PARTS -> 12
            else -> 0
        }
        var detectedWarrantyPolicy = ""
        val warrantyMatchYear = Regex("(\\d+)\\s*(?:years?|سنة|سنوات|عام)\\s*(?:warranty|guarantee|ضمان)", RegexOption.IGNORE_CASE).find(rawText)
        val warrantyMatchMonth = Regex("(\\d+)\\s*(?:months?|شهر|أشهر|شهور)\\s*(?:warranty|guarantee|ضمان)", RegexOption.IGNORE_CASE).find(rawText)

        if (warrantyMatchYear != null) {
            val yrs = warrantyMatchYear.groupValues[1].toIntOrNull() ?: 1
            warrantyMonths = yrs * 12
            detectedWarrantyPolicy = "$yrs Year(s) Warranty"
        } else if (warrantyMatchMonth != null) {
            warrantyMonths = warrantyMatchMonth.groupValues[1].toIntOrNull() ?: 12
            detectedWarrantyPolicy = "$warrantyMonths Month(s) Warranty"
        }

        val calWarranty = Calendar.getInstance().apply {
            timeInMillis = purchaseDateMillis
            add(Calendar.MONTH, warrantyMonths)
        }
        val warrantyExpirationMillis = if (warrantyMonths > 0) calWarranty.timeInMillis else null

        return OcrDraftPurchase(
            storeName = detectedStore,
            isConfidentStore = isConfidentStore,
            purchaseDateMillis = purchaseDateMillis,
            isConfidentDate = isConfidentDate,
            totalPrice = detectedTotal,
            currency = detectedCurrency,
            isConfidentTotal = isConfidentTotal,
            detectedItems = items,
            isConfidentItems = isConfidentItems,
            primaryProductName = primaryProductName,
            suggestedCategory = suggestedCategory,
            serialNumber = serialNumber,
            invoiceNumber = invoiceNumber,
            returnPeriodDays = returnDays,
            returnDeadlineMillis = returnDeadlineMillis,
            warrantyDurationMonths = warrantyMonths,
            warrantyExpirationMillis = warrantyExpirationMillis,
            returnPolicyText = detectedReturnPolicy,
            warrantyPolicyText = detectedWarrantyPolicy,
            rawText = rawText,
            imageUri = imageUri
        )
    }

    private fun guessCategory(rawText: String, productName: String): PurchaseCategory {
        val text = "$rawText $productName".lowercase()
        return when {
            text.contains("iphone") || text.contains("samsung galaxy") || text.contains("phone") || text.contains("هاتف") || text.contains("جوال") -> PurchaseCategory.PHONES
            text.contains("laptop") || text.contains("macbook") || text.contains("dell") || text.contains("lenovo") || text.contains("كمبيوتر") || text.contains("حاسوب") -> PurchaseCategory.COMPUTERS
            text.contains("tv") || text.contains("headphone") || text.contains("airpods") || text.contains("charger") || text.contains("cable") || text.contains("سماعات") || text.contains("شاحن") || text.contains("إلكترونيات") -> PurchaseCategory.ELECTRONICS
            text.contains("refrigerator") || text.contains("washer") || text.contains("microwave") || text.contains("oven") || text.contains("blender") || text.contains("غسالة") || text.contains("ثلاجة") || text.contains("ميكرويف") -> PurchaseCategory.HOME_APPLIANCES
            text.contains("shirt") || text.contains("dress") || text.contains("pants") || text.contains("jacket") || text.contains("ملابس") || text.contains("قميص") -> PurchaseCategory.CLOTHING
            text.contains("shoes") || text.contains("sneakers") || text.contains("boots") || text.contains("حذاء") -> PurchaseCategory.SHOES
            text.contains("sofa") || text.contains("table") || text.contains("chair") || text.contains("desk") || text.contains("أثاث") || text.contains("طاولة") -> PurchaseCategory.FURNITURE
            text.contains("ps5") || text.contains("playstation") || text.contains("xbox") || text.contains("nintendo") || text.contains("gaming") || text.contains("ألعاب") -> PurchaseCategory.GAMING
            text.contains("drill") || text.contains("hammer") || text.contains("wrench") || text.contains("tools") || text.contains("أدوات") -> PurchaseCategory.TOOLS
            text.contains("supermarket") || text.contains("grocery") || text.contains("food") || text.contains("restaurant") || text.contains("مطعم") || text.contains("بقالة") || text.contains("طعام") -> PurchaseCategory.FOOD
            text.contains("pharmacy") || text.contains("vitamins") || text.contains("care") || text.contains("صيدلية") -> PurchaseCategory.HEALTH_PERSONAL_CARE
            else -> PurchaseCategory.OTHER
        }
    }
}
