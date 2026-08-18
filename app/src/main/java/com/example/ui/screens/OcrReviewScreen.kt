package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberAlertContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReviewScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val draft by viewModel.currentOcrDraft.collectAsState()
    val context = LocalContext.current

    if (draft == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No draft to review.")
        }
        return
    }

    val d = draft!!

    var productName by remember { mutableStateOf(d.primaryProductName) }
    var storeName by remember { mutableStateOf(d.storeName) }
    var totalPriceText by remember { mutableStateOf(if (d.totalPrice > 0) "${d.totalPrice}" else "") }
    var currency by remember { mutableStateOf(d.currency) }
    var serialNumber by remember { mutableStateOf(d.serialNumber) }
    var invoiceNumber by remember { mutableStateOf(d.invoiceNumber) }
    var warrantyDurationMonths by remember { mutableStateOf(d.warrantyDurationMonths) }
    var returnPeriodDays by remember { mutableStateOf(d.returnPeriodDays) }

    var showLimitDialog by remember { mutableStateOf(false) }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Free Purchase Limit Reached") },
            text = { Text("You've reached the free tier limit of 30 stored purchases. Upgrade to Premium for unlimited purchases.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLimitDialog = false
                        onNavigateToPremium()
                    }
                ) {
                    Text("Upgrade to Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) { Text("Not Now") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scanned Purchase", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Receipt scanned successfully. Please review any flagged fields before saving.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Primary Product Name Field
            FieldWithConfidence(
                label = "Product Name *",
                value = productName,
                onValueChange = { productName = it },
                isConfident = d.isConfidentItems,
                tag = "review_product_name"
            )

            // Store Name Field
            FieldWithConfidence(
                label = "Store / Merchant",
                value = storeName,
                onValueChange = { storeName = it },
                isConfident = d.isConfidentStore,
                tag = "review_store_name"
            )

            // Price & Currency
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1.5f)) {
                    FieldWithConfidence(
                        label = "Total Price",
                        value = totalPriceText,
                        onValueChange = { totalPriceText = it },
                        isConfident = d.isConfidentTotal,
                        tag = "review_price"
                    )
                }
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Detected Items breakdown
            if (d.detectedItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DETECTED ITEMS (${d.detectedItems.size})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        d.detectedItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                                Text("${item.price} $currency", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Return and Warranty
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = "$returnPeriodDays",
                    onValueChange = { returnPeriodDays = it.toIntOrNull() ?: 0 },
                    label = { Text("Return Window (Days)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = "$warrantyDurationMonths",
                    onValueChange = { warrantyDurationMonths = it.toIntOrNull() ?: 0 },
                    label = { Text("Warranty (Months)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Serial Number & Invoice
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Serial Number (S/N)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice / Receipt #") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (productName.isBlank()) {
                        Toast.makeText(context, "Product name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val price = totalPriceText.toDoubleOrNull() ?: 0.0
                    val now = System.currentTimeMillis()
                    val purchaseDate = d.purchaseDateMillis

                    val returnEnd = if (returnPeriodDays > 0) {
                        Calendar.getInstance().apply {
                            timeInMillis = purchaseDate
                            add(Calendar.DAY_OF_YEAR, returnPeriodDays)
                        }.timeInMillis
                    } else null

                    val warrantyEnd = if (warrantyDurationMonths > 0) {
                        Calendar.getInstance().apply {
                            timeInMillis = purchaseDate
                            add(Calendar.MONTH, warrantyDurationMonths)
                        }.timeInMillis
                    } else null

                    val purchase = PurchaseEntity(
                        productName = productName.trim(),
                        store = storeName.trim(),
                        category = d.suggestedCategory,
                        purchaseDate = purchaseDate,
                        purchasePrice = price,
                        currency = currency,
                        serialNumber = serialNumber.trim(),
                        receiptNumber = invoiceNumber.trim(),
                        orderNumber = invoiceNumber.trim(),
                        warrantyStartDate = if (warrantyDurationMonths > 0) purchaseDate else null,
                        warrantyEndDate = warrantyEnd,
                        warrantyDurationMonths = warrantyDurationMonths,
                        warrantyStatus = if (warrantyDurationMonths > 0) WarrantyStatus.ACTIVE else WarrantyStatus.UNKNOWN,
                        returnStartDate = if (returnPeriodDays > 0) purchaseDate else null,
                        returnEndDate = returnEnd,
                        returnPeriodDays = returnPeriodDays,
                        returnStatus = if (returnPeriodDays > 0) ReturnStatus.ACTIVE else ReturnStatus.UNKNOWN,
                        itemsSummary = d.detectedItems.joinToString(", ") { it.name },
                        receiptImageUri = d.imageUri,
                        createdAt = now,
                        updatedAt = now
                    )

                    viewModel.savePurchase(
                        purchase = purchase,
                        onSuccess = {
                            viewModel.clearOcrDraft()
                            onNavigateToHome()
                        },
                        onLimitReached = { showLimitDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_reviewed_purchase_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm & Save Purchase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = {
                    viewModel.clearOcrDraft()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discard & Scan Again")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FieldWithConfidence(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isConfident: Boolean,
    tag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().testTag(tag),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        if (!isConfident) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AmberAlert,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Please check this",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AmberAlert,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
