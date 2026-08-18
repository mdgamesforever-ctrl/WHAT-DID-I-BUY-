package com.example.billing

/**
 * ============================================================
 * GOOGLE PLAY SUBSCRIPTION CONFIGURATION
 * ============================================================
 *
 * To connect to Google Play Console:
 * 1. Open Google Play Console -> Monetize -> Products -> Subscriptions
 * 2. Create your Monthly subscription base plan.
 * 3. Create your Yearly subscription base plan.
 * 4. Copy the Product IDs and replace the placeholders below.
 *
 * DO NOT scatter Product IDs throughout the codebase.
 * This is the SINGLE isolated configuration file.
 * ============================================================
 */
object BillingConfig {
    /**
     * Replace with the exact Product ID created in Google Play Console
     * before building the release AAB/APK.
     */
    const val SUBSCRIPTION_MONTHLY_ID = "REPLACE_WITH_PLAY_CONSOLE_MONTHLY_ID"

    /**
     * Replace with the exact Product ID created in Google Play Console
     * before building the release AAB/APK.
     */
    const val SUBSCRIPTION_YEARLY_ID = "REPLACE_WITH_PLAY_CONSOLE_YEARLY_ID"

    /**
     * Free tier allowance. Users can store up to this many purchases
     * without requiring a subscription.
     */
    const val FREE_TIER_PURCHASE_LIMIT = 30

    // Display / Fallback pricing metadata when Play Billing is connecting
    const val DEFAULT_MONTHLY_DISPLAY_PRICE = "$4.99 / month"
    const val DEFAULT_YEARLY_DISPLAY_PRICE = "$39.99 / year"
    const val DEFAULT_YEARLY_SAVINGS_PERCENT = "33% OFF"
}

enum class SubscriptionState {
    FREE,
    ACTIVE_MONTHLY,
    ACTIVE_YEARLY,
    PENDING,
    GRACE_PERIOD,
    EXPIRED,
    CANCELLED,
    UNKNOWN;

    val isPremium: Boolean
        get() = this == ACTIVE_MONTHLY || this == ACTIVE_YEARLY || this == GRACE_PERIOD
}

enum class BillingPlan {
    MONTHLY,
    YEARLY
}
