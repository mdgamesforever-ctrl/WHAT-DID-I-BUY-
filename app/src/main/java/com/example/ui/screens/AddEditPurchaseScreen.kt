package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPurchaseScreen(
    purchaseId: Long?,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val context = LocalContext.current
    val purchases by viewModel.allPurchases.collectAsState()
    val existingPurchase = remember(purchaseId, purchases) {
        if (purchaseId != null && purchaseId > 0) purchases.firstOrNull { it.id == purchaseId } else null
    }

    var productName by remember { mutableStateOf(existingPurchase?.productName ?: "") }
    var brand by remember { mutableStateOf(existingPurchase?.brand ?: "") }
    var model by remember { mutableStateOf(existingPurchase?.model ?: "") }
    var category by remember { mutableStateOf(existingPurchase?.category ?: PurchaseCategory.OTHER) }
    var store by remember { mutableStateOf(existingPurchase?.store ?: "") }
    var priceText by remember { mutableStateOf(if (existingPurchase != null && existingPurchase.purchasePrice > 0) "${existingPurchase.purchasePrice}" else "") }
    var currency by remember { mutableStateOf(existingPurchase?.currency ?: "JOD") }
    var quantityText by remember { mutableStateOf("${existingPurchase?.quantity ?: 1}") }
    var serialNumber by remember { mutableStateOf(existingPurchase?.serialNumber ?: "") }
    var orderNumber by remember { mutableStateOf(existingPurchase?.orderNumber ?: "") }
    var receiptNumber by remember { mutableStateOf(existingPurchase?.receiptNumber ?: "") }
    var notes by remember { mutableStateOf(existingPurchase?.notes ?: "") }

    // Warranty & Return configurations
    var warrantyDurationMonths by remember { mutableStateOf(existingPurchase?.warrantyDurationMonths ?: 12) }
    var returnPeriodDays by remember { mutableStateOf(existingPurchase?.returnPeriodDays ?: 14) }

    // Flags
    var isGift by remember { mutableStateOf(existingPurchase?.isGift ?: false) }
    var giftRecipient by remember { mutableStateOf(existingPurchase?.giftRecipient ?: "") }
    var isBorrowedOrLent by remember { mutableStateOf((existingPurchase?.isBorrowed == true || existingPurchase?.isLent == true)) }
    var contactName by remember { mutableStateOf(existingPurchase?.contactName ?: "") }

    var showLimitDialog by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }

    val currencies = listOf("JOD", "USD", "EUR", "GBP", "SAR", "AED", "EGP", "KWD", "QAR")

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Free Purchase Limit Reached") },
            text = { Text("You've reached the free tier limit of 30 stored purchases. Upgrade to Premium for unlimited purchases, AI assistant, and full backup features.") },
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
                title = { Text(if (existingPurchase != null) "Edit Purchase" else "Add Purchase", fontWeight = FontWeight.Bold) },
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
            // Product Name & Category
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name *") },
                placeholder = { Text("e.g. Samsung 55\" OLED TV, iPhone 15 Pro") },
                modifier = Modifier.fillMaxWidth().testTag("add_product_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    placeholder = { Text("e.g. Samsung, Apple") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    placeholder = { Text("e.g. S24 Ultra") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Picker
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    PurchaseCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Store & Pricing
            OutlinedTextField(
                value = store,
                onValueChange = { store = it },
                label = { Text("Store / Merchant") },
                placeholder = { Text("e.g. Carrefour, Amazon, City Mall") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Purchase Price") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.5f).testTag("add_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = currencyExpanded,
                    onExpandedChange = { currencyExpanded = !currencyExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false }
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    currency = curr
                                    currencyExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Warranty Presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Warranty Duration", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "None", 6 to "6 Mo", 12 to "1 Yr", 24 to "2 Yrs", 36 to "3 Yrs").forEach { (months, label) ->
                            val isSel = warrantyDurationMonths == months
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).clickable { warrantyDurationMonths = months }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Return Period Presets
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Return Window Policy", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "None", 7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
                            val isSel = returnPeriodDays == days
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).clickable { returnPeriodDays = days }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Reference Identifiers
            OutlinedTextField(
                value = serialNumber,
                onValueChange = { serialNumber = it },
                label = { Text("Serial Number (S/N)") },
                placeholder = { Text("e.g. SN-892019482") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("Order Number") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("Receipt / Invoice #") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Gift / Lent Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Is this item a gift?", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Switch(checked = isGift, onCheckedChange = { isGift = it })
            }
            if (isGift) {
                OutlinedTextField(
                    value = giftRecipient,
                    onValueChange = { giftRecipient = it },
                    label = { Text("Gift Recipient Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("e.g. Bought with discount coupon, stored in living room") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (productName.isBlank()) {
                        Toast.makeText(context, "Product name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val qty = quantityText.toIntOrNull() ?: 1
                    val now = System.currentTimeMillis()

                    val returnEnd = if (returnPeriodDays > 0) {
                        Calendar.getInstance().apply {
                            timeInMillis = existingPurchase?.purchaseDate ?: now
                            add(Calendar.DAY_OF_YEAR, returnPeriodDays)
                        }.timeInMillis
                    } else null

                    val warrantyEnd = if (warrantyDurationMonths > 0) {
                        Calendar.getInstance().apply {
                            timeInMillis = existingPurchase?.purchaseDate ?: now
                            add(Calendar.MONTH, warrantyDurationMonths)
                        }.timeInMillis
                    } else null

                    val toSave = PurchaseEntity(
                        id = existingPurchase?.id ?: 0L,
                        productName = productName.trim(),
                        brand = brand.trim(),
                        model = model.trim(),
                        category = category,
                        store = store.trim(),
                        purchaseDate = existingPurchase?.purchaseDate ?: now,
                        purchasePrice = price,
                        currency = currency,
                        quantity = qty,
                        serialNumber = serialNumber.trim(),
                        orderNumber = orderNumber.trim(),
                        receiptNumber = receiptNumber.trim(),
                        warrantyStartDate = if (warrantyDurationMonths > 0) (existingPurchase?.purchaseDate ?: now) else null,
                        warrantyEndDate = warrantyEnd,
                        warrantyDurationMonths = warrantyDurationMonths,
                        warrantyStatus = if (warrantyDurationMonths > 0) WarrantyStatus.ACTIVE else WarrantyStatus.UNKNOWN,
                        returnStartDate = if (returnPeriodDays > 0) (existingPurchase?.purchaseDate ?: now) else null,
                        returnEndDate = returnEnd,
                        returnPeriodDays = returnPeriodDays,
                        returnStatus = if (returnPeriodDays > 0) ReturnStatus.ACTIVE else ReturnStatus.UNKNOWN,
                        notes = notes.trim(),
                        isGift = isGift,
                        giftRecipient = giftRecipient.trim(),
                        isBorrowed = isBorrowedOrLent,
                        contactName = contactName.trim(),
                        createdAt = existingPurchase?.createdAt ?: now,
                        updatedAt = now
                    )

                    viewModel.savePurchase(
                        purchase = toSave,
                        onSuccess = { onNavigateBack() },
                        onLimitReached = { showLimitDialog = true }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_purchase_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Purchase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
