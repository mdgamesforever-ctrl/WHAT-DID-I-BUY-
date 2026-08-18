package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun ScanReceiptDialog(
    onDismiss: () -> Unit,
    onReceiptTextSubmitted: (String) -> Unit
) {
    var rawInput by remember { mutableStateOf("") }

    val sampleReceipt1 = """
        CARREFOUR CITY MALL
        Tax Invoice # 849204
        Date: 2026-08-10
        ------------------------------
        SAMSUNG 55 INCH 4K OLED TV    450.00
        HDMI 2.1 CABLE HIGH SPEED      15.00
        WALL MOUNT BRACKET             25.00
        ------------------------------
        TOTAL DUE                     490.00 JOD
        
        S/N: SAM55OLED-998822
        Warranty: 2 Years Official Warranty
        Return policy: 14 days exchange / return with original invoice.
        Thank you for shopping with Carrefour!
    """.trimIndent()

    val sampleReceipt2 = """
        كارفور الأردن - سيتي مول
        فاتورة ضريبية رقم 392019
        التاريخ: 15/08/2026
        ------------------------------
        غسالة سامسونج 9 كجم          380.00
        مسحوق غسيل أوتوماتيك          12.50
        ------------------------------
        المجموع الإجمالي              392.50 د.أ
        
        الرقم التسلسلي: S/N-WM992184
        ضمان لمدة 2 سنة (24 شهر)
        سياسة الاسترجاع: استرجاع خلال 14 يوم
        شكراً لتسوقكم معنا
    """.trimIndent()

    val sampleReceipt3 = """
        APPLE STORE ME
        Order # AP-9920184
        Date: 2026-08-01
        ------------------------------
        MacBook Pro 14 M3 Pro         1650.00 USD
        AppleCare+ Protection Plan     199.00 USD
        ------------------------------
        TOTAL                         1849.00 USD
        
        Serial Number: C02G99ABCD88
        Warranty: 1 Year Limited Hardware Warranty
        Return Policy: 14 days return policy
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("scan_receipt_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Scan or Paste Receipt",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Point camera at receipt or paste OCR text. You can also pick a realistic sample to test instant parsing.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Sample Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip("Carrefour (EN)", onClick = { rawInput = sampleReceipt1 })
                    PresetChip("كارفور (AR)", onClick = { rawInput = sampleReceipt2 })
                    PresetChip("Apple Store", onClick = { rawInput = sampleReceipt3 })
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text("Receipt Text") },
                    placeholder = { Text("Paste receipt contents here or choose a sample above...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("receipt_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawInput.isNotBlank()) {
                        onReceiptTextSubmitted(rawInput)
                    }
                },
                enabled = rawInput.isNotBlank(),
                modifier = Modifier.testTag("process_receipt_btn")
            ) {
                Text("Process Receipt", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeResolved: (productName: String, brand: String, model: String) -> Unit
) {
    var barcodeInput by remember { mutableStateOf("") }

    val barcodeDatabase = mapOf(
        "8806091234567" to Triple("Galaxy S24 Ultra 512GB", "Samsung", "SM-S928B"),
        "194253998811" to Triple("iPhone 15 Pro 256GB Titanium", "Apple", "A3102"),
        "4902370550306" to Triple("Nintendo Switch OLED Model", "Nintendo", "HEG-001"),
        "0885909950799" to Triple("Sony WH-1000XM5 Wireless Headphones", "Sony", "WH1000XM5/B"),
        "8806098765432" to Triple("EcoBubble Front Load Washer 9kg", "Samsung", "WW90TA046AX"),
        "7613034988771" to Triple("Nespresso Vertuo Pop Coffee Machine", "Nespresso", "ENV90.B")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("barcode_scanner_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Scan Product Barcode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Scan or enter a product EAN/UPC barcode number to auto-populate product specifications.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Quick Barcode Samples:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PresetChip("Galaxy S24", onClick = { barcodeInput = "8806091234567" })
                    PresetChip("iPhone 15", onClick = { barcodeInput = "194253998811" })
                    PresetChip("Switch OLED", onClick = { barcodeInput = "4902370550306" })
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text("Barcode Number") },
                    placeholder = { Text("e.g. 8806091234567") },
                    modifier = Modifier.fillMaxWidth().testTag("barcode_number_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resolved = barcodeDatabase[barcodeInput.trim()]
                    if (resolved != null) {
                        onBarcodeResolved(resolved.first, resolved.second, resolved.third)
                    } else {
                        onBarcodeResolved("Scanned Item #$barcodeInput", "Generic", "")
                    }
                },
                enabled = barcodeInput.isNotBlank()
            ) {
                Text("Lookup Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WarrantyClaimDialog(
    purchase: PurchaseEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var problemDescription by remember {
        mutableStateOf("Device screen displays horizontal flickering lines and unit powers down unexpectedly under normal operation.")
    }

    val claimPackageText = remember(problemDescription) {
        BackupManager.buildWarrantyClaimText(purchase, problemDescription, emptyList())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Warranty Claim Package", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Describe the defect or problem. The app compiles all purchase proofs and serials into a clean claim package.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it },
                    label = { Text("Problem Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Claim Preview:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = claimPackageText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    BackupManager.shareText(context, "Warranty Claim - ${purchase.productName}", claimPackageText)
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share / Export Claim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ReturnSlipDialog(
    purchase: PurchaseEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val returnSlipText = remember(purchase) {
        BackupManager.buildReturnSlipText(purchase)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Return Slip Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = returnSlipText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    BackupManager.shareText(context, "Return Slip - ${purchase.productName}", returnSlipText)
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Return Slip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
