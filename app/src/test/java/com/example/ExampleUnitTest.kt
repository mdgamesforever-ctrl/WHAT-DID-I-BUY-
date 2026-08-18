package com.example

import com.example.ai.AiPurchaseAssistant
import com.example.backup.BackupManager
import com.example.data.local.entity.PurchaseEntity
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus
import com.example.ocr.OcrParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOcrParserReceiptExtraction() {
        val sampleReceipt = """
            CARREFOUR EXPRESS
            Tax Invoice / Receipt: #INV-98231
            Date: 15/05/2026
            
            Samsung 55 Inch OLED TV 4K      450.00 JOD
            HDMI 2.1 Ultra High Speed Cable   15.00 JOD
            
            Subtotal: 465.00 JOD
            TOTAL: 465.00 JOD
            
            Warranty: 2 Years Official Agent
            Return Policy: 14 Days with original receipt
            S/N: SAM-9821739182
        """.trimIndent()

        val draft = OcrParser.parseReceiptText(sampleReceipt)
        
        assertEquals("CARREFOUR EXPRESS", draft.storeName)
        assertEquals(465.00, draft.totalPrice, 0.01)
        assertEquals("JOD", draft.currency)
        assertEquals(24, draft.warrantyDurationMonths)
        assertEquals(14, draft.returnPeriodDays)
        assertEquals("SAM-9821739182", draft.serialNumber)
        assertEquals("INV-98231", draft.invoiceNumber)
        assertTrue(draft.detectedItems.isNotEmpty())
    }

    @Test
    fun testAiPurchaseAssistantQueries() {
        val now = System.currentTimeMillis()
        val testPurchases = listOf(
            PurchaseEntity(
                id = 1L,
                productName = "MacBook Pro M3",
                brand = "Apple",
                category = PurchaseCategory.ELECTRONICS,
                store = "Apple Store",
                purchaseDate = now - (10L * 86400000L),
                purchasePrice = 1800.0,
                currency = "USD",
                warrantyEndDate = now + (350L * 86400000L),
                warrantyDurationMonths = 12,
                warrantyStatus = WarrantyStatus.ACTIVE,
                returnEndDate = now + (4L * 86400000L),
                returnPeriodDays = 14,
                returnStatus = ReturnStatus.ACTIVE
            ),
            PurchaseEntity(
                id = 2L,
                productName = "Nike Air Zoom",
                brand = "Nike",
                category = PurchaseCategory.CLOTHING,
                store = "City Mall",
                purchaseDate = now - (30L * 86400000L),
                purchasePrice = 120.0,
                currency = "USD",
                warrantyEndDate = null,
                warrantyStatus = WarrantyStatus.UNKNOWN,
                returnEndDate = now - (16L * 86400000L),
                returnPeriodDays = 14,
                returnStatus = ReturnStatus.EXPIRED
            )
        )

        // Query active returns
        val returnAnswer = AiPurchaseAssistant.answerQuery("Which purchases can I still return?", testPurchases)
        assertTrue(returnAnswer.contains("MacBook Pro M3"))

        // Query active warranties
        val warrantyAnswer = AiPurchaseAssistant.answerQuery("What warranties are active?", testPurchases)
        assertTrue(warrantyAnswer.contains("MacBook Pro M3"))

        // Query category spending
        val spendAnswer = AiPurchaseAssistant.answerQuery("How much did I spend on electronics?", testPurchases)
        assertTrue(spendAnswer.contains("1800.00 USD"))
    }

    @Test
    fun testBackupManagerExportAndClaimPackage() {
        val testPurchase = PurchaseEntity(
            id = 1L,
            productName = "LG Smart Inverter Refrigerator",
            brand = "LG",
            model = "GN-B392SLCL",
            category = PurchaseCategory.APPLIANCES,
            store = "Leaders Center",
            purchaseDate = System.currentTimeMillis() - (60L * 86400000L),
            purchasePrice = 650.0,
            currency = "JOD",
            serialNumber = "LG-99210482",
            warrantyEndDate = System.currentTimeMillis() + (300L * 86400000L),
            warrantyDurationMonths = 12,
            warrantyStatus = WarrantyStatus.ACTIVE
        )

        val claimPkg = BackupManager.generateWarrantyClaimPackage(testPurchase, emptyList())
        assertTrue(claimPkg.contains("WARRANTY SERVICE CLAIM PACKAGE"))
        assertTrue(claimPkg.contains("LG-99210482"))
        assertTrue(claimPkg.contains("Leaders Center"))

        val jsonExport = BackupManager.exportToJson(listOf(testPurchase), emptyList())
        assertTrue(jsonExport.contains("LG Smart Inverter Refrigerator"))
    }
}

