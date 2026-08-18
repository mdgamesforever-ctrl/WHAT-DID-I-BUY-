package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.ui.components.AttentionCard
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MoneySavedHeroCard
import com.example.ui.components.PurchaseItemCard
import com.example.ui.components.ScanReceiptDialog
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPurchaseDetail: (Long) -> Unit,
    onNavigateToAddPurchase: () -> Unit,
    onNavigateToOcrReview: () -> Unit,
    onNavigateToAskAi: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToMyStuff: () -> Unit
) {
    val purchases by viewModel.allPurchases.collectAsState()
    val attentionItems by viewModel.attentionItems.collectAsState()
    val recoverableSavings by viewModel.recoverableSavingsMap.collectAsState()
    val activeWarrantiesCount by viewModel.activeWarrantiesCount.collectAsState()
    val activeReturnCount by viewModel.activeReturnCount.collectAsState()
    val totalSpendingMap by viewModel.totalSpendingMap.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()

    var showScanDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    if (showScanDialog) {
        ScanReceiptDialog(
            onDismiss = { showScanDialog = false },
            onReceiptTextSubmitted = { text ->
                showScanDialog = false
                viewModel.processReceiptText(text)
                onNavigateToOcrReview()
            }
        )
    }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeDialog = false },
            onBarcodeResolved = { name, brand, model ->
                showBarcodeDialog = false
                viewModel.processReceiptText("""
                    PRODUCT LOOKUP
                    -----------------
                    $name
                    Brand: $brand
                    Model: $model
                    Price: 0.00 JOD
                    Warranty: 1 Year
                    Return: 14 Days
                """.trimIndent())
                onNavigateToOcrReview()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "What Did I Buy?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToAskAi,
                        modifier = Modifier.testTag("home_ask_ai_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Purchases",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!subscriptionState.isPremium) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { onNavigateToPremium() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GO PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Money You Can Still Save Hero Card
            item {
                MoneySavedHeroCard(
                    savingsMap = recoverableSavings,
                    onViewReturns = onNavigateToMyStuff
                )
            }

            // 2. Quick Action Bar
            item {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionPill(
                            title = "Scan Receipt",
                            icon = Icons.Default.DocumentScanner,
                            color = EmeraldPrimary,
                            onClick = { showScanDialog = true },
                            tag = "quick_scan_receipt"
                        )
                    }
                    item {
                        QuickActionPill(
                            title = "Scan Barcode",
                            icon = Icons.Default.QrCodeScanner,
                            color = Color(0xFF2563EB),
                            onClick = { showBarcodeDialog = true },
                            tag = "quick_scan_barcode"
                        )
                    }
                    item {
                        QuickActionPill(
                            title = "Add Manually",
                            icon = Icons.Default.Add,
                            color = Color(0xFF7C3AED),
                            onClick = onNavigateToAddPurchase,
                            tag = "quick_add_manual"
                        )
                    }
                    item {
                        QuickActionPill(
                            title = "Ask AI",
                            icon = Icons.Default.AutoAwesome,
                            color = Color(0xFFD97706),
                            onClick = onNavigateToAskAi,
                            tag = "quick_ask_ai"
                        )
                    }
                }
            }

            // 3. Priority Attention Section (if any urgent items)
            if (attentionItems.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE11D48))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATTENTION NEEDED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48),
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Text(
                            text = "${attentionItems.size} Actionable",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                items(attentionItems.take(3), key = { it.id }) { item ->
                    AttentionCard(
                        item = item,
                        onClickView = { onNavigateToPurchaseDetail(item.purchaseId) }
                    )
                }
            }

            // 4. Key Metrics Grid
            item {
                Text(
                    text = "PORTFOLIO OVERVIEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Active Warranties",
                        value = "$activeWarrantiesCount",
                        icon = Icons.Default.Shield,
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Return Windows",
                        value = "$activeReturnCount",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Total Purchases",
                        value = "${purchases.size}",
                        icon = Icons.Default.ShoppingBag,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Recent Purchases
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT PURCHASES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    if (purchases.isNotEmpty()) {
                        TextButton(onClick = onNavigateToMyStuff) {
                            Text("See All (${purchases.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (purchases.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ShoppingBag,
                        title = "No Purchases Logged Yet",
                        description = "Start remembering your purchases, warranties, and return deadlines today.",
                        buttonText = "Add Your First Purchase",
                        onButtonClick = onNavigateToAddPurchase
                    )
                }
            } else {
                items(purchases.take(6), key = { it.id }) { purchase ->
                    PurchaseItemCard(
                        purchase = purchase,
                        onClick = { onNavigateToPurchaseDetail(purchase.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuickActionPill(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .testTag(tag)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
