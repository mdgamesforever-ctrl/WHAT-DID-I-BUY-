package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.OnRoseContainer
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseDangerContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.viewmodel.AttentionItem
import com.example.ui.viewmodel.AttentionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoneySavedHeroCard(
    savingsMap: Map<String, Double>,
    modifier: Modifier = Modifier,
    onViewReturns: () -> Unit = {}
) {
    val totalDisplay = if (savingsMap.isEmpty()) {
        "0.00 JD"
    } else {
        savingsMap.entries.joinToString(" + ") { (curr, sum) ->
            String.format(Locale.ENGLISH, "%.2f %s", sum, curr)
        }
    }
    val hasActiveSavings = savingsMap.values.any { it > 0.0 }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("money_saved_hero_card")
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = if (hasActiveSavings) listOf(
                            Color(0xFF0F172A),
                            Color(0xFF134E4A),
                            Color(0xFF0F766E)
                        ) else listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B),
                            Color(0xFF334155)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (hasActiveSavings) EmeraldPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasActiveSavings) Icons.Default.Shield else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasActiveSavings) Color(0xFF5EEAD4) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MONEY YOU CAN STILL SAVE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = if (hasActiveSavings) Color(0xFF5EEAD4) else Color(0xFF94A3B8)
                            )
                        )
                    }

                    if (hasActiveSavings) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF14B8A6).copy(alpha = 0.2f),
                            modifier = Modifier.clickable { onViewReturns() }
                        ) {
                            Text(
                                text = "Actionable",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF5EEAD4),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = totalDisplay,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (hasActiveSavings)
                        "Potential recoverable value based on active return opportunities."
                    else
                        "Nothing urgent right now. Your purchases and deadlines are in order.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

private data class AttentionCardStyle(
    val icon: ImageVector,
    val tint: Color,
    val bgColor: Color,
    val textTint: Color
)

@Composable
fun AttentionCard(
    item: AttentionItem,
    onClickView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = when (item.type) {
        AttentionType.RETURN_WINDOW -> if (item.priority <= 2) {
            AttentionCardStyle(Icons.Default.AccessTime, RoseDanger, RoseDangerContainer, OnRoseContainer)
        } else {
            AttentionCardStyle(Icons.Default.AccessTime, AmberAlert, AmberAlertContainer, OnAmberContainer)
        }
        AttentionType.WARRANTY_EXPIRATION -> if (item.priority <= 2) {
            AttentionCardStyle(Icons.Default.Shield, RoseDanger, RoseDangerContainer, OnRoseContainer)
        } else {
            AttentionCardStyle(Icons.Default.Shield, AmberAlert, AmberAlertContainer, OnAmberContainer)
        }
        AttentionType.MISSING_WARRANTY -> AttentionCardStyle(Icons.Default.Warning, AmberAlert, AmberAlertContainer, OnAmberContainer)
        AttentionType.DOCUMENT_ATTENTION -> AttentionCardStyle(Icons.Default.Receipt, Color(0xFF2563EB), Color(0xFFDBEAFE), Color(0xFF1E40AF))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attention_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(style.tint.copy(alpha = 0.4f), Color.Transparent)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(style.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.textTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.urgencyLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = style.textTint,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onClickView,
                modifier = Modifier.testTag("view_attention_btn_${item.purchaseId}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "VIEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun PurchaseItemCard(
    purchase: PurchaseEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val ageText = getNaturalAge(purchase.purchaseDate, now)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("purchase_card_${purchase.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(purchase.category),
                        contentDescription = purchase.category.displayName,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = purchase.productName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (purchase.store.isNotEmpty()) "${purchase.store} • $ageText" else ageText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format(Locale.ENGLISH, "%.2f %s", purchase.purchasePrice, purchase.currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pills row for Warranty and Return statuses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return Status Pill
                purchase.returnEndDate?.let { retEnd ->
                    if (retEnd > now && purchase.returnStatus != ReturnStatus.RETURNED) {
                        val daysRemaining = ((retEnd - now) / 86400000L).coerceAtLeast(0)
                        StatusPill(
                            label = "Return: $daysRemaining d left",
                            color = if (daysRemaining <= 2) RoseDanger else EmeraldPrimary,
                            bgColor = if (daysRemaining <= 2) RoseDangerContainer else EmeraldContainer
                        )
                    }
                }

                // Warranty Status Pill
                purchase.warrantyEndDate?.let { warEnd ->
                    if (warEnd > now) {
                        val daysRemaining = ((warEnd - now) / 86400000L).coerceAtLeast(0)
                        StatusPill(
                            label = if (daysRemaining > 30) "Warranty: Active" else "Warranty: $daysRemaining d left",
                            color = if (daysRemaining <= 30) AmberAlert else EmeraldPrimary,
                            bgColor = if (daysRemaining <= 30) AmberAlertContainer else EmeraldContainer
                        )
                    } else {
                        StatusPill(
                            label = "Warranty Expired",
                            color = MaterialTheme.colorScheme.outline,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                if (purchase.isGift) {
                    StatusPill(
                        label = "Gift 🎁",
                        color = Color(0xFF7C3AED),
                        bgColor = Color(0xFFEDE9FE)
                    )
                }

                if (purchase.isBorrowed || purchase.isLent) {
                    StatusPill(
                        label = if (purchase.isBorrowed) "Borrowed" else "Lent",
                        color = Color(0xFF0284C7),
                        bgColor = Color(0xFFE0F2FE)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onButtonClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getNaturalAge(timestamp: Long, now: Long): String {
    val diffDays = ((now - timestamp) / 86400000L).coerceAtLeast(0)
    return when {
        diffDays == 0L -> "Purchased today"
        diffDays == 1L -> "Purchased yesterday"
        diffDays < 30 -> "Purchased $diffDays days ago"
        diffDays < 365 -> "Purchased ${diffDays / 30} months ago"
        else -> "Purchased ${diffDays / 365} years ago"
    }
}

fun getCategoryIcon(category: PurchaseCategory): ImageVector {
    return when (category) {
        PurchaseCategory.ELECTRONICS -> Icons.Default.Devices
        PurchaseCategory.PHONES -> Icons.Default.Smartphone
        PurchaseCategory.COMPUTERS -> Icons.Default.Laptop
        PurchaseCategory.HOME_APPLIANCES -> Icons.Default.Kitchen
        PurchaseCategory.FURNITURE -> Icons.Default.Chair
        PurchaseCategory.CLOTHING -> Icons.Default.Checkroom
        PurchaseCategory.SHOES -> Icons.Default.ShoppingBag
        PurchaseCategory.TOOLS -> Icons.Default.Build
        PurchaseCategory.VEHICLES -> Icons.Default.DirectionsCar
        PurchaseCategory.VEHICLE_PARTS -> Icons.Default.Settings
        PurchaseCategory.GAMING -> Icons.Default.SportsEsports
        PurchaseCategory.SUBSCRIPTIONS -> Icons.Default.Autorenew
        PurchaseCategory.SOFTWARE -> Icons.Default.Code
        PurchaseCategory.FOOD -> Icons.Default.Restaurant
        PurchaseCategory.HEALTH_PERSONAL_CARE -> Icons.Default.Favorite
        PurchaseCategory.BEAUTY -> Icons.Default.Face
        PurchaseCategory.CHILDREN -> Icons.Default.Face
        PurchaseCategory.PET -> Icons.Default.Pets
        PurchaseCategory.HOME -> Icons.Default.Home
        PurchaseCategory.OFFICE -> Icons.Default.Work
        PurchaseCategory.OTHER -> Icons.Default.Category
    }
}
