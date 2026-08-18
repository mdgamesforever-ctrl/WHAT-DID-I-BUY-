package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiPurchaseAssistant
import com.example.backup.BackupManager
import com.example.billing.BillingConfig
import com.example.billing.BillingManager
import com.example.billing.BillingPlan
import com.example.billing.SubscriptionState
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.model.DocumentType
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.notification.NotificationHelper
import com.example.ocr.OcrDraftPurchase
import com.example.ocr.OcrParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class AttentionItem(
    val id: String,
    val purchaseId: Long,
    val type: AttentionType,
    val title: String,
    val subtitle: String,
    val urgencyLabel: String,
    val amountFormatted: String,
    val priority: Int // 1 highest (today), 2 tomorrow, 3 soon, etc.
)

enum class AttentionType {
    RETURN_WINDOW,
    WARRANTY_EXPIRATION,
    MISSING_WARRANTY,
    DOCUMENT_ATTENTION
}

data class PriceMemoryComparison(
    val productName: String,
    val previousPrice: Double,
    val latestPrice: Double,
    val currency: String,
    val difference: Double, // latest - previous
    val previousDate: Long,
    val latestDate: Long
)

data class CategorySpending(
    val category: PurchaseCategory,
    val amount: Double,
    val currency: String,
    val count: Int,
    val percentage: Float
)

data class StoreSpending(
    val storeName: String,
    val amount: Double,
    val currency: String,
    val count: Int
)

