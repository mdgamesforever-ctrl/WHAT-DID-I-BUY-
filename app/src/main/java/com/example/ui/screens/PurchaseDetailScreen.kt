package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backup.BackupManager
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.ui.components.ReturnSlipDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.WarrantyClaimDialog
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.getNaturalAge
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.AmberAlertContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseDangerContainer
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(
    purchaseId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val purchases by viewModel.allPurchases.collectAsState()
    val purchase = purchases.firstOrNull { it.id == purchaseId }
    val context = LocalContext.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showWarrantyClaimDialog by remember { mutableStateOf(false) }
    var showReturnSlipDialog by remember { mutableStateOf(false) }

    if (purchase == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Purchase not found.")
        }
        return
    }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    val now = System.currentTimeMillis()

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Purchase?") },
            text = { Text("Are you sure you want to delete '${purchase.productName}' and all linked documents?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchase(purchase)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showWarrantyClaimDialog) {
        WarrantyClaimDialog(
            purchase = purchase,
            onDismiss = { showWarrantyClaimDialog = false }
        )
    }

    if (showReturnSlipDialog) {
        ReturnSlipDialog(
            purchase = purchase,
            onDismiss = { showReturnSlipDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(purchase.id) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE11D48))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(purchase.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = purchase.productName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            if (purchase.brand.isNotEmpty() || purchase.model.isNotEmpty()) {
                                Text(
                                    text = "${purchase.brand} ${purchase.model}".trim(),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format(Locale.ENGLISH, "%.2f %s", purchase.purchasePrice, purchase.currency),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Return status pill
                        purchase.returnEndDate?.let { retEnd ->
                            val days = ((retEnd - now) / 86400000L).coerceAtLeast(0)
                            if (retEnd > now && purchase.returnStatus != ReturnStatus.RETURNED) {
                                StatusPill(
                                    label = "Return: $days d left",
                                    color = if (days <= 2) RoseDanger else EmeraldPrimary,
                                    bgColor = if (days <= 2) RoseDangerContainer else EmeraldContainer
                                )
                            }
                        }

                        // Warranty status pill
                        purchase.warrantyEndDate?.let { warEnd ->
                            val days = ((warEnd - now) / 86400000L).coerceAtLeast(0)
                            if (warEnd > now) {
                                StatusPill(
                                    label = "Warranty: $days d left",
                                    color = if (days <= 30) AmberAlert else EmeraldPrimary,
                                    bgColor = if (days <= 30) AmberAlertContainer else EmeraldContainer
                                )
                            } else {
                                StatusPill(
                                    label = "Warranty Expired",
                                    color = MaterialTheme.colorScheme.outline,
                                    bgColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions: Warranty Claim & Return Slip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showWarrantyClaimDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Warranty Claim", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { showReturnSlipDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prepare Return", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Timeline Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PURCHASE LIFECYCLE TIMELINE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    TimelineItem(
                        title = "Purchased",
                        date = dateFormat.format(Date(purchase.purchaseDate)),
                        subtitle = getNaturalAge(purchase.purchaseDate, now),
                        isCompleted = true,
                        isLast = purchase.returnEndDate == null && purchase.warrantyEndDate == null
                    )

                    purchase.returnEndDate?.let { retEnd ->
                        val isPast = retEnd <= now
                        TimelineItem(
                            title = "Return Deadline",
                            date = dateFormat.format(Date(retEnd)),
                            subtitle = if (isPast) "Window closed" else "${((retEnd - now) / 86400000L)} days remaining",
                            isCompleted = isPast,
                            isLast = purchase.warrantyEndDate == null
                        )
                    }

                    purchase.warrantyEndDate?.let { warEnd ->
                        val isPast = warEnd <= now
                        TimelineItem(
                            title = "Warranty Expiration",
                            date = dateFormat.format(Date(warEnd)),
                            subtitle = if (isPast) "Warranty ended" else "${((warEnd - now) / 86400000L)} days remaining",
                            isCompleted = isPast,
                            isLast = true
                        )
                    }
                }
            }

            // Purchase Specifications & Reference Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RECORD DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )

                    DetailRow("Merchant / Store", purchase.store.ifEmpty { "Not specified" })
                    DetailRow("Category", purchase.category.displayName)
                    DetailRow("Quantity", "${purchase.quantity}")

                    if (purchase.serialNumber.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Serial Number", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(purchase.serialNumber, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Serial", purchase.serialNumber))
                                    Toast.makeText(context, "Serial number copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Serial", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    if (purchase.orderNumber.isNotEmpty()) {
                        DetailRow("Order Number", purchase.orderNumber)
                    }

                    if (purchase.receiptNumber.isNotEmpty()) {
                        DetailRow("Receipt / Invoice #", purchase.receiptNumber)
                    }

                    if (purchase.isGift) {
                        DetailRow("Gift Recipient", purchase.giftRecipient.ifEmpty { "Yes (Gift)" })
                    }

                    if (purchase.isBorrowed || purchase.isLent) {
                        DetailRow(if (purchase.isBorrowed) "Borrowed From" else "Lent To", purchase.contactName.ifEmpty { "Contact" })
                    }

                    if (purchase.notes.isNotEmpty()) {
                        DetailRow("Notes", purchase.notes)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    date: String,
    subtitle: String,
    isCompleted: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = "$date • $subtitle", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