data class MonthlySpending(
    val monthYearLabel: String,
    val amount: Double,
    val currency: String,
    val count: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val purchaseDao = db.purchaseDao()
    private val documentDao = db.documentDao()
    private val reminderDao = db.reminderDao()

    val billingManager = BillingManager(application)
    val subscriptionState: StateFlow<SubscriptionState> = billingManager.subscriptionState

    val allPurchases: StateFlow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentEntity>> = documentDao.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PurchaseCategory?>(null)
    val selectedCategory: StateFlow<PurchaseCategory?> = _selectedCategory.asStateFlow()

    // OCR Draft State
    private val _currentOcrDraft = MutableStateFlow<OcrDraftPurchase?>(null)
    val currentOcrDraft: StateFlow<OcrDraftPurchase?> = _currentOcrDraft.asStateFlow()

    // AI Query State
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // UI Feedback Banner
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        NotificationHelper.createNotificationChannel(application)
    }

    // Filtered Purchases list for My Stuff & Search
    val filteredPurchases: StateFlow<List<PurchaseEntity>> = combine(
        allPurchases,
        searchQuery,
        selectedFilter,
        selectedCategory
    ) { purchases, query, filter, category ->
        val now = System.currentTimeMillis()
        var list = purchases

        // 1. Text Query Filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.productName.lowercase().contains(q) ||
                it.brand.lowercase().contains(q) ||
                it.model.lowercase().contains(q) ||
                it.store.lowercase().contains(q) ||
                it.serialNumber.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.itemsSummary.lowercase().contains(q) ||
                "${it.purchasePrice}".contains(q) ||
                it.currency.lowercase().contains(q)
            }
        }

        // 2. Category Filter
        if (category != null) {
            list = list.filter { it.category == category }
        }

        // 3. Status Filters
        when (filter) {
            "Warranty Active" -> list = list.filter {
                it.warrantyEndDate != null && it.warrantyEndDate > now
            }
            "Warranty Expiring" -> list = list.filter {
                it.warrantyEndDate != null && it.warrantyEndDate > now && (it.warrantyEndDate - now) <= (30L * 86400000L)
            }
            "Return Active" -> list = list.filter {
                it.returnEndDate != null && it.returnEndDate > now
            }
            "Return Expiring" -> list = list.filter {
                it.returnEndDate != null && it.returnEndDate > now && (it.returnEndDate - now) <= (3L * 86400000L)
            }
            "Warranty Expired" -> list = list.filter {
                it.warrantyEndDate != null && it.warrantyEndDate <= now
            }
            "Returned" -> list = list.filter {
                it.returnStatus == ReturnStatus.RETURNED
            }
            "Missing Documents" -> list = list.filter {
                it.receiptNumber.isEmpty() && it.primaryImageUri.isEmpty()
            }
            "High Value" -> list = list.filter {
                it.purchasePrice >= 100.0
            }
            "Gifts" -> list = list.filter { it.isGift }
            "Borrowed/Lent" -> list = list.filter { it.isBorrowed || it.isLent }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Priority Attention Items for Home Screen
    val attentionItems: StateFlow<List<AttentionItem>> = allPurchases.map { purchases ->
        val items = mutableListOf<AttentionItem>()
        val now = System.currentTimeMillis()

        for (p in purchases) {
            // Check Return window
            p.returnEndDate?.let { retEnd ->
                if (retEnd > now && p.returnStatus != ReturnStatus.RETURNED) {
                    val daysRemaining = ((retEnd - now) / 86400000L).coerceAtLeast(0)
                    val (label, priority) = when (daysRemaining) {
                        0L -> Pair("Return deadline closes today", 1)
                        1L -> Pair("Return deadline closes tomorrow", 2)
                        else -> Pair("$daysRemaining days remaining", 3)
                    }
                    items.add(
                        AttentionItem(
                            id = "return_${p.id}",
                            purchaseId = p.id,
                            type = AttentionType.RETURN_WINDOW,
                            title = p.productName,
                            subtitle = if (p.store.isNotEmpty()) "Bought from ${p.store}" else "Potential return opportunity",
                            urgencyLabel = label,
                            amountFormatted = String.format(Locale.ENGLISH, "%.2f %s", p.purchasePrice, p.currency),
                            priority = priority
                        )
                    )
                }
            }

            // Check Warranty expiration
            p.warrantyEndDate?.let { warEnd ->
                if (warEnd > now && p.warrantyStatus != WarrantyStatus.CLAIMED) {
                    val daysRemaining = ((warEnd - now) / 86400000L).coerceAtLeast(0)
                    if (daysRemaining <= 30) {
                        val (label, priority) = when {
                            daysRemaining == 0L -> Pair("Warranty expires today", 1)
                            daysRemaining == 1L -> Pair("Warranty expires tomorrow", 2)
                            daysRemaining <= 7 -> Pair("Warranty expires in $daysRemaining days", 3)
                            else -> Pair("Warranty expires in $daysRemaining days", 4)
                        }
                        items.add(
                            AttentionItem(
                                id = "warranty_${p.id}",
                                purchaseId = p.id,
                                type = AttentionType.WARRANTY_EXPIRATION,
                                title = p.productName,
                                subtitle = if (p.brand.isNotEmpty()) "${p.brand} ${p.model}" else "Warranty appears active",
                                urgencyLabel = label,
                                amountFormatted = String.format(Locale.ENGLISH, "%.2f %s", p.purchasePrice, p.currency),
                                priority = priority
                            )
                        )
                    }
                }
            }

            // Large purchase without warranty
            if (p.purchasePrice >= 150.0 && p.warrantyEndDate == null && (p.category == PurchaseCategory.ELECTRONICS || p.category == PurchaseCategory.HOME_APPLIANCES || p.category == PurchaseCategory.COMPUTERS || p.category == PurchaseCategory.PHONES)) {
                items.add(
                    AttentionItem(
                        id = "missing_war_${p.id}",
                        purchaseId = p.id,
                        type = AttentionType.MISSING_WARRANTY,
                        title = p.productName,
                        subtitle = "High value item with no warranty expiration set",
                        urgencyLabel = "Missing warranty info",
                        amountFormatted = String.format(Locale.ENGLISH, "%.2f %s", p.purchasePrice, p.currency),
                        priority = 5
                    )
                )
            }
        }

        items.sortedBy { it.priority }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Money You Can Still Save (Potential Recoverable Value)
    val recoverableSavingsMap: StateFlow<Map<String, Double>> = allPurchases.map { purchases ->
        val now = System.currentTimeMillis()
        val activeReturnPurchases = purchases.filter {
            it.returnEndDate != null && it.returnEndDate > now && it.returnStatus != ReturnStatus.RETURNED
        }
        val map = mutableMapOf<String, Double>()
        for (p in activeReturnPurchases) {
            map[p.currency] = (map[p.currency] ?: 0.0) + p.purchasePrice
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Total Spending by Currency
    val totalSpendingMap: StateFlow<Map<String, Double>> = allPurchases.map { purchases ->
        val map = mutableMapOf<String, Double>()
        for (p in purchases) {
            map[p.currency] = (map[p.currency] ?: 0.0) + p.purchasePrice
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Active Warranties Count
    val activeWarrantiesCount: StateFlow<Int> = allPurchases.map { purchases ->
        val now = System.currentTimeMillis()
        purchases.count { it.warrantyEndDate != null && it.warrantyEndDate > now }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Return Windows Count
    val activeReturnCount: StateFlow<Int> = allPurchases.map { purchases ->
        val now = System.currentTimeMillis()
        purchases.count { it.returnEndDate != null && it.returnEndDate > now && it.returnStatus != ReturnStatus.RETURNED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Warranties Expiring This Month
    val warrantiesExpiringThisMonthCount: StateFlow<Int> = allPurchases.map { purchases ->
        val now = System.currentTimeMillis()
        purchases.count {
            it.warrantyEndDate != null && it.warrantyEndDate > now && (it.warrantyEndDate - now) <= (30L * 86400000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Insights: Category Spending Breakdown
    val categorySpendingList: StateFlow<List<CategorySpending>> = allPurchases.map { purchases ->
        if (purchases.isEmpty()) return@map emptyList()
        val total = purchases.sumOf { it.purchasePrice }.coerceAtLeast(1.0)
        val grouped = purchases.groupBy { it.category }
        grouped.map { (cat, items) ->
            val sum = items.sumOf { it.purchasePrice }
            val primaryCurr = items.firstOrNull()?.currency ?: "JOD"
            CategorySpending(
                category = cat,
                amount = sum,
                currency = primaryCurr,
                count = items.size,
                percentage = (sum / total).toFloat()
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insights: Store Spending Breakdown
    val storeSpendingList: StateFlow<List<StoreSpending>> = allPurchases.map { purchases ->
        val grouped = purchases.filter { it.store.isNotBlank() }.groupBy { it.store.trim() }
        grouped.map { (store, items) ->
            StoreSpending(
                storeName = store,
                amount = items.sumOf { it.purchasePrice },
                currency = items.firstOrNull()?.currency ?: "JOD",
                count = items.size
            )
        }.sortedByDescending { it.amount }.take(8)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Insights: Price Memory for Repeat Items
    val priceMemoryList: StateFlow<List<PriceMemoryComparison>> = allPurchases.map { purchases ->
        val comparisons = mutableListOf<PriceMemoryComparison>()
        val groupedByName = purchases.groupBy { it.productName.trim().lowercase() }
        for ((_, items) in groupedByName) {
            if (items.size >= 2) {
                val sorted = items.sortedBy { it.purchaseDate }
                val prev = sorted[sorted.size - 2]
                val latest = sorted.last()
                if (prev.currency == latest.currency) {
                    comparisons.add(
                        PriceMemoryComparison(
                            productName = latest.productName,
                            previousPrice = prev.purchasePrice,
                            latestPrice = latest.purchasePrice,
                            currency = latest.currency,
                            difference = latest.purchasePrice - prev.purchasePrice,
                            previousDate = prev.purchaseDate,
                            latestDate = latest.purchaseDate
                        )
                    )
                }
            }
        }
        comparisons
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Operations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setCategory(category: PurchaseCategory?) {
        _selectedCategory.value = category
    }

    fun processReceiptText(rawText: String, imageUri: String = "") {
        val draft = OcrParser.parseReceiptText(rawText, imageUri)
        _currentOcrDraft.value = draft
    }

    fun clearOcrDraft() {
        _currentOcrDraft.value = null
    }

    fun savePurchase(
        purchase: PurchaseEntity,
        onSuccess: (Long) -> Unit,
        onLimitReached: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = purchaseDao.getPurchaseCountSync()
            if (purchase.id == 0L && !billingManager.canAddMorePurchases(count)) {
                onLimitReached()
                return@launch
            }

            val id = purchaseDao.insertPurchase(purchase)
            val updatedPurchase = purchase.copy(id = if (purchase.id == 0L) id else purchase.id)

            // Auto-schedule reminders
            val reminders = NotificationHelper.generateRemindersForPurchase(updatedPurchase)
            if (reminders.isNotEmpty()) {
                reminderDao.insertReminders(reminders)
            }

            // Create initial receipt document record if image uri is attached
            if (updatedPurchase.receiptImageUri.isNotEmpty()) {
                documentDao.insertDocument(
                    DocumentEntity(
                        purchaseId = updatedPurchase.id,
                        title = "${updatedPurchase.productName} Receipt",
                        docType = DocumentType.RECEIPT,
                        uriString = updatedPurchase.receiptImageUri,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            _toastMessage.value = "Purchase remembered successfully!"
            onSuccess(updatedPurchase.id)
        }
    }

    fun updatePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseDao.updatePurchase(purchase.copy(updatedAt = System.currentTimeMillis()))
            _toastMessage.value = "Purchase updated."
        }
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseDao.deletePurchase(purchase)
            documentDao.deleteDocumentsForPurchase(purchase.id)
            reminderDao.deleteRemindersForPurchase(purchase.id)
            _toastMessage.value = "Purchase deleted."
        }
    }

    fun addDocument(document: DocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.insertDocument(document)
            _toastMessage.value = "Document saved to vault."
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.deleteDocument(document)
            _toastMessage.value = "Document removed."
        }
    }

    fun askAiAssistant(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiLoading.value = true
            val purchases = allPurchases.value
            val result = AiPurchaseAssistant.answerQueryOffline(query, purchases)
            _aiResponse.value = result
            _isAiLoading.value = false
        }
    }

    fun clearAiResponse() {
        _aiResponse.value = null
    }

    fun exportBackupJson(): String {
        return BackupManager.exportPurchasesToJson(allPurchases.value)
    }

    fun exportBackupCsv(): String {
        return BackupManager.exportPurchasesToCsv(allPurchases.value)
    }

    fun restoreFromJson(jsonString: String, replaceExisting: Boolean, onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parsed = BackupManager.parsePurchasesFromJson(jsonString)
                if (replaceExisting) {
                    purchaseDao.deleteAllPurchases()
                    documentDao.deleteAllDocuments()
                    reminderDao.deleteAllReminders()
                }
                purchaseDao.insertPurchases(parsed)
                _toastMessage.value = "Restored ${parsed.size} purchases successfully."
                onComplete(parsed.size)
            } catch (e: Exception) {
                _toastMessage.value = "Failed to import backup: ${e.localizedMessage}"
                onComplete(0)
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            purchaseDao.deleteAllPurchases()
            documentDao.deleteAllDocuments()
            reminderDao.deleteAllReminders()
            _toastMessage.value = "All purchase records deleted."
            onComplete()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
